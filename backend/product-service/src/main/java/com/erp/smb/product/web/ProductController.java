package com.erp.smb.product.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.product.domain.Item;
import com.erp.smb.product.repo.ItemRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

 import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ItemRepository repo;

    public ProductController(ItemRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Item>> list(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "20") int size) {
        var p = repo.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()));
    }

    @PostMapping
    public Item create(@RequestBody @Valid Item item) {
        return repo.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable(name = "id") long id, @RequestBody Map<String, Object> body) {
        var item = repo.findById(id).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();

        if (body != null) {
            if (body.containsKey("status")) {
                Object v = body.get("status");
                if (v != null) item.setStatus(String.valueOf(v));
            }
            if (body.containsKey("stock")) {
                item.setStock(toInt(body.get("stock")));
            }
            if (body.containsKey("reorder")) {
                item.setReorder(toInt(body.get("reorder")));
            }
        }

        return ResponseEntity.ok(repo.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
        var item = repo.findById(id).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();
        if (!isInactive(item.getStatus()) || item.getStock() != 0) {
            return ResponseEntity.status(409).body(Map.of("error", "only_inactive_products_with_zero_stock_can_be_deleted"));
        }
        repo.delete(item);
        return ResponseEntity.noContent().build();
    }

    private boolean isInactive(String status) {
        if (status == null) return false;
        return status.trim().equalsIgnoreCase("INACTIVE");
    }

    private int toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception ex) {
            return 0;
        }
    }
}
