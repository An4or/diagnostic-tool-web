/**
 * DCCOMP Calculator Module
 * Расчет DC_COMP (диагностического охвата)
 */

export class DCCOMPCalculator {
    constructor() {
        this.isUpdating = false;
        this.lastUpdateTime = 0;
        this.UPDATE_DEBOUNCE_TIME = 500;
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

                let totalCoverage = 0;
                let selectedMethodsCount = 0;

                methodSelects.forEach((select, idx) => {
                    const selectedOption = select.options[select.selectedIndex];
                    
                    if (selectedOption && selectedOption.value) {
                        const faultItem = select.closest('.fault-item');
                        const coverageInput = faultItem?.querySelector('.coverage-percent');
                        const coverage = parseInt(coverageInput?.value) || 0;

                        console.group(`Method ${idx + 1}:`);
                        console.log('Selected option:', selectedOption.text);
                        console.log('Coverage input value:', coverageInput?.value);
                        console.log('Parsed coverage:', coverage);

                        if (coverage > 0) {
                            console.log('Adding to total coverage');
                            totalCoverage += coverage;
                            selectedMethodsCount++;
                        } else {
                            console.log('Skipping - coverage is 0 or invalid');
                        }
                        console.groupEnd();
                    } else {
                        console.log(`Method ${idx + 1}: No method selected`);
                    }
                });

                console.log(`Total coverage: ${totalCoverage}, Selected methods: ${selectedMethodsCount}`);

                const averageCoverage = selectedMethodsCount > 0
                    ? Math.round((totalCoverage / selectedMethodsCount) * 10) / 10
                    : 0;

                const currentValue = parseFloat(dcCompCell?.textContent) || 0;
                const needsUpdate = Math.abs(currentValue - averageCoverage) > 0.1;

                console.log(`Current DCCOMP: ${currentValue}%, New value: ${averageCoverage}%`);
                console.log('Needs update:', needsUpdate);

                if (needsUpdate || force) {
                    console.log(`Updating DCCOMP: ${currentValue}% -> ${averageCoverage}%`);
                    if (dcCompCell) {
                        dcCompCell.textContent = averageCoverage;
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
                cell.textContent = value.toFixed(2);
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
