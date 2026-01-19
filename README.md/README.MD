# Advanced Grade Management System

A comprehensive, production-ready student grade management system built with Java, featuring advanced data management, concurrent operations, multi-format file support, and comprehensive analytics.

## Features

### Core Features
- **Student Management**: Support for Regular and Honors students with validation
- **Grade Recording**: Track grades across multiple subjects with GPA calculation
- **Comprehensive Reporting**: Detailed grade reports and analytics
- **Data Persistence**: In-memory storage with export/import capabilities

### Advanced Features
- **Multi-Format File Support**: Import/export data in CSV, JSON, and Binary formats
- **Real-Time Dashboard**: Live statistics updates with background monitoring
- **Pattern-Based Search**: Regex-powered search capabilities for advanced queries
- **Concurrent Operations**: Thread-safe batch processing with configurable thread pools
- **Automated Tasks**: Scheduled operations (GPA calculation, notifications, reports)
- **Performance Monitoring**: System metrics, memory usage, and operation timing
- **Audit Trail**: Complete operation logging with timestamps and user tracking
- **Data Caching**: LRU cache with expiration for improved performance
- **Comprehensive Validation**: Regex-based input validation with custom patterns
- **Batch Processing**: Bulk import/export operations with progress tracking

##  Architecture

### Project Structure
```
ManagementSystemIII/
├── pom.xml                           # Maven configuration
├── data/                             # Data directories
│   ├── cache/                        # Cache storage
│   ├── exports/                      # Exported files
│   └── imports/                      # Imported files
├── logs/                             # Application logs
├── src/
│   ├── main/
│   │   ├── java/com/grademanagement/
│   │   │   ├── Main.java             # Application entry point
│   │   │   ├── AdvancedGradeManagementSystem.java  # Main application class
│   │   │   ├── collections/          # Custom collection implementations
│   │   │   │   ├── GradeTreeMap.java # Thread-safe grade storage
│   │   │   │   ├── StudentCache.java # Student caching
│   │   │   │   └── ThreadSafeList.java # Concurrent list implementation
│   │   │   ├── concurrent/           # Concurrent processing
│   │   │   │   ├── GradeProcessor.java
│   │   │   │   ├── ReportGenerator.java
│   │   │   │   └── StatisticsUpdater.java
│   │   │   ├── exceptions/           # Custom exceptions
│   │   │   │   ├── FileImportException.java
│   │   │   │   ├── GradeException.java
│   │   │   │   ├── StudentException.java
│   │   │   │   └── ValidationException.java
│   │   │   ├── io/                   # File I/O operations
│   │   │   │   ├── BinaryExporter.java
│   │   │   │   ├── BinaryImporter.java
│   │   │   │   ├── CsvStreamProcessor.java
│   │   │   │   ├── FileWatcher.java
│   │   │   │   ├── JsonExporter.java
│   │   │   └── └── JsonImporter.java
│   │   │   ├── model/                # Data models
│   │   │   │   ├── AuditLog.java
│   │   │   │   ├── ContactInfo.java
│   │   │   │   ├── Grade.java
│   │   │   │   ├── HonorsStudent.java
│   │   │   │   ├── RegularStudent.java
│   │   │   │   ├── Student.java
│   │   │   │   ├── enums/
│   │   │   │   │   ├── FileFormat.java
│   │   │   │   │   ├── GradeLetter.java
│   │   │   │   │   └── Subject.java
│   │   │   │   ├── interfaces/
│   │   │   │   └── subjects/
│   │   │   ├── repository/           # Data access layer
│   │   │   │   ├── AuditRepository.java
│   │   │   │   ├── GradeRepository.java
│   │   │   └── └── StudentRepository.java
│   │   │   ├── service/              # Business logic
│   │   │   │   ├── BatchService.java
│   │   │   │   ├── CacheService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── FileService.java
│   │   │   │   ├── GPACalculator.java
│   │   │   │   ├── GradeService.java
│   │   │   │   ├── NotificationService.java
│   │   │   │   ├── PerformanceMonitor.java
│   │   │   │   ├── ReportService.java
│   │   │   │   ├── SearchService.java
│   │   │   │   ├── StatisticsService.java
│   │   │   │   ├── StudentService.java
│   │   │   │   ├── TaskScheduler.java
│   │   │   └── └── ValidationService.java
│   │   │   └── utils/                # Utilities
│   │   │       ├── Constants.java
│   │   │       ├── Logger.java
│   │   │       ├── PerformanceMetrics.java
│   │   │       ├── RegexPatterns.java
│   │   │       └── Validator.java
│   │   └── resources/                # Configuration files
│   └── test/java/com/grademanagement/  # Unit tests
└── README.md
```

