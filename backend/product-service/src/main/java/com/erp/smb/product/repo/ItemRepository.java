package com.erp.smb.product.repo;

import com.erp.smb.product.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
  Optional<Item> findByName(String name);
  Optional<Item> findBySku(String sku);
}
