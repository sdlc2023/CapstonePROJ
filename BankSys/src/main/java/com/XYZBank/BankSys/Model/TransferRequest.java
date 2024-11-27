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
public class TransferRequest {

    private String senderAccountNumber;
    private String recipientAccountNumber;
    private BigDecimal amount;

}