### Design Patterns Used
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic separation
- **Factory Pattern**: Object creation
- **Observer Pattern**: Event-driven updates
- **Strategy Pattern**: Algorithm selection
- **Decorator Pattern**: Feature extension
- **Singleton Pattern**: Resource management

##  Technology Stack

- **Language**: Java 16
- **Build Tool**: Maven 3.x
- **Testing**: JUnit 5, Mockito
- **Code Quality**: Checkstyle, PMD, JaCoCo
- **Logging**: SLF4J with simple logger
- **JSON Processing**: Google Gson
- **CSV Processing**: OpenCSV
- **Collections**: Custom thread-safe implementations

##  Prerequisites

- Java 16 or higher
- Maven 3.6 or higher
- Windows/Linux/macOS

##  Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Harriee01/GradeManagementSystem.git
   cd GradeManagementSystem/ManagementSystemIII
   ```

2. **Build the project**:
   ```bash
   mvn clean compile
   ```

3. **Run tests**:
   ```bash
   mvn test
   ```

4. **Package the application**:
   ```bash
   mvn package
   ```

5. **Run the application**:
   ```bash
   # Using Maven
   mvn exec:java

   # Or using the JAR
   java -jar target/advanced-grade-management-3.0.0.jar
   ```

##  Usage

The application provides a console-based menu interface with the following main categories:

### Student Management
- Add students with validation (Regular/Honors types)
- View all registered students
- Record grades for students
- View individual grade reports

### File Operations
- Export grade reports (CSV/JSON/Binary formats)
- Import data from multiple formats
- Bulk import grades with progress tracking

### Analytics & Reporting
- Calculate individual student GPA
- View class-wide statistics
- Real-time statistics dashboard
- Generate batch reports

### Search & Query
- Advanced student search
- Pattern-based search with regex
- Query grade history

### Advanced Features
- Schedule automated tasks
- View system performance metrics
- Cache management
- Audit trail viewer

##  Configuration

### Constants (src/main/java/com/grademanagement/utils/Constants.java)
- `REGULAR_PASSING_GRADE`: 50.0
- `HONORS_PASSING_GRADE`: 60.0
- `HONORS_THRESHOLD`: 85.0
- `MAX_CACHE_SIZE`: 100
- `CACHE_TIMEOUT`: 3600000ms (1 hour)
- `THREAD_POOL_SIZE`: 4

### Validation Patterns
- **Student ID**: `^[A-Z][A-Z0-9]{2,}$` (e.g., S001, CS2024001)
- **Name**: `^[A-Za-z]{2,}(\s+[A-Za-z]{2,})+$` (First Last format)
- **Email**: University domain validation
- **Phone**: Automatic normalization

## Testing

The project includes comprehensive unit tests covering:
- Service layer functionality
- Validation logic
- Repository operations
- Exception handling
- Concurrent operations

Run tests with:
```bash
mvn test
```

Generate test coverage report:
```bash
mvn jacoco:report
```



Run quality checks:
```bash
mvn checkstyle:check
mvn pmd:check
```

##  Performance Features

- **LRU Caching**: Student data caching with expiration
- **Thread Pools**: Configurable concurrent processing
- **Background Monitoring**: Real-time performance metrics
- **Batch Processing**: Efficient bulk operations
- **Memory Management**: Automatic cleanup and optimization

##  Monitoring & Logging

- **Performance Monitor**: Tracks operation timing and resource usage
- **Audit Repository**: Logs all system operations with timestamps
- **Logger**: Centralized logging with configurable levels
- **Metrics Collection**: System performance statistics






## 🙏 Acknowledgments

- Built with modern Java practices
- Inspired by enterprise-grade management systems
- Focus on maintainability and extensibility


