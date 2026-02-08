package com.erp.smb.product.repo;

import com.erp.smb.product.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
  Optional<Item> findByName(String name);
  Optional<Item> findBySku(String sku);

  @Query(value = "SELECT COUNT(*) FROM products.items i WHERE i.status ILIKE 'ACTIVE'", nativeQuery = true)
  long countActive();

  @Query(value = "SELECT COALESCE(SUM(i.stock), 0) FROM products.items i", nativeQuery = true)
  long sumStock();
}

