# KODA.md — Инструкции для работы с проектом

## Обзор проекта

Веб-приложение для диагностики, разработанное компанией Intervale. Представляет собой инструмент управления профилями, устройствами и категориями устройств для проведения диагностических операций.

### Основные технологии

- **Backend**: Java 17, Spring Boot 3.2.0, Spring Data JPA
- **База данных**: H2 (встраиваемая база данных)
- **Frontend**: HTML5, CSS3 (Bootstrap 5), JavaScript (ES6+), Thymeleaf, jQuery 3.6.0, Select2
- **Сборка**: Maven 3.8.6+
- **Миграции БД**: Flyway

### Архитектура проекта

```
src/main/java/com/intervale/diagnostictool/
├── config/           # Конфигурационные классы (Spring Security, Web, OpenAPI)
├── controller/       # MVC-контроллеры (включая REST API)
├── dto/              # Data Transfer Objects и запросы
├── exception/        # Обработка исключений
├── mapper/           # Мапперы объектов
├── model/            # Сущности БД (JPA-entities), включая enums
├── repository/       # Слой доступа к данным (Spring Data JPA)
├── service/          # Бизнес-логика
└── util/             # Утилиты

src/main/resources/
├── static/           # Статические ресурсы (CSS, JS)
├── templates/        # Thymeleaf-шаблоны
└── db/migration/     # Миграции Flyway (V1, V2)
```

---

## Сборка и запуск

### Предварительные требования

- Java 17 или выше
- Maven 3.8.6 или выше
- Интернет-соединение (для загрузки зависимостей)

### Команды для сборки и запуска

```bash
# Клонирование репозитория
git clone <repository-url>
cd diagnostic-tool-web

# Сборка приложения
mvn clean package

# Запуск приложения
mvn spring-boot:run
```

Альтернативный способ запуска (после сборки):

```bash
./start.sh
# или
java -jar target/diagnostic-tool-web.jar
```

### Доступ к приложению

- **URL приложения**: http://localhost:8090
- **Консоль H2**: http://localhost:8090/h2-console
  - JDBC URL: `jdbc:h2:file:./data/diagnostic_db`
  - Username: `sa`
  - Password: `password`

### Конфигурация

Основной конфигурационный файл: `src/main/resources/application.yml`

- Порт сервера: `8090` (или значение переменной окружения `PORT`)
- Путь к БД: `./data/diagnostic_db`
- Логирование: Уровень DEBUG для основного пакета, логи пишутся в `logs/application.log`

---

## Правила разработки

### Стиль кодирования

Проект следует **Google Java Style Guide**. Перед отправкой PR убедитесь, что код соответствует этим стандартам.

### Структура импортов

Используется стандартная структура пакетов Java с группировкой:
1. Стандартные Java-классы
2. Сторонние библиотеки (Spring, Hibernate и т.д.)
3. Внутренние пакеты проекта

### Использование Lombok

Проект активно использует Lombok для сокращения шаблонного кода:
- `@Data`, `@Getter`, `@Setter` для сущностей
- `@Builder` для создания объектов
- `@RequiredArgsConstructor` для внедрения зависимостей

### Работа с базой данных

- Миграции управляются через **Flyway**
- Файлы миграций: `src/main/resources/db/migration/V1__initial_schema.sql`, `V2__initial_data.sql`
- DDL-auto: `validate` (не изменяет схему автоматически)

### Тестирование

- Используется `spring-boot-starter-test`
- Для запуска тестов: `mvn test`

---

## Ключевые компоненты

### Модели (Сущности)

- `Profile` — диагностический профиль
- `Device` — устройство
- `DeviceCategory` — категория устройства
- `DiagnosticMethod` — диагностический метод
- `FaultType` — тип неисправности
- `DiagnosticMethodFault` — связь метода диагностики с неисправностью
- `ProfileDiagnosticMethod` — связь профиля с диагностическими методами
- `ProfileFault` — связь профиля с неисправностями

### Основные сервисы

- `ProfileService` — управление профилями
- `DeviceService` — управление устройствами
- `DeviceCategoryService` — управление категориями
- `DiagnosticMethodService` — управление методами диагностики
- `FaultTypeService` — управление типами неисправностей
- `ProfileFaultService` — связь профилей с неисправностями
- `DiagnosticMethodFaultService` — связь методов с неисправностями

### API

- REST API доступен через контроллеры в пакете `controller/api/`
- Swagger/OpenAPI документация: `/swagger-ui.html` (благодаря springdoc-openapi-starter-webmvc-ui)

---

## Рекомендации по расширению

1. **Добавление новой сущности**:
   - Создать класс в `model/`
   - Создать Repository в `repository/`
   - Создать Service в `service/`
   - Создать Controller в `controller/`
   - Создать DTO в `dto/`
   - Добавить шаблоны в `templates/`

2. **Добавление миграции**:
   - Создать файл `V{n}__description.sql` в `src/main/resources/db/migration/`

3. **Изменение стиля CSS**:
   - Основные стили: `src/main/resources/static/css/style.css`
   - Стили для страницы неисправностей: `src/main/resources/static/css/faults.css`

---

## TODO

- [ ] Настроить профили Maven для разных окружений (dev, prod)
- [ ] Добавить unit-тесты для сервисов
- [ ] Настроить CI/CD
- [ ] Добавить аутентификацию и авторизацию (Spring Security)
- [ ] Заменить H2 на PostgreSQL в production
