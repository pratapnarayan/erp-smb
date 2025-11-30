package com.erp.smb.product.repo;

import com.erp.smb.product.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {}
