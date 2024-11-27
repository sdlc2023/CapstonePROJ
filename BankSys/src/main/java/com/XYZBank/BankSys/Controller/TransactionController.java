package com.XYZBank.BankSys.Controller;

import com.XYZBank.BankSys.Entity.TransactionEntity;
import com.XYZBank.BankSys.Service.BankStatement;
import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
@AllArgsConstructor
@Tag(name = "Transaction-related APIs")
public class TransactionController {

    private BankStatement bankStatement;

    @Operation(
            summary = "Generating Bank Statement",
            description = "Generating bank statement of a user and sending over email"
    )
    @ApiResponse(
            responseCode = "240",
            description = "HTTP Status 240 ACCOUNT STATEMENT SENT"
    )
    @GetMapping("/bankStatement")
    public List<TransactionEntity> generateBankStatement(@RequestParam String accountNumber,
                                                         @RequestParam String startDate,
                                                         @RequestParam String endDate) throws DocumentException, FileNotFoundException {
        return bankStatement.generateStatement(accountNumber, startDate, endDate);
    }

}
