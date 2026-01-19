package com.erp.smb.product.service;

import com.erp.smb.common.imports.AbstractImportService;
import com.erp.smb.common.imports.FileParser;
import com.erp.smb.common.imports.ImportValidationException;
import com.erp.smb.product.domain.Item;
import com.erp.smb.product.repo.ItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductImportService extends AbstractImportService<Item> {

  private final ItemRepository itemRepository;

  public ProductImportService(List<FileParser> fileParsers, ItemRepository itemRepository) {
    super(fileParsers);
    this.itemRepository = itemRepository;
  }

  @Override
  protected Item validateAndCreateEntity(String[] row, int rowNumber) throws ImportValidationException {
    // Skip empty rows
    if (isEmptyRow(row)) return null;

    Item item = new Item();

    // product_name (required)
    String productName = getColumnValue(row, 0);
    if (productName == null || productName.isEmpty()) {
      throw new ImportValidationException("product_name", "Product name is required");
    }
    item.setName(productName);

    // sku (optional, but generate if empty)
    String sku = getColumnValue(row, 1);
    if (sku == null || sku.isEmpty()) {
      sku = "SKU-" + System.currentTimeMillis() + "-" + rowNumber;
    }
    item.setSku(sku);

    // category (optional)
    String category = getColumnValue(row, 2);
    if (category != null && !category.isEmpty()) {
      item.setCategory(category);
    }

    // unit (optional)
    String unit = getColumnValue(row, 3);
    if (unit != null && !unit.isEmpty()) {
      item.setUnit(unit);
    }

    // cost_price (optional, numeric)
    String costPrice = getColumnValue(row, 4);
    if (costPrice != null && !costPrice.isEmpty()) {
      try {
        BigDecimal cost = new BigDecimal(costPrice);
        if (cost.compareTo(BigDecimal.ZERO) < 0) {
          throw new ImportValidationException("cost_price", "Cost price must be non-negative");
        }
        item.setCostPrice(cost);
      } catch (NumberFormatException e) {
        throw new ImportValidationException("cost_price", "Cost price must be a valid number");
      }
    }

    // selling_price (optional, numeric)
    String sellingPrice = getColumnValue(row, 5);
    if (sellingPrice != null && !sellingPrice.isEmpty()) {
      try {
        BigDecimal selling = new BigDecimal(sellingPrice);
        if (selling.compareTo(BigDecimal.ZERO) < 0) {
          throw new ImportValidationException("selling_price", "Selling price must be non-negative");
        }
        item.setSellingPrice(selling);
      } catch (NumberFormatException e) {
        throw new ImportValidationException("selling_price", "Selling price must be a valid number");
      }
    }

    // gst_rate (optional, 0-28)
    String gstRate = getColumnValue(row, 6);
    if (gstRate != null && !gstRate.isEmpty()) {
      try {
        BigDecimal gst = new BigDecimal(gstRate);
        if (gst.compareTo(BigDecimal.ZERO) < 0 || gst.compareTo(new BigDecimal("28")) > 0) {
          throw new ImportValidationException("gst_rate", "GST rate must be between 0 and 28");
        }
        item.setGstRate(gst);
      } catch (NumberFormatException e) {
        throw new ImportValidationException("gst_rate", "GST rate must be a valid number");
      }
    }

    // Set default values
    item.setStock(0);
    item.setReorder(0);
    item.setStatus("Active");

    return item;
  }

  @Override
  protected void saveEntity(Item item) {
    itemRepository.save(item);
  }

  @Override
  public String generateTemplate() {
    return """
product_name,sku,category,unit,cost_price,selling_price,gst_rate
Laptop Computer,LAP-001,Electronics,Unit,45000.00,55000.00,18
Office Chair,CHR-002,Furniture,Unit,3500.00,4500.00,12
A4 Paper Ream,PAP-003,Stationery,Ream,250.00,300.00,5
""";
  }
}
