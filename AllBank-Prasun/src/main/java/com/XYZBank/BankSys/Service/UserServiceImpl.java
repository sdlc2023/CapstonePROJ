package com.XYZBank.BankSys.Service;

import com.XYZBank.BankSys.Config.JwtTokenProvider;
import com.XYZBank.BankSys.Entity.Role;
import com.XYZBank.BankSys.Entity.UserEntity;
import com.XYZBank.BankSys.Model.*;
import com.XYZBank.BankSys.Repository.UserRepository;
import com.XYZBank.BankSys.Utils.Accountutils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private final EmailService emailService;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TransactionService transactionService;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(EmailService emailService, UserRepository userRepository, TransactionService transactionService, PasswordEncoder passwordEncoder) {

        this.emailService = emailService;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public BankResponse createAccount(UserModel userModel) {
        //Creating an account and saving user details in Spring Trial DB

        //Check if User Already Exists - In UserRepository

        if (userRepository.existsByUserEmail(userModel.getUserEmail())) {
            BankResponse bankResponse = BankResponse.builder()
                    .reponseCode(Accountutils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(Accountutils.ACCOUNT_EXISTS_MESSAGE)
                    .accountModel(null)
                    .build();
            return bankResponse;
        }

        UserEntity newUser = UserEntity.builder()
                .userFirstName(userModel.getUserFirstName())
                .userLastName(userModel.getUserLastName())
                .userAddress(userModel.getUserAddress())
                .userGender(userModel.getUserGender())
                .userEmail(userModel.getUserEmail())
                .userPassword(passwordEncoder.encode(userModel.getUserPassword()))
                .userMobileNumber(userModel.getUserMobileNumber())
                .accountNumber(Accountutils.generateAccountNumber())
                .accountBalance(userModel.getAccountBalance())
                .userStatus("ACTIVE")
                .role(Role.valueOf("ROLE_ADMIN"))
                .build();

        UserEntity savedUser = userRepository.save(newUser);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getUserEmail())
                .subject("Account Created")
                .messageBody("Congratulations for opening your account with our bank\nYour account details are: \n" +
                        "Account Name: " + savedUser.getUserFirstName() + " " + savedUser.getUserLastName() +
                        "\nAccount Number: " + savedUser.getAccountNumber() +
                        "\nMobile Number: " + savedUser.getUserMobileNumber() +
                        "\nEmail ID: " + savedUser.getUserEmail())
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .reponseCode(Accountutils.ACCOUNT_CREATED_CODE)
                .responseMessage(Accountutils.ACCOUNT_CREATED_MESSAGE)
                .accountModel(AccountModel.builder()
                        .accountName(savedUser.getUserFirstName() + " " + savedUser.getUserLastName())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountBalance(savedUser.getAccountBalance())
                        .build())
                .build();

    }

    @Override
    public BankResponse login(LoginModel loginModel) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginModel.getEmail(), loginModel.getPassword())
        );
        if(authentication.isAuthenticated()) {

            EmailDetails loginAlert = EmailDetails.builder()
                    .subject("Log-In Alert")
                    .recipient(loginModel.getEmail())
                    .messageBody("There was a login made to your account. Contact your branch if this wasn't you")
                    .build();

            emailService.sendEmailAlert(loginAlert);

            return BankResponse.builder()
                    .reponseCode("222")
                    .responseMessage("Login Successful! "+jwtTokenProvider.generateToken(loginModel.getEmail()))
                    .accountModel(null)
                    .build();


        }
        else return BankResponse.builder()
                .reponseCode("402")
                .responseMessage("Login Unsuccessful")
                .accountModel(null)
                .build();
    }


    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .reponseCode(Accountutils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(Accountutils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountModel(null)
                    .build();
        }
        UserEntity foundUser = userRepository.findByAccountNumber(request.getAccountNumber());
        return BankResponse.builder()
                .reponseCode(Accountutils.ACCOUNT_FOUND_CODE)
                .responseMessage(Accountutils.ACCOUNT_FOUND_MESSAGE)
                .accountModel(AccountModel.builder()
                        .accountNumber(foundUser.getAccountNumber())
                        .accountName(foundUser.getUserFirstName() + " " + foundUser.getUserLastName())
                        .accountBalance(foundUser.getAccountBalance())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return Accountutils.ACCOUNT_NOT_EXISTS_MESSAGE;
        }
        UserEntity foundUser = userRepository.findByAccountNumber(request.getAccountNumber());
        return foundUser.getUserFirstName() + " " + foundUser.getUserLastName();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest request) {

        //Checking if account exists
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .reponseCode(Accountutils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(Accountutils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountModel(null)
                    .build();
        }

        UserEntity creditedUser = userRepository.findByAccountNumber(request.getAccountNumber());

        creditedUser.setAccountBalance(creditedUser.getAccountBalance().add(request.getAmount()));
        userRepository.save(creditedUser);

        TransactionModel transactionModel = TransactionModel.builder()
                .accountNumber(creditedUser.getAccountNumber())
                .amount(request.getAmount())
                .transactionType("CREDIT")
                .build();

        transactionService.saveTransaction(transactionModel);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(creditedUser.getUserEmail())
                .subject("Transaction Alert")
                .messageBody("There has been a Transaction into your account. Below are the details\n" +
                        "Account Name: " + creditedUser.getUserFirstName() + " " + creditedUser.getUserLastName() +
                        "\nAccount Number: " + creditedUser.getAccountNumber() +
                        "\nTransaction Type: CREDIT"+
                        "\nNew Account Balance: " + creditedUser.getAccountBalance())
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .reponseCode(Accountutils.ACCOUNT_CREDIT_SUCCESS_CODE)
                .responseMessage(Accountutils.ACCOUNT_CREDIT_SUCCESS_MESSAGE)
                .accountModel(AccountModel.builder()
                        .accountNumber(creditedUser.getAccountNumber())
                        .accountName(creditedUser.getUserFirstName() + " " + creditedUser.getUserLastName())
                        .accountBalance(creditedUser.getAccountBalance())
                        .build())
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {

        //Checking if account exists
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());

        if (!isAccountExist) {
            return BankResponse.builder()
                    .reponseCode(Accountutils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(Accountutils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountModel(null)
                    .build();
        }

        UserEntity debitedUser = userRepository.findByAccountNumber(request.getAccountNumber());

        BigDecimal balance = debitedUser.getAccountBalance();
        BigDecimal amountToDebit = request.getAmount();

        //Checking is sufficient balance is there
        if(balance.compareTo(amountToDebit)==-1){
            return BankResponse.builder()
                    .reponseCode(Accountutils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(Accountutils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountModel(AccountModel.builder()
                            .accountNumber(debitedUser.getAccountNumber())
                            .accountBalance(debitedUser.getAccountBalance())
                            .build())
                    .build();
        }

        debitedUser.setAccountBalance(debitedUser.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(debitedUser);

        TransactionModel transactionModel = TransactionModel.builder()
                .accountNumber(debitedUser.getAccountNumber())
                .amount(request.getAmount())
                .transactionType("DEBIT")
                .build();

        transactionService.saveTransaction(transactionModel);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(debitedUser.getUserEmail())
                .subject("Transaction Alert")
                .messageBody("There has been a Transaction into your account. Below are the details\n" +
                        "Account Name: " + debitedUser.getUserFirstName() + " " + debitedUser.getUserLastName() +
                        "\nAccount Number: " + debitedUser.getAccountNumber() +
                        "\nTransaction Type: DEBIT"+
                        "\nNew Account Balance: " + debitedUser.getAccountBalance())
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .reponseCode(Accountutils.ACCOUNT_DEBIT_SUCCESS_CODE)
                .responseMessage(Accountutils.ACCOUNT_DEBIT_SUCCESS_MESSAGE)
                .accountModel(AccountModel.builder()
                        .accountNumber(debitedUser.getAccountNumber())
                        .accountName(debitedUser.getUserFirstName() + " " + debitedUser.getUserLastName())
                        .accountBalance(debitedUser.getAccountBalance())
                        .build())
                .build();
    }


    @Override
    public BankResponse transferAmount(TransferRequest request) {
        boolean isRecipientAccountExist = userRepository.existsByAccountNumber(request.getRecipientAccountNumber());
        if (!isRecipientAccountExist) {
            return BankResponse.builder()
                    .reponseCode(Accountutils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(Accountutils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountModel(null)
                    .build();
        }
        UserEntity senderAccount = userRepository.findByAccountNumber(request.getSenderAccountNumber());
        UserEntity recipientAccount = userRepository.findByAccountNumber(request.getRecipientAccountNumber());

        if(request.getAmount().compareTo(senderAccount.getAccountBalance())==1){
            return BankResponse.builder()
                    .reponseCode(Accountutils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(Accountutils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountModel(AccountModel.builder()
                            .accountNumber(senderAccount.getAccountNumber())
                            .accountBalance(senderAccount.getAccountBalance())
                            .build())
                    .build();
        }

        senderAccount.setAccountBalance(senderAccount.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(senderAccount);

        TransactionModel senderTransactionModel = TransactionModel.builder()
                .accountNumber(senderAccount.getAccountNumber())
                .amount(request.getAmount())
                .transactionType("DEBIT")
                .build();

        transactionService.saveTransaction(senderTransactionModel);
        EmailDetails senderEmailDetails = EmailDetails.builder()
                .recipient(senderAccount.getUserEmail())
                .subject("Transaction Alert")
                .messageBody("There has been a Transaction into your account. Below are the details\n" +
                        "Account Name: " + senderAccount.getUserFirstName() + " " + senderAccount.getUserLastName() +
                        "\nAccount Number: " + senderAccount.getAccountNumber() +
                        "\nTransaction Type: DEBIT"+
                        "\nNew Account Balance: " + senderAccount.getAccountBalance()+
                        "\nReceiver Account Name: "+recipientAccount.getUserFirstName() + " " + recipientAccount.getUserLastName())
                .build();
        emailService.sendEmailAlert(senderEmailDetails);

        recipientAccount.setAccountBalance(recipientAccount.getAccountBalance().add(request.getAmount()));
        userRepository.save(recipientAccount);

        TransactionModel recipientTransactionModel = TransactionModel.builder()
                .accountNumber(recipientAccount.getAccountNumber())
                .amount(request.getAmount())
                .transactionType("CREDIT")
                .build();

        transactionService.saveTransaction(recipientTransactionModel);
        EmailDetails recipientEmailDetails = EmailDetails.builder()
                .recipient(recipientAccount.getUserEmail())
                .subject("Transaction Alert")
                .messageBody("There has been a Transaction into your account. Below are the details\n" +
                        "Account Name: " + recipientAccount.getUserFirstName() + " " + recipientAccount.getUserLastName() +
                        "\nAccount Number: " + recipientAccount.getAccountNumber() +
                        "\nTransaction Type: CREDIT"+
                        "\nNew Account Balance: " + recipientAccount.getAccountBalance()+
                        "\nSender Account Name: "+senderAccount.getUserFirstName() + " " + senderAccount.getUserLastName())
                .build();
        emailService.sendEmailAlert(recipientEmailDetails);

        return BankResponse.builder()
                .reponseCode(Accountutils.ACCOUNT_TRANSFER_SUCCESS_CODE)
                .responseMessage(Accountutils.ACCOUNT_TRANSFER_SUCCESS_MESSAGE)
                .accountModel(null)
                .build();
    }

}
