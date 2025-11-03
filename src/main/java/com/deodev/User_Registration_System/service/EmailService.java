package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.dto.EmailContent;

public interface EmailService {
    public void sendMail(EmailContent emailContent);
}
