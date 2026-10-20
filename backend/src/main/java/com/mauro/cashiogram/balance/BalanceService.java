package com.mauro.cashiogram.balance;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauro.cashiogram.account.entity.AccountEntity;
import com.mauro.cashiogram.account.repository.AccountRepository;
import com.mauro.cashiogram.balance.dto.BalanceResponseDTO;
import com.mauro.cashiogram.balance.repository.BalanceRepository;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@Service
@Transactional(readOnly = true)
public class BalanceService {

    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;

    public BalanceService(
            AccountRepository accountRepository,
            BalanceRepository balanceRepository) {

        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
    }

    public BalanceResponseDTO getBalance() {
        AccountEntity account = accountRepository
                .findFirstByOrderByCreatedAt()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account has not been initialized"));

        BigDecimal totalIncome = balanceRepository.sumAmountByTransactionType(
                TransactionType.INCOME);

        BigDecimal totalExpenses = balanceRepository.sumAmountByTransactionType(
                TransactionType.EXPENSE);

        BigDecimal currentBalance = account.getInitialBalance()
                .add(totalIncome)
                .subtract(totalExpenses);

        return new BalanceResponseDTO(
                account.getInitialBalance(),
                totalIncome,
                totalExpenses,
                currentBalance);
    }
}