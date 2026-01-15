package com.erp.smb.sales.web;

import com.erp.smb.common.dto.ImportResponse;
import com.erp.smb.sales.service.CustomerImportService;
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
@RequestMapping("/api/customers/import")
@Tag(name = "Customer Import", description = "Bulk customer data import")
@SecurityRequirement(name = "bearer-jwt")
public class CustomerImportController {

  private final CustomerImportService importService;

  public CustomerImportController(CustomerImportService importService) {
    this.importService = importService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
  @Operation(summary = "Import customers from CSV/Excel", description = "Bulk import customer data. Only ADMIN and OWNER roles allowed.")
  public ResponseEntity<ImportResponse> importCustomers(@RequestParam("file") MultipartFile file) {
    try {
      ImportResponse response = importService.importCustomers(file);
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
  @Operation(summary = "Download customer import template", description = "Get CSV template for customer import")
  public ResponseEntity<String> downloadTemplate() {
    String template = importService.generateTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("text/csv"));
    headers.setContentDispositionFormData("attachment", "customers_import_template.csv");
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
