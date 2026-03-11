/**
 * Profile View Controller
 * Главный контроллер страницы просмотра профиля
 */

import { CoverageManager } from './coverage-manager.js';
import { DCCOMPCalculator } from './dccomp-calculator.js';
import { SafetyIntegrityCalculator } from './safety-calculator.js';

export class ProfileViewController {
    constructor() {
        this.coverageManager = new CoverageManager();
        this.dccompCalculator = new DCCOMPCalculator();
        this.safetyCalculator = new SafetyIntegrityCalculator();

        this.profileId = this.getProfileIdFromUrl();
        this.initialized = false;

        // Debounce timers для сохранения
        this.sValueDebounceTimer = null;
        this.lambdaDebounceTimer = null;
    }

    /**
     * Инициализация контроллера
     */
    async init() {
        if (this.initialized) return;

        console.log('Initializing Profile View Controller...');

        try {
            // Загружаем сохраненные методы
            await this.coverageManager.loadSavedMethods(this.profileId);

            // Ждем обновления DOM
            await new Promise(resolve => setTimeout(resolve, 100));

            // Инициализируем покрытие
            this.initializeCoverage();

            // Инициализируем обработчики событий
            this.setupEventListeners();

            // Выполняем начальные расчеты
            this.dccompCalculator.update(true);
            this.safetyCalculator.calculate();

            this.initialized = true;
            console.log('Profile View Controller initialized successfully');
        } catch (error) {
            console.error('Error initializing Profile View Controller:', error);
            this.showToast('Ошибка при инициализации страницы', 'danger');
        }
    }

    /**
     * Инициализировать покрытие
     */
    initializeCoverage() {
        const selects = document.querySelectorAll('.diagnostic-method-select');

        selects.forEach(select => {
            const selectedOption = select.options[select.selectedIndex];
            const coverageInput = select.closest('.fault-item')?.querySelector('.coverage-percent');

            if (!coverageInput) return;

            const faultId = select.dataset.faultId;
            const deviceId = select.dataset.deviceId;
            const methodId = selectedOption?.value;

            if (methodId) {
                // Пробуем загрузить сохраненное покрытие
                const cachedCoverage = this.coverageManager.getCachedCoverage(
                    this.profileId,
                    deviceId,
                    faultId,
                    methodId
                );

                if (cachedCoverage !== undefined) {
                    coverageInput.value = cachedCoverage;
                    console.log('Using cached coverage:', cachedCoverage);
                } else {
                    // Используем значение по умолчанию
                    const defaultCoverage = this.coverageManager.getDefaultCoverageForMethod(methodId);
                    coverageInput.value = defaultCoverage;
                    console.log('Using default coverage:', defaultCoverage);
                }

                // Включаем кнопки
                const buttons = coverageInput.closest('.input-group')?.querySelectorAll('button');
                buttons?.forEach(btn => btn.disabled = false);
            } else {
                coverageInput.value = '0';
            }
        });

        console.log('Coverage inputs initialization complete');
    }

    /**
     * Настроить обработчики событий
     */
    setupEventListeners() {
        // Изменение метода диагностики
        document.addEventListener('change', async (e) => {
            if (e.target.classList.contains('diagnostic-method-select')) {
                await this.handleMethodChange(e.target);
            }
        });

        // Изменение покрытия
        document.addEventListener('change', async (e) => {
            if (e.target.classList.contains('coverage-percent')) {
                await this.handleCoverageChange(e.target);
            }
        });

        // Изменение S значения
        document.addEventListener('input', (e) => {
            if (e.target.classList.contains('s-value')) {
                this.handleSValueChange(e.target);
            }
        });

        // Изменение lambda
        document.addEventListener('input', (e) => {
            if (e.target.classList.contains('lambda-value')) {
                this.handleLambdaChange(e.target);
            }
        });

        // Изменение архитектуры
        const architectureSelect = document.getElementById('architectureType');
        if (architectureSelect) {
            architectureSelect.addEventListener('change', () => {
                this.handleArchitectureChange(architectureSelect);
            });
        }

        // Сортировка таблицы
        document.querySelectorAll('th.sortable').forEach(th => {
            th.addEventListener('click', () => {
                this.handleTableSort(th);
            });
        });

        // Модальное окно
        const modalButton = document.getElementById('openCoverageModalBtn');
        if (modalButton) {
            modalButton.addEventListener('click', () => {
                const modal = new bootstrap.Modal(document.getElementById('coverageLevelsModal'));
                modal.show();
            });
        }
    }

