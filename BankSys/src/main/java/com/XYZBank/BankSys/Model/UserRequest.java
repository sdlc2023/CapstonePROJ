package com.XYZBank.BankSys.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userFirstName;

    private String userLastName;

    private String userGender;

    private String userAddress;

    private String accountNumber;

    private String userMobileNumber;

    private String userEmail;

    private String userPassword;

    private BigDecimal accountBalance;

    private String userStatus;

    @CreationTimestamp
    private LocalDateTime userCreatedAt;

    @UpdateTimestamp
    private LocalDateTime userModifiedAt;

}
