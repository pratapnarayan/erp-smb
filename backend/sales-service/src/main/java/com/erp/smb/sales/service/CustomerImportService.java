package com.erp.smb.sales.service;

import com.erp.smb.common.imports.AbstractImportService;
import com.erp.smb.common.imports.FileParser;
import com.erp.smb.common.imports.ImportValidationException;
import com.erp.smb.sales.domain.Customer;
import com.erp.smb.sales.repo.CustomerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CustomerImportService extends AbstractImportService<Customer> {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
  private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s()]+$");

  private final CustomerRepository customerRepository;
  private final CustomerService customerService;

  public CustomerImportService(List<FileParser> fileParsers, CustomerRepository customerRepository, 
                               CustomerService customerService) {
    super(fileParsers);
    this.customerRepository = customerRepository;
    this.customerService = customerService;
  }

  @Override
  protected Customer validateAndCreateEntity(String[] row, int rowNumber) throws ImportValidationException {
    // Skip empty rows
    if (isEmptyRow(row))
      return null;

    Customer customer = new Customer();

    // customer_name (required)
    String customerName = getColumnValue(row, 0);
    if (customerName == null || customerName.isEmpty()) {
      throw new ImportValidationException("customer_name", "Customer name is required");
    }
    customer.setCustomerName(customerName);

    // phone (optional, numeric)
    String phone = getColumnValue(row, 1);
    if (phone != null && !phone.isEmpty()) {
      if (!PHONE_PATTERN.matcher(phone).matches()) {
        throw new ImportValidationException("phone", "Phone must contain only numbers, +, -, spaces, or parentheses");
      }
      customer.setPhone(phone);
    }

    // email (optional, valid format)
    String email = getColumnValue(row, 2);
    if (email != null && !email.isEmpty()) {
      if (!EMAIL_PATTERN.matcher(email).matches()) {
        throw new ImportValidationException("email", "Invalid email format");
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
        throw new ImportValidationException("opening_balance", "Opening balance must be a valid number");
      }
    }

    // balance_type (optional, Credit/Debit)
    String balanceType = getColumnValue(row, 6);
    if (balanceType != null && !balanceType.isEmpty()) {
      if (!balanceType.equalsIgnoreCase("Credit") && !balanceType.equalsIgnoreCase("Debit")) {
        throw new ImportValidationException("balance_type", "Balance type must be 'Credit' or 'Debit'");
      }
      customer.setBalanceType(balanceType);
    }

    return customer;
  }

  @Override
  protected void saveEntity(Customer customer) {
    // Note: Import doesn't have tenantId context yet, so direct save
    // In production, extract tenantId from import context
    customerRepository.save(customer);
  }

  @Override
  public String generateTemplate() {
    return """
        customer_name,phone,email,address,gst_number,opening_balance,balance_type
        Acme Corp,+91-9876543210,contact@acme.com,"123 Business St, Mumbai",27AABCU9603R1ZX,50000.00,Credit
        Example Ltd,9988776655,info@example.com,"456 Trade Rd, Delhi",,25000.50,Debit
        """;
  }
}
