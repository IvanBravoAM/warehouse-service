inventory-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── inventory/
│   │   │               ├── controller/   # REST Controllers
│   │   │               ├── model/        # Entity Classes
│   │   │               ├── repository/   # JPA Repositories
│   │   │               ├── service/      # Business Logic
│   │   │               └── config/       # Configuration Classes
│   │   ├── resources/
│   │       ├── application.yml           # Application Configuration
│   │       ├── schema.sql                # Database Schema Initialization (optional)
│   │       ├── data.sql                  # Sample Data Initialization (optional)
│   ├── test/                             # Unit and Integration Tests
├── Dockerfile                             # Containerization for the service
├── pom.xml                                # Maven Dependencies and Configuration
├── README.md                              # Service Documentation
└── .gitignore                             # Ignored Files and Folders
