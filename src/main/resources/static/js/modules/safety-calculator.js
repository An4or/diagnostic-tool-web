/**
 * Safety Integrity Calculator Module
 * Расчет УПБ (Уровня полноты безопасности)
 */

export class SafetyIntegrityCalculator {
    constructor() {
        this.architectureSelect = document.getElementById('architectureType');
    }

    /**
     * Рассчитать УПБ
     */
    calculate() {
        const selectedArchitecture = this.architectureSelect?.value;
        if (!selectedArchitecture) return null;

        // Агрегируем суммы по всем устройствам
        let totalLambdaS = 0;
        let totalLambdaDD = 0;
        let totalLambdaD = 0;
        let totalLambdaDU = 0;
        let hasData = false;

        const deviceRows = document.querySelectorAll('table:has(.dc-comp-cell) tbody tr[data-device-id]');
        
        deviceRows.forEach(row => {
            const lambdaS = parseFloat(row.getAttribute('data-lambda-s') || '0');
            const lambdaDD = parseFloat(row.getAttribute('data-lambda-dd') || '0');
            const lambdaDU = parseFloat(row.getAttribute('data-lambda-du') || '0');
            const totalLambdaPerDevice = parseFloat(row.getAttribute('data-total-lambda') || '0');

            if (totalLambdaPerDevice > 0) {
                hasData = true;
                totalLambdaS += lambdaS;
                totalLambdaDD += lambdaDD;
                totalLambdaDU += lambdaDU;
                totalLambdaD += (totalLambdaPerDevice - lambdaS);
            }
        });

        const totalDenominator = totalLambdaS + totalLambdaD;
        const sff = totalDenominator > 0 ? ((totalLambdaS + totalLambdaDD) / totalDenominator) * 100 : 0;
        const minCoveragePercent = hasData ? sff : 0;

        // Определяем SIL
        const sil = this.determineSIL(selectedArchitecture, minCoveragePercent);

        // Обновляем таблицу
        this.updateTable(selectedArchitecture, minCoveragePercent);

        // Обновляем summary
        this.updateSummary(hasData, totalLambdaS, totalLambdaDD, totalLambdaDU, sff, sil, selectedArchitecture);

        return {
            coveragePercent: minCoveragePercent,
            sil: sil
        };
    }

    /**
     * Определить УПБ на основе архитектуры и покрытия
     */
    determineSIL(architecture, coveragePercent) {
        const n = this.getFaultTolerance(architecture);

        if (coveragePercent < 60) {
            return n >= 1 ? (n >= 2 ? 'УПБ 2' : 'УПБ 1') : '-';
        } else if (coveragePercent < 90) {
            return n === 0 ? 'УПБ 1' : n === 1 ? 'УПБ 2' : 'УПБ 3';
        } else if (coveragePercent < 99) {
            return n === 0 ? 'УПБ 2' : n === 1 ? 'УПБ 3' : 'УПБ 4';
        } else {
            return n === 0 ? 'УПБ 2' : 'УПБ 4';
        }
    }

    /**
     * Получить отказоустойчивость (N) для архитектуры
     */
    getFaultTolerance(architecture) {
        switch (architecture) {
            case 'ONE_OUT_OF_ONE': return 0;
            case 'ONE_OUT_OF_TWO': return 1;
            case 'TWO_OUT_OF_THREE': return 2;
            default: return 0;
        }
    }

    /**
     * Обновить таблицу УПБ
     */
    updateTable(architecture, coveragePercent) {
        const n = this.getFaultTolerance(architecture);

        // Очищаем предыдущие выделения
        document.querySelectorAll('.safety-table td').forEach(td => {
            td.classList.remove('table-primary', 'fw-bold');
        });

        // Находим нужную строку
        const rows = document.querySelectorAll('.safety-table tbody tr');
        let targetRow = null;

        for (const row of rows) {
            const rangeText = row.querySelector('td:first-child')?.textContent.trim();

            if ((rangeText?.includes('менее 60%') && coveragePercent < 60) ||
                (rangeText?.includes('от 60% до менее 90%') && coveragePercent >= 60 && coveragePercent < 90) ||
                (rangeText?.includes('от 90% до менее 99%') && coveragePercent >= 90 && coveragePercent < 99) ||
                (rangeText?.includes('более и равно 99%') && coveragePercent >= 99)) {
                targetRow = row;
                break;
            }
        }

        if (!targetRow) return;

        // Выделяем нужную ячейку
        const cellIndex = n + 1;
        const cell = targetRow.querySelector(`td:nth-child(${cellIndex + 1})`);
        if (cell) {
            cell.classList.add('table-primary', 'fw-bold');
        }
    }

    /**
     * Обновить summary текст
     */
    updateSummary(hasData, totalLambdaS, totalLambdaDD, totalLambdaDU, sff, sil, architecture) {
        const summaryElement = document.getElementById('upb-summary');
        if (!summaryElement) return;

        const architectureText = this.getArchitectureText(architecture);
        const n = this.getFaultTolerance(architecture);

        if (hasData) {
            const lambdaS = totalLambdaS.toFixed(1);
            const lambdaDD = totalLambdaDD.toFixed(1);
            const lambdaDU = totalLambdaDU.toFixed(1);

            summaryElement.innerHTML =
                `ДБО = <span style="font-size: smaller;">(Σ λ<sub>S</sub> + Σ λ<sub>DD</sub>) / (Σ λ<sub>S</sub> + Σ λ<sub>DD</sub> + Σ λ<sub>DU</sub>) × 100%</span> ` +
                `= (${lambdaS} + ${lambdaDD}) / (${lambdaS} + ${lambdaDD} + ${lambdaDU}) × 100% = ${sff.toFixed(1)}% ` +
                `(по архитектуре ${architectureText}, N=${n}) = ${sil}`;
        } else {
            summaryElement.textContent = 'УПБ = -- (формула не рассчитана)';
        }
    }

    /**
     * Получить текстовое представление архитектуры
     */
    getArchitectureText(architecture) {
        switch (architecture) {
            case 'ONE_OUT_OF_ONE': return '1oo1';
            case 'ONE_OUT_OF_TWO': return '1oo2';
            case 'TWO_OUT_OF_THREE': return '1oo3';
            default: return architecture;
        }
    }
}
