# Data Import Feature - Implementation Summary

## Overview
This feature enables SMB admins to bulk import existing business data (Customers, Products, Opening Stock) from Excel/CSV files during onboarding, accessible via **Admin → Data Import**.

## Architecture

### Backend Services

#### 1. **Sales Service** - Customer Import
- **Entity**: `Customer` (sales.customers table)
- **Endpoints**:
  - `POST /api/customers/import` - Import customers from CSV/Excel
  - `GET /api/customers/import/template` - Download CSV template
- **Fields**: customer_name*, phone, email, address, gst_number, opening_balance, balance_type
- **Validations**:
  - Name required
  - Email format validation
  - Phone numeric validation
  - Balance numeric validation
  - Balance type: Credit/Debit

#### 2. **Product Service** - Product & Opening Stock Import
- **Extended Entity**: `Item` (added category, unit, cost_price, selling_price, gst_rate)
- **Endpoints**:
  - `POST /api/products/import` - Import products
  - `GET /api/products/import/template` - Download product template
  - `POST /api/products/import/opening-stock` - Import opening stock
  - `GET /api/products/import/opening-stock/template` - Download opening stock template
- **Product Fields**: product_name*, sku, category, unit, cost_price, selling_price, gst_rate
- **Opening Stock Fields**: product_name* (must exist), quantity* (>0), warehouse
- **Validations**:
  - Product name required
  - Prices numeric
  - GST rate 0-28%
  - Product must exist for stock import
  - Quantity must be positive

### Common Components
- **DTOs** (common-lib):
  - `ImportResponse` - Contains totalRows, successCount, failedCount, errors[]
  - `ImportError` - Contains rowNumber, field, reason
- **Dependencies**: OpenCSV (5.9), Apache POI (5.2.5) for CSV/Excel parsing

### Security
- **Role-based access**: Only `ROLE_ADMIN` and `ROLE_OWNER` can access import features
- **Method-level security**: `@PreAuthorize` annotations on all import endpoints
- **File limits**: 5MB max file size, 5000 max rows
- **Input sanitization**: Row-by-row validation, SQL injection protection

### Database Migrations
- `V5__add_customers_table.sql` (sales-service)
- `V5__add_product_import_fields.sql` (product-service)

### Gateway Configuration
- Added `/api/customers/**` route to sales-service in `gateway-service/routes/sales.yml`

## Frontend Implementation

### UI Location
**Admin Page → Data Import Tab**
- Only visible to ADMIN/OWNER roles
- Tab-based interface: Customers | Products | Opening Stock

### Features
1. **File Upload** - CSV/Excel support (.csv, .xlsx, .xls)
2. **Template Download** - One-click template download for each import type
3. **Real-time Validation** - File size and format validation before upload
4. **Progress Indicator** - Loading state during upload/processing
5. **Detailed Results**:
   - Summary: Total, Success, Failed counts
   - Row-by-row error display with field and reason
   - Color-coded success/failure indicators

### Component Structure
- `DataImport.jsx` - Main component with tab navigation
- `ImportCard` - Reusable card for each import type
- Integrated with existing `FrostedCard`, `Badge` components

## Import Flow

### 1. Partial Success Mode
- Valid rows are saved
- Invalid rows generate errors
- Import continues even with failures
- Returns detailed error report

### 2. Error Handling
- **File-level errors**: Size, format, empty file
- **Row-level errors**: Validation failures with row number and field
- **No silent failures**: All errors reported to user

### 3. Processing
- In-memory processing (no file persistence)
- Transaction-based (per row, not all-or-nothing)
- Automatic SKU generation if missing

## CSV Template Examples

### Customers Template
```csv
customer_name,phone,email,address,gst_number,opening_balance,balance_type
Acme Corp,+91-9876543210,contact@acme.com,"123 Business St, Mumbai",27AABCU9603R1ZX,50000.00,Credit
```

### Products Template
```csv
product_name,sku,category,unit,cost_price,selling_price,gst_rate
Laptop Computer,LAP-001,Electronics,Unit,45000.00,55000.00,18
```

### Opening Stock Template
```csv
product_name,quantity,warehouse
Laptop Computer,50,Main Warehouse
```

