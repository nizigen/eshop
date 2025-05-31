package com.example.eshop.service;

import com.example.eshop.model.Transaction;
import com.example.eshop.model.User;
import com.example.eshop.model.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface WalletService {

  Wallet getWalletByUser(User user);

  Wallet createWallet(User user);

  Wallet recharge(User user, BigDecimal amount);

  Wallet withdraw(User user, BigDecimal amount);

  BigDecimal getMonthlyIncome(User user);

  BigDecimal getMonthlyExpense(User user);

  Page<Transaction> getTransactionHistory(User user, Pageable pageable);

  Page<Transaction> getRechargeHistory(User user, Pageable pageable);

  Page<Transaction> getPaymentHistory(User user, Pageable pageable);

  /**
   * 获取用户钱包余额
   * 
   * @param user 用户
   * @return 钱包余额
   */
  double getBalance(User user);

  void updateBalance(User user, double amount);
}