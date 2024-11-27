package com.XYZBank.BankSys.Service;

import com.XYZBank.BankSys.Model.EmailDetails;

public interface EmailService {

    void sendEmailAlert(EmailDetails emailDetails);

    void sendEmailWithAttachment(EmailDetails emailDetails);

}
