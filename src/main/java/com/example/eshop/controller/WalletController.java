package com.example.eshop.controller;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.model.Transaction;
import com.example.eshop.model.TransactionType;
import com.example.eshop.model.User;
import com.example.eshop.model.Wallet;
import com.example.eshop.service.UserService;
import com.example.eshop.service.WalletService;
import com.example.eshop.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.security.Principal;

@Controller
@RequestMapping("/profile/wallet")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WalletController {

  private final WalletService walletService;
  private final UserService userService;
  private final TransactionRepository transactionRepository;

  @GetMapping
  public String showWalletPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    model.addAttribute("user", user);

    // 获取钱包信息
    Wallet wallet = walletService.getWalletByUser(user);
    model.addAttribute("wallet", wallet);

    // 获取交易记录
    List<Transaction> transactions = transactionRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(0, 10))
        .getContent();
    model.addAttribute("transactions", transactions);

    // 计算本月收支
    LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

    // 本月收入
    BigDecimal monthlyIncome = transactionRepository.findByUserAndTypeAndCreatedAtBetween(
        user, TransactionType.SALE_INCOME, startOfMonth, endOfMonth)
        .stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    model.addAttribute("monthlyIncome", monthlyIncome);

    // 本月支出
    BigDecimal monthlyExpense = transactionRepository.findByUserAndTypeAndCreatedAtBetween(
        user, TransactionType.EXPENSE, startOfMonth, endOfMonth)
        .stream()
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    model.addAttribute("monthlyExpense", monthlyExpense);

    return "user/profile#wallet";
  }

  @PostMapping("/recharge")
  @ResponseBody
  public Map<String, Object> recharge(@RequestBody Map<String, Object> payload, Principal principal) {
    Map<String, Object> result = new HashMap<>();
    try {
      BigDecimal amount = new BigDecimal(payload.get("amount").toString());
      User user = userService.findByEmail(principal.getName())
          .orElseThrow(() -> new RuntimeException("用户不存在"));
      walletService.recharge(user, amount);
      result.put("success", true);
    } catch (Exception e) {
      result.put("success", false);
      result.put("message", e.getMessage());
    }
    return result;
  }

  @PostMapping("/withdraw")
  @ResponseBody
  public Map<String, Object> withdraw(@RequestBody Map<String, Object> payload, Principal principal) {
    Map<String, Object> result = new HashMap<>();
    try {
      BigDecimal amount = new BigDecimal(payload.get("amount").toString());
      User user = userService.findByEmail(principal.getName())
          .orElseThrow(() -> new RuntimeException("用户不存在"));
      walletService.withdraw(user, amount);
      result.put("success", true);
    } catch (Exception e) {
      result.put("success", false);
      result.put("message", e.getMessage());
    }
    return result;
  }

  @GetMapping("/recharge-history")
  public String getRechargeHistory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Model model) {

    Page<Transaction> transactions = walletService.getRechargeHistory(
        userService.getCurrentUser(), PageRequest.of(page, size));

    model.addAttribute("transactions", transactions);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", transactions.getTotalPages());

    return "user/wallet-recharge-history";
  }

  @GetMapping("/payment-history")
  public String getPaymentHistory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Model model) {

    Page<Transaction> transactions = walletService.getPaymentHistory(
        userService.getCurrentUser(), PageRequest.of(page, size));

    model.addAttribute("transactions", transactions);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", transactions.getTotalPages());

    return "user/wallet-payment-history";
  }
}