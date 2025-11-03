package com.deodev.User_Registration_System.dto;

import lombok.Builder;

@Builder
public record EmailContent(
        String template,
        String subject,
        String recipientName,
        String recipientAddress
) {
}
