package com.erp.smb.product.web;

import com.erp.smb.common.dto.ImportResponse;
import com.erp.smb.product.service.OpeningStockImportService;
import com.erp.smb.product.service.ProductImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/products/import")
@Tag(name = "Product Import", description = "Bulk product and opening stock import")
@SecurityRequirement(name = "bearer-jwt")
public class ProductImportController {

  private final ProductImportService productImportService;
  private final OpeningStockImportService openingStockImportService;

  public ProductImportController(ProductImportService productImportService, 
                                  OpeningStockImportService openingStockImportService) {
    this.productImportService = productImportService;
    this.openingStockImportService = openingStockImportService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  @Operation(summary = "Import products from CSV/Excel", description = "Bulk import product data. Only ADMIN and OWNER roles allowed.")
  public ResponseEntity<ImportResponse> importProducts(@RequestParam("file") MultipartFile file) {
    try {
      ImportResponse response = productImportService.importFromFile(file);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Error processing file: " + e.getMessage()));
    }
  }

  @PostMapping(value = "/opening-stock", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  @Operation(summary = "Import opening stock from CSV/Excel", description = "Bulk import opening stock data. Only ADMIN and OWNER roles allowed.")
  public ResponseEntity<ImportResponse> importOpeningStock(@RequestParam("file") MultipartFile file) {
    try {
      ImportResponse response = openingStockImportService.importFromFile(file);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Error processing file: " + e.getMessage()));
    }
  }

  @GetMapping("/template")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  @Operation(summary = "Download product import template", description = "Get CSV template for product import")
  public ResponseEntity<String> downloadProductTemplate() {
    String template = productImportService.generateTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", "products_import_template.csv");
    return ResponseEntity.ok().headers(headers).body(template);
  }

  @GetMapping("/opening-stock/template")
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  @Operation(summary = "Download opening stock import template", description = "Get CSV template for opening stock import")
  public ResponseEntity<String> downloadOpeningStockTemplate() {
    String template = openingStockImportService.generateTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", "opening_stock_import_template.csv");
    return ResponseEntity.ok().headers(headers).body(template);
  }

  private ImportResponse createErrorResponse(String message) {
    ImportResponse response = new ImportResponse();
    response.setTotalRows(0);
    response.setSuccessCount(0);
    response.setFailedCount(0);
    response.getErrors().add(new com.erp.smb.common.dto.ImportError(0, "file", message));
    return response;
  }
}
