package com.XYZBank.BankSys.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionModel {

    private String transactionId;

    private String transactionType;

    private BigDecimal amount;

    private String accountNumber;

    private String accountStatus;

}
