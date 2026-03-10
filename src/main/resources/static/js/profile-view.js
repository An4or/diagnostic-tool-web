/**
 * Profile View Entry Point
 * Точка входа для страницы просмотра профиля
 */

import { ProfileViewController } from './modules/profile-view-controller.js';

// Создаём контроллер синхронно для немедленного экспорта функций
const controller = new ProfileViewController();

// Экспортируем функции в глобальную область видимости для inline-обработчиков
window.adjustSValue = (button, direction) => controller.adjustSValue(button, direction);
window.adjustPercentage = (button, change) => controller.adjustCoverage(button, change);
window.profileViewController = controller;

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', async () => {
    await controller.init();
    console.log('Profile View Controller initialized');
});