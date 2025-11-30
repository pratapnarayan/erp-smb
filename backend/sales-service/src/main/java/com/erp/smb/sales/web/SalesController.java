package com.erp.smb.sales.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.sales.domain.Invoice;
import com.erp.smb.sales.repo.InvoiceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
  private final InvoiceRepository repo; public SalesController(InvoiceRepository repo){this.repo=repo;}
  @GetMapping
  public ResponseEntity<PageResponse<Invoice>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){ var p=repo.findAll(PageRequest.of(page,size)); return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages())); }
}
