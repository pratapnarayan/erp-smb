package com.erp.smb.finance.repo;

import com.erp.smb.finance.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {}
