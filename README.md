# Diagnostic Tool Web Application

A web-based diagnostic tool for managing profiles, devices, and device categories.

## Features

- **Profile Management**: Create, view, edit, and delete diagnostic profiles
- **Device Management**: Manage devices with categories and descriptions
- **Category Management**: Organize devices into categories
- **Responsive Design**: Works on desktop and mobile devices
- **Modern UI**: Built with Bootstrap 5 and Thymeleaf

## Technologies Used

- **Backend**:
  - Java 17
  - Spring Boot 3.2.0
  - Spring Data JPA
  - H2 Database (embedded)
  - Maven

- **Frontend**:
  - HTML5
  - CSS3 (with Bootstrap 5)
  - JavaScript (ES6+)
  - Thymeleaf template engine
  - jQuery 3.6.0
  - Select2 for enhanced select inputs

## Prerequisites

- Java 17 or higher
- Maven 3.8.6 or higher
- Internet connection (for downloading dependencies)

## Getting Started

### Running the Application

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd diagnostic-tool-web
   ```

2. Build the application:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

### Default Access

- The application will be available at: http://localhost:8080
- H2 Database Console: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:file:./data/diagnosticdb
  - Username: sa
  - Password: (leave empty)

## Project Structure

```
src/main/java/
├── com/intervale/diagnostictool/
│   ├── config/           # Configuration classes
│   ├── controller/       # MVC Controllers
│   ├── exception/        # Exception handling
│   ├── model/            # Entity classes
│   ├── repository/       # Data access layer
│   ├── service/          # Business logic
│   └── DiagnosticToolApplication.java  # Main application class

src/main/resources/
├── static/               # Static resources (CSS, JS, images)
│   ├── css/
│   └── js/
└── templates/            # Thymeleaf templates
    ├── devices/
    ├── device-categories/
    ├── profiles/
    └── fragments/        # Reusable template fragments
```

## Development

### Adding a New Feature

1. Create a new branch for your feature:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and commit them:
   ```bash
   git add .
   git commit -m "Add your feature description"
   ```

3. Push to the branch:
   ```bash
   git push origin feature/your-feature-name
   ```

4. Create a Pull Request

### Code Style

This project follows the Google Java Style Guide. Please ensure your code adheres to these standards before submitting a PR.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Bootstrap](https://getbootstrap.com/)
- [Thymeleaf](https://www.thymeleaf.org/)
- [H2 Database](https://www.h2database.com/)