    /**
     * Обработать изменение метода
     */
    async handleMethodChange(select) {
        const methodId = select.value;
        const faultId = select.dataset.faultId;
        const deviceId = select.dataset.deviceId;
        const coverageInput = select.closest('.fault-item')?.querySelector('.coverage-percent');

        if (!faultId || !deviceId || !this.profileId) {
            console.error('Missing required IDs');
            return;
        }

        // Если выбрано "Выберите метод"
        if (!methodId) {
            coverageInput.value = 0;
            await this.clearMethod(deviceId, faultId);
            this.dccompCalculator.update(true);
            return;
        }

        // Определяем покрытие по умолчанию
        const selectedOption = select.options[select.selectedIndex];
        const coverageMatch = selectedOption.text.match(/\((LOW|MEDIUM|HIGH)\)/);
        let coveragePercent = 0;

        if (coverageMatch) {
            switch (coverageMatch[1]) {
                case 'LOW': coveragePercent = 60; break;
                case 'MEDIUM': coveragePercent = 90; break;
                case 'HIGH': coveragePercent = 99; break;
            }
        }

        coverageInput.value = coveragePercent;

        // Сохраняем
        try {
            await this.coverageManager.saveCoverage(
                this.profileId,
                deviceId,
                faultId,
                methodId,
                coveragePercent
            );

            this.dccompCalculator.update(true);
            this.safetyCalculator.calculate();
        } catch (error) {
            console.error('Error saving method:', error);
            this.showToast('Ошибка при сохранении метода диагностики', 'danger');
        }
    }

