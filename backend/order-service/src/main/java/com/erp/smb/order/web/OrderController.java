package com.erp.smb.order.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.order.domain.SalesOrder;
import com.erp.smb.order.repo.SalesOrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final SalesOrderRepository repo;
  public OrderController(SalesOrderRepository repo){this.repo=repo;}
  @GetMapping
  public ResponseEntity<PageResponse<SalesOrder>> list(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size
  ){
    var p = repo.findAll(PageRequest.of(page,size));
    return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
  }
  @PostMapping
  public SalesOrder create(@RequestBody SalesOrder so){ return repo.save(so);} 
}
