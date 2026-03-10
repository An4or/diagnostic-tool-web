/**
 * Coverage Manager Module
 * Управление покрытием диагностических методов
 */

export class CoverageManager {
    constructor() {
        this.cache = new Map();
        this.isUpdating = false;
        this.isLoading = false;
    }

    /**
     * Получить ключ кэша
     */
    getCacheKey(profileId, deviceId, faultId, methodId) {
        return `${profileId}_${deviceId}_${faultId}_${methodId}`;
    }

    /**
     * Загрузить сохраненные методы диагностики
     */
    async loadSavedMethods(profileId) {
        if (!profileId) return;

        try {
            const [faultsResponse, coverageResponse] = await Promise.all([
                fetch(`/api/profiles/${profileId}/faults`),
                fetch(`/api/profiles/${profileId}/faults/coverage-percentages`)
            ]);

            const faults = await faultsResponse.json();
            const coveragePercentages = await coverageResponse.json();

            console.log('Loaded saved faults:', faults);
            console.log('Loaded coverage percentages:', coveragePercentages);

            // Обработка загруженных данных
            this.processLoadedData(faults, coveragePercentages, profileId);

            return { faults, coveragePercentages };
        } catch (error) {
            console.error('Error loading saved methods:', error);
            throw error;
        }
    }

    /**
     * Обработать загруженные данные
     */
    processLoadedData(faults, coveragePercentages, profileId) {
        // Сначала обрабатываем faults для восстановления выбранных методов
        faults.forEach(fault => {
            if (fault.coveredMethodsIds) {
                try {
                    const coverageData = JSON.parse(fault.coveredMethodsIds);
                    console.log('Parsed coverage data for fault', fault.faultTypeId, ':', coverageData);

                    for (const [deviceIdStr, methodData] of Object.entries(coverageData)) {
                        const deviceId = parseInt(deviceIdStr);
                        for (const [methodIdStr, defaultCoveragePercent] of Object.entries(methodData)) {
                            const methodId = parseInt(methodIdStr);
                            
                            // Восстанавливаем выбранный метод в select
                            const select = document.querySelector(
                                `.diagnostic-method-select[data-device-id="${deviceId}"][data-fault-id="${fault.faultTypeId}"]`
                            );
                            if (select && methodId) {
                                select.value = methodId;
                                console.log(`Restored method ${methodId} for fault ${fault.faultTypeId} on device ${deviceId}`);
                            }

                            // Кэшируем покрытие
                            const cacheKey = this.getCacheKey(profileId, deviceId, fault.faultTypeId, methodId);
                            
                            let coverageValue = parseInt(defaultCoveragePercent);
                            if (isNaN(coverageValue)) {
                                coverageValue = this.getDefaultCoverageForMethod(methodId);
                            }

                            this.cache.set(cacheKey, coverageValue);
                            console.log(`Cached coverage for ${cacheKey} = ${coverageValue}`);

                            // Устанавливаем значение покрытия в input
                            const coverageInput = select?.closest('.fault-item')?.querySelector('.coverage-percent');
                            if (coverageInput) {
                                coverageInput.value = coverageValue;
                                // Включаем кнопки +/-
                                const buttons = coverageInput.closest('.input-group')?.querySelectorAll('button');
                                buttons?.forEach(btn => btn.disabled = false);
                                console.log(`Set coverage input value to ${coverageValue}`);
                            }
                        }
                    }
                } catch (e) {
                    console.warn('Error parsing covered methods for fault', fault.faultTypeId, ':', e);
                }
            }
        });

        // Обрабатываем coverage-percentages для дополнительных данных
        for (const [key, value] of Object.entries(coveragePercentages)) {
            if (value !== null && value !== undefined) {
                this.cache.set(key, value);
                console.log(`Cached coverage from percentages API: ${key} = ${value}`);
            }
        }
    }

    /**
     * Получить покрытие по умолчанию для метода
     */
    getDefaultCoverageForMethod(methodId) {
        const select = document.querySelector(`.diagnostic-method-select option[value="${methodId}"]`);
        if (!select) return 60;

        const text = select.textContent;
        const match = text.match(/\((LOW|MEDIUM|HIGH)\)/);
        if (!match) return 60;

        switch (match[1]) {
            case 'LOW': return 60;
            case 'MEDIUM': return 90;
            case 'HIGH': return 99;
            default: return 60;
        }
    }

    /**
     * Сохранить покрытие на сервере
     */
    async saveCoverage(profileId, deviceId, faultId, methodId, coveragePercent) {
        console.log('Сохранение покрытия:', { profileId, deviceId, faultId, methodId, coveragePercent });

        // Очищаем старые значения для этой неисправности
        for (const key of this.cache.keys()) {
            if (key.startsWith(`${profileId}_${deviceId}_${faultId}_`)) {
                this.cache.delete(key);
            }
        }

        // Кэшируем новое значение
        const cacheKey = this.getCacheKey(profileId, deviceId, faultId, methodId);
        this.cache.set(cacheKey, coveragePercent);

        try {
            const response = await fetch(`/api/profiles/${profileId}/faults/devices/${deviceId}/faults/${faultId}/method`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    methodId: methodId,
                    coveragePercent: coveragePercent
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('Покрытие успешно сохранено:', result);
            return result;
        } catch (error) {
            console.error('Ошибка при сохранении покрытия:', error);
            this.cache.delete(cacheKey);
            throw error;
        }
    }

    /**
     * Получить кэшированное покрытие
     */
    getCachedCoverage(profileId, deviceId, faultId, methodId) {
        const cacheKey = this.getCacheKey(profileId, deviceId, faultId, methodId);
        return this.cache.get(cacheKey);
    }

    /**
     * Установить флаг обновления
     */
    setUpdating(value) {
        this.isUpdating = value;
    }

    /**
     * Установить флаг загрузки
     */
    setLoading(value) {
        this.isLoading = value;
    }
}