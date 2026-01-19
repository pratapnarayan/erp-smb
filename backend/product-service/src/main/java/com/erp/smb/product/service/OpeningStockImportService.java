package com.erp.smb.product.service;

import com.erp.smb.common.imports.AbstractImportService;
import com.erp.smb.common.imports.FileParser;
import com.erp.smb.common.imports.ImportValidationException;
import com.erp.smb.product.domain.Item;
import com.erp.smb.product.repo.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OpeningStockImportService extends AbstractImportService<Item> {

  private final ItemRepository itemRepository;

  public OpeningStockImportService(List<FileParser> fileParsers, ItemRepository itemRepository) {
    super(fileParsers);
    this.itemRepository = itemRepository;
  }

  @Override
  protected Item validateAndCreateEntity(String[] row, int rowNumber) throws ImportValidationException {
    // Skip empty rows
    if (isEmptyRow(row)) return null;

    // product_name (required, must exist)
    String productName = getColumnValue(row, 0);
    if (productName == null || productName.isEmpty()) {
      throw new ImportValidationException("product_name", "Product name is required");
    }

    // Find product by name or SKU
    Optional<Item> itemOpt = itemRepository.findByName(productName);
    if (itemOpt.isEmpty()) {
      itemOpt = itemRepository.findBySku(productName);
    }
    
    if (itemOpt.isEmpty()) {
      throw new ImportValidationException("product_name", "Product not found: " + productName);
    }

    Item item = itemOpt.get();

    // quantity (required, numeric, > 0)
    String quantityStr = getColumnValue(row, 1);
    if (quantityStr == null || quantityStr.isEmpty()) {
      throw new ImportValidationException("quantity", "Quantity is required");
    }

    int quantity;
    try {
      // Handle decimal values from Excel by parsing as double first
      double quantityDouble = Double.parseDouble(quantityStr);
      quantity = (int) quantityDouble;
      if (quantity <= 0) {
        throw new ImportValidationException("quantity", "Quantity must be greater than 0");
      }
    } catch (NumberFormatException e) {
      throw new ImportValidationException("quantity", "Quantity must be a valid number");
    }

    // warehouse (optional, not used currently)
    String warehouse = getColumnValue(row, 2);
    // Warehouse field is optional and not processed in current implementation

    // Update stock
    item.setStock(item.getStock() + quantity);

    return item;
  }

  @Override
  protected void saveEntity(Item item) {
    itemRepository.save(item);
  }

  @Override
  public String generateTemplate() {
    return """
product_name,quantity,warehouse
Laptop Computer,50,Main Warehouse
Office Chair,100,Main Warehouse
A4 Paper Ream,500,Stationery Store
""";
  }
}
