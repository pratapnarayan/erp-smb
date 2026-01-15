package com.erp.smb.sales.service;

import com.erp.smb.common.dto.ImportError;
import com.erp.smb.common.dto.ImportResponse;
import com.erp.smb.sales.domain.Customer;
import com.erp.smb.sales.repo.CustomerRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CustomerImportService {

  private static final int MAX_ROWS = 5000;
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s()]+$");

  private final CustomerRepository customerRepository;

  public CustomerImportService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  public ImportResponse importCustomers(MultipartFile file) throws IOException {
    // Validate file size
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("File size exceeds 5MB limit");
    }

    String filename = file.getOriginalFilename();
    if (filename == null) {
      throw new IllegalArgumentException("Invalid file");
    }

    List<String[]> rows;
    if (filename.endsWith(".csv")) {
      rows = parseCsv(file);
    } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
      rows = parseExcel(file);
    } else {
      throw new IllegalArgumentException("Unsupported file format. Use CSV or Excel");
    }

    return processRows(rows);
  }

  private List<String[]> parseCsv(MultipartFile file) throws IOException {
    try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
      return reader.readAll();
    } catch (CsvException e) {
      throw new IOException("Error parsing CSV file", e);
    }
  }

  private List<String[]> parseExcel(MultipartFile file) throws IOException {
    List<String[]> rows = new ArrayList<>();
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        String[] rowData = new String[7]; // 7 columns: customer_name, phone, email, address, gst_number,
                                          // opening_balance, balance_type
        for (int i = 0; i < 7; i++) {
          Cell cell = row.getCell(i);
          rowData[i] = getCellValueAsString(cell);
        }
        rows.add(rowData);
      }
    }
    return rows;
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null)
      return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> String.valueOf(cell.getNumericCellValue());
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> cell.getCellFormula();
      default -> "";
    };
  }

  private ImportResponse processRows(List<String[]> rows) {
    ImportResponse response = new ImportResponse();

    if (rows.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    // Skip header row
    int totalRows = rows.size() - 1;
    if (totalRows > MAX_ROWS) {
      throw new IllegalArgumentException("File exceeds maximum " + MAX_ROWS + " rows");
    }

    response.setTotalRows(totalRows);
    int successCount = 0;
    int failedCount = 0;

    for (int i = 1; i < rows.size(); i++) {
      String[] row = rows.get(i);
      try {
        Customer customer = validateAndCreateCustomer(row, i);
        if (customer != null) {
          customerRepository.save(customer);
          successCount++;
        }
      } catch (ValidationException e) {
        response.addError(new ImportError(i, e.getField(), e.getMessage()));
        failedCount++;
      }
    }

    response.setSuccessCount(successCount);
    response.setFailedCount(failedCount);
    return response;
  }

  private Customer validateAndCreateCustomer(String[] row, int rowNumber) throws ValidationException {
    // Skip empty rows
    if (isEmptyRow(row))
      return null;

    Customer customer = new Customer();

    // customer_name (required)
    String customerName = getColumnValue(row, 0);
    if (customerName == null || customerName.isEmpty()) {
      throw new ValidationException("customer_name", "Customer name is required");
    }
    customer.setCustomerName(customerName);

    // phone (optional, numeric)
    String phone = getColumnValue(row, 1);
    if (phone != null && !phone.isEmpty()) {
      if (!PHONE_PATTERN.matcher(phone).matches()) {
        throw new ValidationException("phone", "Phone must contain only numbers, +, -, spaces, or parentheses");
      }
      customer.setPhone(phone);
    }

    // email (optional, valid format)
    String email = getColumnValue(row, 2);
    if (email != null && !email.isEmpty()) {
      if (!EMAIL_PATTERN.matcher(email).matches()) {
        throw new ValidationException("email", "Invalid email format");
      }
      customer.setEmail(email);
    }

    // address (optional)
    String address = getColumnValue(row, 3);
    if (address != null && !address.isEmpty()) {
      customer.setAddress(address);
    }

    // gst_number (optional)
    String gstNumber = getColumnValue(row, 4);
    if (gstNumber != null && !gstNumber.isEmpty()) {
      customer.setGstNumber(gstNumber);
    }

    // opening_balance (optional, numeric)
    String openingBalance = getColumnValue(row, 5);
    if (openingBalance != null && !openingBalance.isEmpty()) {
      try {
        customer.setOpeningBalance(new BigDecimal(openingBalance));
      } catch (NumberFormatException e) {
        throw new ValidationException("opening_balance", "Opening balance must be a valid number");
      }
    }

    // balance_type (optional, Credit/Debit)
    String balanceType = getColumnValue(row, 6);
    if (balanceType != null && !balanceType.isEmpty()) {
      if (!balanceType.equalsIgnoreCase("Credit") && !balanceType.equalsIgnoreCase("Debit")) {
        throw new ValidationException("balance_type", "Balance type must be 'Credit' or 'Debit'");
      }
      customer.setBalanceType(balanceType);
    }

    return customer;
  }

  private String getColumnValue(String[] row, int index) {
    if (index >= row.length)
      return null;
    String value = row[index];
    return value == null ? null : value.trim();
  }

  private boolean isEmptyRow(String[] row) {
    for (String cell : row) {
      if (cell != null && !cell.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public String generateTemplate() {
    return """
        customer_name,phone,email,address,gst_number,opening_balance,balance_type
        Acme Corp,+91-9876543210,contact@acme.com,"123 Business St, Mumbai",27AABCU9603R1ZX,50000.00,Credit
        Example Ltd,9988776655,info@example.com,"456 Trade Rd, Delhi",,25000.50,Debit
        """;
  }

  private static class ValidationException extends Exception {
    private final String field;

    public ValidationException(String field, String message) {
      super(message);
      this.field = field;
    }

    public String getField() {
      return field;
    }
  }
}