    /**
     * Очистить метод
     */
    async clearMethod(deviceId, faultId) {
        try {
            const response = await fetch(
                `/api/profiles/${this.profileId}/faults/devices/${deviceId}/faults/${faultId}/clear-method`,
                { method: 'POST' }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            console.log('Method cleared successfully');
            this.safetyCalculator.calculate();
        } catch (error) {
            console.error('Error clearing method:', error);
            this.showToast('Ошибка при удалении метода диагностики', 'danger');
        }
    }

    /**
     * Обработать изменение покрытия
     */
    async handleCoverageChange(input) {
        if (this.coverageManager.isUpdating || this.coverageManager.isLoading) {
            this.dccompCalculator.update();
            return;
        }

        const coveragePercent = parseInt(input.value) || 0;
        const faultId = input.dataset.faultId;
        const deviceId = input.dataset.deviceId;

        const select = input.closest('.fault-item')?.querySelector('.diagnostic-method-select');
        const methodId = select?.value;

        if (!methodId || !faultId || !deviceId) {
            console.warn('Missing required IDs for saving coverage');
            this.dccompCalculator.update();
            return;
        }

        try {
            await this.coverageManager.saveCoverage(
                this.profileId,
                deviceId,
                faultId,
                methodId,
                coveragePercent
            );

            this.dccompCalculator.update();
            this.safetyCalculator.calculate();
        } catch (error) {
            console.error('Error saving coverage:', error);
        }
    }

    /**
     * Обработать регулировку покрытия
     */
    async handleCoverageAdjust(button, change) {
        const inputGroup = button.closest('.input-group');
        const input = inputGroup?.querySelector('input.coverage-percent');

        if (!input) return;

        const deviceId = input.dataset.deviceId;
        const faultId = input.dataset.faultId;

        const select = document.querySelector(
            `.diagnostic-method-select[data-device-id="${deviceId}"][data-fault-id="${faultId}"]`
        );
        const methodId = select?.value;

        if (!methodId) {
            this.showToast('Сначала выберите метод диагностики', 'warning');
            return;
        }

        // Корректируем значение
        let value = parseInt(input.value) || 0;
        value = Math.max(0, Math.min(100, value + change));

        this.coverageManager.setUpdating(true);
        input.value = value;
        this.coverageManager.setUpdating(false);

        // Сохраняем
        try {
            await this.coverageManager.saveCoverage(
                this.profileId,
                deviceId,
                faultId,
                methodId,
                value
            );

            this.dccompCalculator.update();
            this.safetyCalculator.calculate();
        } catch (error) {
            console.error('Error saving coverage:', error);
            this.showToast('Ошибка при сохранении покрытия', 'danger');

            // Восстанавливаем предыдущее значение
            const previousValue = parseInt(input.dataset.prevValue) || 0;
            this.coverageManager.setUpdating(true);
            input.value = previousValue;
            this.coverageManager.setUpdating(false);
        }
    }

    /**
     * Обработать изменение S значения
     */
    handleSValueChange(input) {
        let value = parseFloat(input.value) || 0;

        // Ограничиваем значение
        value = Math.max(0, Math.min(1, value));
        input.value = value.toFixed(1);

        // Обновляем D
        const row = input.closest('tr');
        const dInput = row?.querySelector('.d-value');
        if (dInput) {
            dInput.value = (1 - value).toFixed(1);
        }

        // Обновляем расчеты
        if (row) {
            this.dccompCalculator.updateLambdaCalculations(row);
        }

        this.safetyCalculator.calculate();

        // Сохраняем значение
        this.saveSValue(input, value);
    }

    /**
     * Сохранить S значение на сервере
     */
    async saveSValue(input, value) {
        const row = input.closest('tr');
        const deviceId = row?.dataset.deviceId;

        if (!deviceId || !this.profileId) return;

        // Debounce: отменяем предыдущий таймер
        if (this.sValueDebounceTimer) {
            clearTimeout(this.sValueDebounceTimer);
        }

        // Сохраняем через 500ms после последнего изменения
        this.sValueDebounceTimer = setTimeout(async () => {
            try {
                const response = await fetch(
                    `/api/profiles/${this.profileId}/device-config/devices/${deviceId}/s-value?value=${value}`,
                    {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }
                );

                if (!response.ok) {
                    console.error('Error saving S value:', response.status);
                }
            } catch (error) {
                console.error('Error saving S value:', error);
            }
        }, 500);
    }

    /**
     * Регулировка S через кнопки +/- (для совместимости с inline-обработчиками)
     * Использует jQuery как в оригинальном коде
     */
    adjustSValue(button, direction) {
        const $input = $(button).siblings('input.s-value');
        let value = parseFloat($input.val()) || 0.5;
        const step = 0.1;
        value += direction * step;
        value = Math.max(0, Math.min(1, value));
        $input.val(value.toFixed(1));
        this.handleSValueChange($input[0]);
        return false;
    }

    /**
     * Регулировка покрытия через кнопки +/- (для совместимости с inline-обработчиками)
     */
    async adjustCoverage(button, change) {
        await this.handleCoverageAdjust(button, change);
        return false;
    }

    /**
     * Обработать изменение lambda
     */
    handleLambdaChange(input) {
        const row = input.closest('tr');
        if (row) {
            this.dccompCalculator.updateLambdaCalculations(row);
        }
        this.safetyCalculator.calculate();

        // Сохраняем значение
        this.saveLambdaValue(input, parseFloat(input.value) || 0);
    }

    /**
     * Сохранить lambda значение на сервере
     */
    async saveLambdaValue(input, value) {
        const row = input.closest('tr');
        const deviceId = row?.dataset.deviceId;

        if (!deviceId || !this.profileId) return;

        // Debounce: отменяем предыдущий таймер
        if (this.lambdaDebounceTimer) {
            clearTimeout(this.lambdaDebounceTimer);
        }

        // Сохраняем через 500ms после последнего изменения
        this.lambdaDebounceTimer = setTimeout(async () => {
            try {
                const response = await fetch(
                    `/api/profiles/${this.profileId}/device-config/devices/${deviceId}/lambda-value?value=${value}`,
                    {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }
                );

                if (!response.ok) {
                    console.error('Error saving lambda value:', response.status);
                }
            } catch (error) {
                console.error('Error saving lambda value:', error);
            }
        }, 500);
    }

    /**
     * Обработать изменение типа архитектуры
     */
    async handleArchitectureChange(select) {
        const architecture = select.value;

        if (!architecture || !this.profileId) return;

        try {
            const response = await fetch(
                `/api/profiles/${this.profileId}/architecture?architecture=${architecture}`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (response.ok) {
                this.safetyCalculator.calculate();
                console.log('Architecture saved successfully');
            } else {
                console.error('Error saving architecture:', response.status);
                this.showToast('Ошибка при сохранении архитектуры', 'danger');
            }
        } catch (error) {
            console.error('Error saving architecture:', error);
            this.showToast('Ошибка при сохранении архитектуры', 'danger');
        }
    }

    /**
     * Обработать сортировку таблицы
     */
    handleTableSort(th) {
        const table = document.getElementById('devicesTable');
        const columnIndex = th.cellIndex;
        const isAsc = !th.classList.contains('sort-asc');

        // Обновляем индикаторы
        table.querySelectorAll('th.sortable').forEach(header => {
            header.classList.remove('sort-asc', 'sort-desc');
        });
        th.classList.add(isAsc ? 'sort-asc' : 'sort-desc');

        // Сортируем
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));

        rows.sort((a, b) => {
            const aValue = a.cells[columnIndex]?.textContent.trim().toLowerCase() || '';
            const bValue = b.cells[columnIndex]?.textContent.trim().toLowerCase() || '';
            return isAsc ? aValue.localeCompare(bValue) : bValue.localeCompare(aValue);
        });

        rows.forEach(row => tbody.appendChild(row));
    }

    /**
     * Получить ID профиля из URL
     */
    getProfileIdFromUrl() {
        const match = window.location.pathname.match(/\/profiles\/(\d+)/);
        return match ? match[1] : null;
    }

    /**
     * Показать toast уведомление
     */
    showToast(message, type = 'info') {
        const toastId = 'toast-' + Date.now();
        const toastHtml = `
            <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0"
                 role="alert" aria-live="assertive" aria-atomic="true"
                 style="position: fixed; top: 20px; right: 20px; z-index: 1100; min-width: 250px;">
                <div class="d-flex">
                    <div class="toast-body">
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto"
                            data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', toastHtml);
        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement);
        toast.show();

        toastElement.addEventListener('hidden.bs.toast', () => {
            toastElement.remove();
        });
    }
}
