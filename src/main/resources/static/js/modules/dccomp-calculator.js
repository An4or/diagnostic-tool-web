/**
 * DCCOMP Calculator Module
 * Расчет DC_COMP (диагностического охвата)
 */

export class DCCOMPCalculator {
    constructor() {
        this.isUpdating = false;
        this.lastUpdateTime = 0;
        this.UPDATE_DEBOUNCE_TIME = 500;
        this.coverageWeights = {
            LOW: 0.6,
            MEDIUM: 0.3,
            HIGH: 0.1
        };
    }

    /**
     * Обновить DCCOMP для всех устройств
     */
    update(force = false) {
        const now = Date.now();

        if ((this.isUpdating || (now - this.lastUpdateTime < this.UPDATE_DEBOUNCE_TIME)) && !force) {
            console.log('Skipping DCCOMP update: update in progress or too soon');
            return;
        }

        this.isUpdating = true;
        this.lastUpdateTime = now;

        console.group('=== DCCOMP Update Start ===');
        console.log('Force update:', force);

        try {
            const deviceRows = document.querySelectorAll('#devicesTable > tbody > tr');
            let updatesMade = false;

            deviceRows.forEach((deviceRow, index) => {
                const deviceName = deviceRow.querySelector('td:nth-child(2)')?.textContent.trim();
                console.group(`Device ${index + 1}: "${deviceName}"`);

                const calcRow = this.findCalculationRow(deviceName);
                if (!calcRow) {
                    console.warn('No calculation row found for device:', deviceName);
                    console.groupEnd();
                    return;
                }

                const dcCompCell = calcRow.querySelector('.dc-comp-cell');
                const methodSelects = deviceRow.querySelectorAll('.diagnostic-method-select');

                console.log(`Found ${methodSelects.length} method selects for device`);

                const groupStats = {
                    LOW: { count: 0, totalCoverage: 0 },
                    MEDIUM: { count: 0, totalCoverage: 0 },
                    HIGH: { count: 0, totalCoverage: 0 }
                };

                methodSelects.forEach((select, idx) => {
                    const selectedOption = select.options[select.selectedIndex];

                    if (selectedOption && selectedOption.value) {
                        const faultItem = select.closest('.fault-item');
                        const coverageInput = faultItem?.querySelector('.coverage-percent');
                        const coverage = parseInt(coverageInput?.value) || 0;
                        const coverageLevel = (faultItem?.dataset?.coverageLevel || '').toUpperCase();

                        console.group(`Method ${idx + 1}:`);
                        console.log('Selected option:', selectedOption.text);
                        console.log('Coverage level:', coverageLevel);
                        console.log('Coverage input value:', coverageInput?.value);
                        console.log('Parsed coverage:', coverage);

                        if (!this.coverageWeights[coverageLevel]) {
                            console.log('Skipping - unknown coverage requirement group');
                        } else if (coverage > 0) {
                            console.log(`Adding to group ${coverageLevel}`);
                            groupStats[coverageLevel].count++;
                            groupStats[coverageLevel].totalCoverage += coverage;
                        } else {
                            console.log('Skipping - coverage is 0 or invalid');
                        }
                        console.groupEnd();
                    } else {
                        console.log(`Method ${idx + 1}: No method selected`);
                    }
                });

                let dcComp = 0;
                Object.entries(this.coverageWeights).forEach(([group, weight]) => {
                    const groupCount = groupStats[group].count;
                    const groupCoverageSum = groupStats[group].totalCoverage;
                    const contribution = groupCount > 0
                        ? (weight / groupCount) * groupCoverageSum
                        : 0;

                    console.log(`${group}: count=${groupCount}, coverageSum=${groupCoverageSum}, weight=${weight}, contribution=${contribution.toFixed(2)}%`);
                    dcComp += contribution;
                });

                const roundedDcComp = Math.round(dcComp * 10) / 10;

                const currentValue = parseFloat(dcCompCell?.textContent) || 0;
                const currentDisplay = currentValue.toFixed(1)
                const newDisplay = roundedDcComp.toFixed(1)
                const needsUpdate = currentDisplay !== newDisplay
                // const needsUpdate = Math.abs(currentValue - roundedDcComp) > 0.1;

                console.log(`Current DCCOMP: ${currentValue}%, New value: ${roundedDcComp}%`);
                console.log('Needs update:', needsUpdate);

                if (needsUpdate || force) {
                    console.log(`Updating DCCOMP: ${currentValue}% -> ${roundedDcComp}%`);
                    if (dcCompCell) {
                        dcCompCell.textContent = roundedDcComp;
                    }
                    updatesMade = true;

                    // Обновляем lambda расчеты
                    this.updateLambdaCalculations(calcRow);
                } else {
                    console.log('No update needed - value unchanged or change too small');
                }

                console.groupEnd();
            });

            if (updatesMade) {
                console.log('DCCOMP update complete with changes');
            } else {
                console.log('DCCOMP update complete (no changes)');
            }

            console.groupEnd();
        } catch (e) {
            console.error('Error in updateDCCOMP:', e);
        } finally {
            setTimeout(() => {
                this.isUpdating = false;
            }, 50);
        }
    }

