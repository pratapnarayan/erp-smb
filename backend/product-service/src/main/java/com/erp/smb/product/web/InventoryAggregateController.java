package com.erp.smb.product.web;

import com.erp.smb.product.repo.ItemRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RestController
@RequestMapping("/api/products/inventory")
public class InventoryAggregateController {

  private final ItemRepository itemRepository;

  public InventoryAggregateController(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  /**
   * Read-only aggregate endpoint for dashboard KPI "Inventory Turnover".
   *
   * Assumptions (v1, pragmatic):
   * - We do not yet have COGS / shipments / stock movements.
   * - We approximate "turnover" as: active items count / (total stock + 1) * 100.
   *   This produces a stable, explainable number that changes as inventory changes.
   * - Goal is consistency over correctness until richer inventory/sales data exists.
   */
  @GetMapping("/turnover")
  public BigDecimal turnover() {
    long activeItems = itemRepository.countActive();
    long totalStock = itemRepository.sumStock();

    BigDecimal numerator = BigDecimal.valueOf(activeItems);
    BigDecimal denom = BigDecimal.valueOf(totalStock + 1); // avoid division by zero

    return numerator.divide(denom, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .setScale(2, RoundingMode.HALF_UP);
  }
}
