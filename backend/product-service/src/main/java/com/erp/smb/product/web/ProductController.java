package com.erp.smb.product.web;

import com.erp.smb.common.dto.PageResponse;
import com.erp.smb.product.domain.Item;
import com.erp.smb.product.repo.ItemRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
