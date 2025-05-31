package com.example.eshop.service.impl;

import com.example.eshop.exception.InsufficientBalanceException;
import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.*;
import com.example.eshop.repository.TransactionRepository;
import com.example.eshop.repository.UserRepository;
import com.example.eshop.repository.WalletRepository;
import com.example.eshop.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class WalletServiceImpl implements WalletService {

  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;

  @Autowired
  public WalletServiceImpl(WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      UserRepository userRepository) {
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public Wallet recharge(User user, BigDecimal amount) {
    if (user instanceof Seller) {
      throw new IllegalArgumentException("商家不能直接充值钱包");
    }
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Recharge amount must be positive");
    }

    // First ensure we have a managed user entity with ID
    User managedUser = userRepository.findById(user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + user.getId()));

    Wallet wallet = walletRepository.findByUserWithLock(managedUser)
        .orElseGet(() -> {
          Wallet newWallet = new Wallet();
          newWallet.setUser(managedUser);
          newWallet.setBalance(BigDecimal.ZERO);
          return walletRepository.save(newWallet);
        });

    wallet.setBalance(wallet.getBalance().add(amount));
    walletRepository.save(wallet);

    Transaction transaction = new Transaction();
    transaction.setUser(managedUser);
    transaction.setType(TransactionType.RECHARGE);
    transaction.setAmount(amount);
    transaction.setBalanceAfter(wallet.getBalance());
    transaction.setDescription("钱包充值");

    transactionRepository.save(transaction);
    return wallet;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Transaction> getTransactionHistory(User user, Pageable pageable) {
    return transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Transaction> getRechargeHistory(User user, Pageable pageable) {
    return transactionRepository.findByUserAndTypeOrderByCreatedAtDesc(
        user, TransactionType.RECHARGE, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Transaction> getPaymentHistory(User user, Pageable pageable) {
    return transactionRepository.findByUserAndTypeOrderByCreatedAtDesc(
        user, TransactionType.PURCHASE, pageable);
  }

  @Override
  public Wallet getWalletByUser(User user) {
    return walletRepository.findByUser(user)
        .orElseGet(() -> createWallet(user));
  }

  @Override
  @Transactional
  public Wallet createWallet(User user) {
    Wallet wallet = new Wallet();
    wallet.setUser(user);
    wallet.setBalance(BigDecimal.ZERO);
    return walletRepository.save(wallet);
  }

  @Override
  @Transactional
  public Wallet withdraw(User user, BigDecimal amount) {
    if (user instanceof Seller) {
      throw new IllegalArgumentException("商家不能直接提现钱包");
    }
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("提现金额必须大于0");
    }

    Wallet wallet = walletRepository.findByUserWithLock(user)
        .orElseThrow(() -> new RuntimeException("钱包不存在"));

    if (wallet.getBalance().compareTo(amount) < 0) {
      throw new InsufficientBalanceException("余额不足");
    }

    BigDecimal newBalance = wallet.getBalance().subtract(amount);
    wallet.setBalance(newBalance);
    wallet = walletRepository.save(wallet);

    // 记录交易
    Transaction transaction = new Transaction();
    transaction.setUser(user);
    transaction.setType(TransactionType.WITHDRAWAL);
    transaction.setAmount(amount.negate()); // 提现金额为负数
    transaction.setBalanceAfter(newBalance);
    transaction.setDescription("钱包提现");
    transactionRepository.save(transaction);

    return wallet;
  }

  @Override
  public BigDecimal getMonthlyIncome(User user) {
    LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
    LocalDateTime endOfMonth = LocalDateTime.now().with(LocalTime.MAX);

    BigDecimal rechargeAmount = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
        user, TransactionType.RECHARGE, startOfMonth, endOfMonth);
    BigDecimal saleAmount = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
        user, TransactionType.SALE_INCOME, startOfMonth, endOfMonth);
    BigDecimal refundInAmount = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
        user, TransactionType.REFUND_IN, startOfMonth, endOfMonth);

    return (rechargeAmount != null ? rechargeAmount : BigDecimal.ZERO)
        .add(saleAmount != null ? saleAmount : BigDecimal.ZERO)
        .add(refundInAmount != null ? refundInAmount : BigDecimal.ZERO);
  }

  @Override
  public BigDecimal getMonthlyExpense(User user) {
    LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
    LocalDateTime endOfMonth = LocalDateTime.now().with(LocalTime.MAX);

    BigDecimal purchaseAmount = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
        user, TransactionType.PURCHASE, startOfMonth, endOfMonth);
    BigDecimal withdrawalAmount = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
        user, TransactionType.WITHDRAWAL, startOfMonth, endOfMonth);
    BigDecimal refundOutAmount = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
        user, TransactionType.REFUND_OUT, startOfMonth, endOfMonth);

    return (purchaseAmount != null ? purchaseAmount.abs() : BigDecimal.ZERO)
        .add(withdrawalAmount != null ? withdrawalAmount.abs() : BigDecimal.ZERO)
        .add(refundOutAmount != null ? refundOutAmount.abs() : BigDecimal.ZERO);
  }

  @Override
  public double getBalance(User user) {
    Wallet wallet = getWalletByUser(user);
    return wallet.getBalance().doubleValue();
  }

  @Override
  @Transactional
  public void updateBalance(User user, double amount) {
    Wallet wallet = walletRepository.findByUserWithLock(user)
        .orElseGet(() -> createWallet(user));

    BigDecimal newBalance = wallet.getBalance().add(BigDecimal.valueOf(amount));
    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new InsufficientBalanceException("Insufficient balance");
    }

    wallet.setBalance(newBalance);
    walletRepository.save(wallet);

    // Record transaction
    Transaction transaction = new Transaction();
    transaction.setUser(user);
    if (amount >= 0) {
      transaction.setType(TransactionType.RECHARGE);
      transaction.setAmount(BigDecimal.valueOf(amount)); // 正数
      transaction.setDescription("Balance update");
    } else {
      transaction.setType(TransactionType.PURCHASE);
      transaction.setAmount(BigDecimal.valueOf(amount)); // 负数
      transaction.setDescription("Balance deduction");
    }
    transaction.setBalanceAfter(newBalance);
    transactionRepository.save(transaction);
  }
}