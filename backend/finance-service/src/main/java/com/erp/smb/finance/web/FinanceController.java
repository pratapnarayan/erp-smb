package com.erp.smb.finance.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.finance.domain.Transaction;
import com.erp.smb.finance.repo.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {
  private final TransactionRepository repo; public FinanceController(TransactionRepository repo){this.repo=repo;}
  @GetMapping
  public ResponseEntity<PageResponse<Transaction>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){ var p=repo.findAll(PageRequest.of(page,size)); return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages())); }
  @PostMapping
  public Transaction create(@RequestBody Transaction t){ return repo.save(t);} 
}
