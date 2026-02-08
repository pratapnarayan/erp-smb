package com.erp.smb.finance.repo;

import com.erp.smb.finance.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  /**
   * Sum of CREDIT transactions between [start, end) optionally filtered by account.
   */
  @Query(value = """
      SELECT COALESCE(SUM(t.amount), 0)
      FROM finance.transactions t
      WHERE t.tx_type = 'CREDIT'
        AND t.tx_date >= :start
        AND t.tx_date < :end
        AND (:account IS NULL OR t.account = :account)
      """, nativeQuery = true)
  BigDecimal sumCredits(@Param("start") LocalDate start,
                        @Param("end") LocalDate end,
                        @Param("account") String account);

  /**
   * Sum of CREDIT AR transactions before the given cutoff date.
   */
  @Query(value = """
      SELECT COALESCE(SUM(t.amount), 0)
      FROM finance.transactions t
      WHERE t.tx_type = 'CREDIT'
        AND t.account = 'AR'
        AND t.tx_date < :cutoff
      """, nativeQuery = true)
  BigDecimal sumArCreditsBefore(@Param("cutoff") LocalDate cutoff);

  interface AccountBalanceRow {
    String getAccount();
    BigDecimal getBalance();
  }

  /**
   * Sum of all transaction amounts grouped by account.
   * Note: This is a pragmatic "balance" model based on the seed data and current schema.
   */
  @Query(value = """
      SELECT t.account AS account, COALESCE(SUM(t.amount), 0) AS balance
      FROM finance.transactions t
      GROUP BY t.account
      """, nativeQuery = true)
  List<AccountBalanceRow> sumAmountByAccount();
}

