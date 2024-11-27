package com.XYZBank.BankSys.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

import com.XYZBank.BankSys.Entity.TransactionEntity;
import com.XYZBank.BankSys.Entity.UserEntity;
import com.XYZBank.BankSys.Model.EmailDetails;
import com.XYZBank.BankSys.Repository.TransactionRepository;
import com.XYZBank.BankSys.Repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class BankStatement {

    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private EmailService emailService;

    private static final String FILE = "D:\\MyStatement.pdf";

    public List<TransactionEntity> generateStatement(String accountNumber, String startDate, String endDate) throws FileNotFoundException, DocumentException {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<TransactionEntity> transactionEntityList = transactionRepository.findAll().stream().filter(transactionEntity -> transactionEntity.getAccountNumber().equals(accountNumber))
                .filter(transactionEntity -> transactionEntity.getCreatedAt().isEqual(start))
                .filter(transactionEntity -> transactionEntity.getCreatedAt().isEqual(end))
                .toList();

        UserEntity user = userRepository.findByAccountNumber(accountNumber);
        String customerName = user.getUserFirstName() + " "+ user.getUserLastName();


        com.itextpdf.text.Rectangle statementSize = new Rectangle(PageSize.A4);
        Document document = new Document(statementSize);
        log.info("Setting Size of doc");
        OutputStream outputStream = new FileOutputStream(FILE);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        PdfPTable bankInfoTable = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase("XYZ Bank"));
        bankName.setBorder(0);
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(20f);

        PdfPCell bankAddress = new PdfPCell(new Phrase("PTT Bangalore"));
        bankAddress.setBorder(0);
        bankInfoTable.addCell(bankName);
        bankInfoTable.addCell(bankAddress);

        PdfPTable statementInfo = new PdfPTable(2);
        PdfPCell beginDate = new PdfPCell(new Phrase("Start Date: "+startDate));
        beginDate.setBorder(0);
        PdfPCell statement = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT"));
        statement.setBorder(0);
        PdfPCell stopDate = new PdfPCell(new Phrase("End Date: "+endDate));
        stopDate.setBorder(0);
        PdfPCell userName = new PdfPCell(new Phrase("Customer Name :"+customerName));
        userName.setBorder(0);
        PdfPCell space = new PdfPCell();
        space.setBorder(0);
        PdfPCell customerAddress = new PdfPCell(new Phrase("Address: "+user.getUserAddress()));
        customerAddress.setBorder(0);

        PdfPTable transactionTable = new PdfPTable(4);
        PdfPCell date = new PdfPCell(new Phrase("DATE"));
        date.setBorder(0);
        date.setBackgroundColor(BaseColor.BLUE);
        PdfPCell transactionType = new PdfPCell(new Phrase("TYPE"));
        transactionType.setBorder(0);
        transactionType.setBackgroundColor(BaseColor.BLUE);
        PdfPCell amount = new PdfPCell(new Phrase("AMOUNT"));
        amount.setBackgroundColor(BaseColor.BLUE);
        amount.setBorder(0);
        PdfPCell status = new PdfPCell(new Phrase("STATUS"));
        status.setBackgroundColor(BaseColor.BLUE);
        status.setBorder(0);

        transactionTable.addCell(date);
        transactionTable.addCell(transactionType);
        transactionTable.addCell(amount);
        transactionTable.addCell(status);

        transactionEntityList.forEach(transactionEntity -> {
            transactionTable.addCell(new Phrase(transactionEntity.getCreatedAt().toString()));
            transactionTable.addCell(new Phrase(transactionEntity.getTransactionType()));
            transactionTable.addCell(new Phrase(transactionEntity.getAmount().toString()));
            transactionTable.addCell(new Phrase(transactionEntity.getAccountStatus()));
        });

        statementInfo.addCell(beginDate);
        statementInfo.addCell(statement);
        statementInfo.addCell(stopDate);
        statementInfo.addCell(userName);
        statementInfo.addCell(space);
        statementInfo.addCell(customerAddress);

        document.add(bankInfoTable);
        document.add(statementInfo);
        document.add(transactionTable);

        document.close();

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getUserEmail())
                .subject("STATEMENT OF ACCOUNT")
                .messageBody("Kindly find the attached account statement!")
                .attachment(FILE)
                .build();

        emailService.sendEmailWithAttachment(emailDetails);

        return transactionEntityList;
    }





}