## Testing

### Manual Testing
1. Start all backend services (discovery, gateway, auth, sales, product)
2. Start frontend (`npm run dev`)
3. Login as admin/admin
4. Navigate to Admin → Data Import
5. Test each import type:
   - Download template
   - Modify with test data
   - Upload and verify results

### Automated Testing
Run test scripts:
```bash
# Linux/Mac
bash tmp_rovodev_test_data_import.sh

# Windows
powershell tmp_rovodev_test_data_import.ps1
```

## API Examples

### Customer Import
```bash
curl -X POST http://localhost:8080/api/customers/import \
  -H "Authorization: Bearer <token>" \
  -F "file=@customers.csv"
```

**Response:**
```json
{
  "totalRows": 10,
  "successCount": 8,
  "failedCount": 2,
  "errors": [
    {
      "rowNumber": 3,
      "field": "email",
      "reason": "Invalid email format"
    },
    {
      "rowNumber": 7,
      "field": "customer_name",
      "reason": "Customer name is required"
    }
  ]
}
```

### Product Import
```bash
curl -X POST http://localhost:8080/api/products/import \
  -H "Authorization: Bearer <token>" \
  -F "file=@products.xlsx"
```

### Opening Stock Import
```bash
curl -X POST http://localhost:8080/api/products/import/opening-stock \
  -H "Authorization: Bearer <token>" \
  -F "file=@opening_stock.csv"
```

## Files Modified/Created

### Backend
**New Files:**
- `backend/sales-service/src/main/java/com/erp/smb/sales/domain/Customer.java`
- `backend/sales-service/src/main/java/com/erp/smb/sales/repo/CustomerRepository.java`
- `backend/sales-service/src/main/java/com/erp/smb/sales/service/CustomerImportService.java`
- `backend/sales-service/src/main/java/com/erp/smb/sales/web/CustomerImportController.java`
- `backend/sales-service/src/main/resources/db/migration/V5__add_customers_table.sql`
- `backend/product-service/src/main/java/com/erp/smb/product/service/ProductImportService.java`
- `backend/product-service/src/main/java/com/erp/smb/product/service/OpeningStockImportService.java`
- `backend/product-service/src/main/java/com/erp/smb/product/web/ProductImportController.java`
- `backend/product-service/src/main/resources/db/migration/V5__add_product_import_fields.sql`
- `backend/common-lib/src/main/java/com/erp/smb/common/dto/ImportError.java`
- `backend/common-lib/src/main/java/com/erp/smb/common/dto/ImportResponse.java`

**Modified Files:**
- `backend/sales-service/pom.xml` (added OpenCSV, Apache POI)
- `backend/product-service/pom.xml` (added OpenCSV, Apache POI)
- `backend/product-service/src/main/java/com/erp/smb/product/domain/Item.java` (extended fields)
- `backend/product-service/src/main/java/com/erp/smb/product/repo/ItemRepository.java` (added queries)
- `backend/product-service/src/main/java/com/erp/smb/product/config/SecurityConfig.java` (@EnableMethodSecurity)
- `backend/sales-service/src/main/java/com/erp/smb/sales/config/SecurityConfig.java` (@EnableMethodSecurity)
- `backend/gateway-service/src/main/resources/routes/sales.yml` (added customers route)

### Frontend
**New Files:**
- `frontend/src/components/DataImport.jsx`

**Modified Files:**
- `frontend/src/pages/Admin.jsx` (added Data Import tab)

## Non-Goals (Not Implemented)
- ❌ Tally integration
- ❌ Google Drive sync
- ❌ AI parsing
- ❌ Auto-mapping UI
- ❌ Scheduled imports
- ❌ Email notifications

## Future Enhancements
- Export functionality (reverse operation)
- Import history tracking
- Scheduled/automated imports
- Data validation preview before import
- Bulk update support
- Multi-sheet Excel support
- Custom field mapping

## Notes
- All imports support partial success (save valid, report invalid)
- Files are processed in-memory (no persistence)
- Auto-generated SKU for products if not provided
- Opening stock increments existing stock (doesn't replace)
- Balance type defaults to Credit if not specified
