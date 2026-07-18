package com.erp.smb.sales.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.sales.domain.Invoice;
import com.erp.smb.sales.repo.InvoiceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final InvoiceRepository repo;

    public SalesController(InvoiceRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Invoice>> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        var p = repo.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages()));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Invoice invoice) {
        if (invoice.getInvoiceNo() == null || invoice.getInvoiceNo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invoice_no_required"));
        }
        if (invoice.getCustomer() == null || invoice.getCustomer().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "customer_required"));
        }
        if (invoice.getAmount() == null || invoice.getAmount().signum() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "amount_must_be_positive"));
        }
        if (invoice.getDue() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "due_date_required"));
        }
        if (invoice.getStatus() == null || invoice.getStatus().isBlank()) {
            invoice.setStatus("OPEN");
        }
        if (repo.existsByInvoiceNo(invoice.getInvoiceNo())) {
            return ResponseEntity.badRequest().body(Map.of("error", "invoice_no_already_exists"));
        }
        return ResponseEntity.ok(repo.save(invoice));
    }
}
