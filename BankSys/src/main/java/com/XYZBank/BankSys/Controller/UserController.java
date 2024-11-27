package com.XYZBank.BankSys.Controller;

import com.XYZBank.BankSys.Model.*;
import com.XYZBank.BankSys.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Account Related APIs")
public class UserController {

    @Autowired
    UserService userService;

//    @GetMapping("/")
//    public String helloWorldTest(){
//        return "Hello World testing";
//    }
    @Operation(
            summary = "Creation of User Account",
            description = "Creating a new user and assigning account number"
    )
    @ApiResponse(
            responseCode = "201",
            description = "HTTP Status 201 CREATED"
    )
    @PostMapping("/register")
    public BankResponse createAccount(@RequestBody UserModel userModel){
        return userService.createAccount(userModel);
    }

    @Operation(
            summary = "Login Page",
            description = "Logging in to the platform"
    )
    @ApiResponse(
            responseCode = "225",
            description = "HTTP Status 225 LOGGED IN"
    )
    @PostMapping("/login")
    public BankResponse login(@RequestBody LoginModel loginModel){
        //System.out.println(loginModel.getEmail()+" "+loginModel.getPassword());
        return userService.login(loginModel);
    }

    @Operation(
            summary = "Enquiring Balance",
            description = "Finding Balance of a User using Account Number"
    )
    @ApiResponse(
            responseCode = "202",
            description = "HTTP Status 202 BALANCE RETRIEVED"
    )
    @GetMapping("/balanceEnquiry")
    public BankResponse balanceEnquiry(@RequestBody EnquiryRequest request){
        return userService.balanceEnquiry(request);
    }

    @Operation(
            summary = "Enquiring Name",
            description = "Finding Name of a User using Account Number"
    )
    @ApiResponse(
            responseCode = "203",
            description = "HTTP Status 202 USER NAME RETRIEVED"
    )
    @GetMapping("/nameEnquiry")
    public String nameEnquiry(@RequestBody EnquiryRequest request){
        return userService.nameEnquiry(request);
    }

    @Operation(
            summary = "Credit Account",
            description = "Crediting an Account with Amount using Account Number"
    )
    @ApiResponse(
            responseCode = "204",
            description = "HTTP Status 204 ACCOUNT CREDITED"
    )
    @PostMapping("/credit")
    public BankResponse creditAccount(@RequestBody CreditDebitRequest request){
        return userService.creditAccount(request);
    }

    @Operation(
            summary = "Debit Account",
            description = "Debiting an Account with Amount using Account Number"
    )
    @ApiResponse(
            responseCode = "205",
            description = "HTTP Status 205 ACCOUNT DEBITED"
    )
    @PostMapping("/debit")
    public BankResponse debitAccount(@RequestBody CreditDebitRequest request){
        return userService.debitAccount(request);
    }

    @Operation(
            summary = "Amount Transfer",
            description = "Transferring amount between accounts using Account Number"
    )
    @ApiResponse(
            responseCode = "206",
            description = "HTTP Status 206 AMOUNT TRANSFER SUCCESSFUL"
    )
    @PostMapping("/transfer")
    public BankResponse transferAccount(@RequestBody TransferRequest request){
        return userService.transferAmount(request);
    }

    @GetMapping("/")
    public String greet(HttpServletRequest request){
        return request.getSession().getId();
    }

}
