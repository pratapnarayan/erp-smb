# ERP SMB Platform

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://github.com/pratapnarayan/erp-smb/actions/workflows/build.yml/badge.svg)](https://github.com/pratapnarayan/erp-smb/actions)

A comprehensive Enterprise Resource Planning (ERP) solution designed for Small and Medium Businesses (SMBs) to manage their operations efficiently.

## Features

- **Multi-tenant Architecture**: Support for multiple businesses with isolated data
- **Comprehensive Modules**:
  - Inventory Management
  - Sales & Order Processing
  - Customer Relationship Management (CRM)
  - Human Resource Management (HRM)
  - Financial Management
  - Reporting & Analytics
- **Data Import/Export**: Bulk import/export of business data
- **Role-based Access Control**: Fine-grained permissions system
- **Responsive UI**: Modern, mobile-friendly interface

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 21 JDK
- Node.js 18+
- Maven 3.9+

### Running with Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/pratapnarayan/erp-smb.git
cd erp-smb

# Start all services
docker-compose up -d

# Access the application
open http://localhost:3000
```

### Local Development

1. **Start backend services:**
   ```bash
   docker-compose up -d postgres redis
   cd backend
   ./mvnw spring-boot:run -pl discovery-service
   # In separate terminals, run other services
   ```

2. **Start frontend:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Recent Updates

### Search improvements
- **Exact code/identifier search**: Code-like queries such as `SO-1005` or `SKU-1005` are treated as exact matches to avoid noisy results from tokenization and fuzzy matching.
- **Search bar UX**: After entering a query and navigating to the results page, the global search input is reset so it’s ready for the next search.

### Security / roles
- **Search indexing & reindexing endpoints** now require the `ADMIN` role (previously referenced a non-existent `SYSTEM` role).

## Documentation

For detailed documentation, please refer to:

- [Comprehensive Project Analysis](COMPREHENSIVE_PROJECT_ANALYSIS.md)
- [API Documentation](http://localhost:8080/swagger-ui.html) (after starting services)
- [Deployment Guide](COMPREHENSIVE_PROJECT_ANALYSIS.md#deployment-guide)
- [Configuration Guide](COMPREHENSIVE_PROJECT_ANALYSIS.md#configuration-guide)

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please [open an issue](https://github.com/pratapnarayan/erp-smb/issues) or contact [Your Support Email].