    /**
     * Найти строку в таблице расчетов по имени устройства
     */
    findCalculationRow(deviceName) {
        const calcRows = document.querySelectorAll('table:has(.dc-comp-cell) tbody tr');

        for (const row of calcRows) {
            const firstCell = row.querySelector('td:first-child');
            if (firstCell && firstCell.textContent.trim() === deviceName) {
                return row;
            }
        }

        // Попробуем найти по частичному совпадению
        for (const row of calcRows) {
            if (row.textContent.includes(deviceName)) {
                return row;
            }
        }

        return null;
    }

    /**
     * Обновить lambda расчеты
     */
    updateLambdaCalculations(row) {
        const sValue = parseFloat(row.querySelector('.s-value')?.value) || 0;
        const dValue = 1 - sValue;
        const lambda = parseFloat(row.querySelector('.lambda-value')?.value) || 10;

        const lambdaS = sValue * lambda;
        const lambdaD = dValue * lambda;
        const dcComp = parseFloat(row.querySelector('.dc-comp-cell')?.textContent) / 100 || 0;
        const lambdaDD = dcComp * lambdaD;
        const lambdaDU = lambdaD - lambdaDD;

        const totalLambda = lambdaS + lambdaD;
        const sff = totalLambda > 0 ? ((lambdaS + lambdaDD) / totalLambda) * 100 : 0;

// Обновляем ячейки
        const updateCell = (selector, value) => {
            const cell = row.querySelector(selector);
            if (cell) {
                cell.textContent = value.toFixed(1);
            }
        };

        updateCell('[data-formula="s*lambda"]', lambdaS);
        updateCell('[data-formula="d*lambda"]', lambdaD);
        updateCell('[data-formula="lambda_dd"]', lambdaDD);
        updateCell('[data-formula="lambda_du"]', lambdaDU);
        updateCell('[data-formula="lambda_dd_du"]', lambdaD);
        updateCell('[data-formula="lambda_s_plus_lambda_dd"]', lambdaS + lambdaDD);
        // Сохраняем данные для агрегации
        row.setAttribute('data-lambda-s', lambdaS.toFixed(1));
        row.setAttribute('data-lambda-dd', lambdaDD.toFixed(1));
        row.setAttribute('data-total-lambda', totalLambda.toFixed(1));
        row.setAttribute('data-sff', sff.toFixed(1));
        row.setAttribute('data-lambda-du', lambdaDU.toFixed(1));

        console.log(`Updated calculations:`, {
            s: sValue,
            d: dValue,
            lambda,
            lambdaS: lambdaS.toFixed(4),
            lambdaD: lambdaD.toFixed(4),
            dcComp: (dcComp * 100).toFixed(0),
            lambdaDD: lambdaDD.toFixed(4),
            sff: sff.toFixed(2) + '%'
        });
    }
}
