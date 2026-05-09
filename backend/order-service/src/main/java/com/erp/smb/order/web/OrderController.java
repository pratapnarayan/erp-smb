package com.erp.smb.order.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.order.domain.OrderItem;
import com.erp.smb.order.domain.SalesOrder;
import com.erp.smb.order.repo.OrderItemRepository;
import com.erp.smb.order.repo.SalesOrderRepository;
import com.erp.smb.order.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final SalesOrderRepository repo;
  private final OrderItemRepository itemRepo;
  private final OrderService orderService;

  public OrderController(SalesOrderRepository repo, OrderItemRepository itemRepo, OrderService orderService) {
    this.repo = repo;
    this.itemRepo = itemRepo;
    this.orderService = orderService;
  }

  @GetMapping
  public ResponseEntity<PageResponse<SalesOrder>> list(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size
  ) {
    var p = repo.findAll(PageRequest.of(page, size));
    return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<SalesOrder> get(@PathVariable Long id) {
    return repo.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/items")
  public ResponseEntity<List<OrderItem>> items(@PathVariable Long id) {
    if (!repo.existsById(id)) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(itemRepo.findByOrderIdOrderById(id));
  }

  @PostMapping
  public SalesOrder create(@RequestBody SalesOrder so, @RequestHeader("X-Tenant-Id") String tenantId) {
    return orderService.createOrder(so, tenantId);
  }
}
