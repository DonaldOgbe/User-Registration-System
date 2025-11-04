package com.deodev.User_Registration_System.service;

import com.deodev.User_Registration_System.dto.EmailContent;
import io.mailtrap.client.MailtrapClient;
import io.mailtrap.client.api.MailtrapEmailSendingApi;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import io.mailtrap.model.response.emails.SendResponse;
import io.mailtrap.api.sendingemails.SendingEmails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailTrapEmailServiceTest {
    @Mock
    private MailTrapClientFactory mailTrapClientFactory;

    @Mock
    private MailtrapClient mailtrapClient;

    @Mock
    private MailtrapEmailSendingApi sendingApi;

    @Mock
    private SendingEmails sendingEmails;

    @Mock
    private MailtrapClient client;

    @Mock
    private SendResponse sendResponse;

    @InjectMocks
    private MailTrapEmailService mailTrapEmailService;


    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(mailTrapEmailService, "appDomain", "no-reply@myapp.com");
        ReflectionTestUtils.setField(mailTrapEmailService, "providerToken", "fake-api-key");
    }

    @Test
    void sendMail_ShouldBuildAndSendCorrectMail() throws Exception {
        // given
        EmailContent emailContent = new EmailContent(
                "<p>Hello there!</p>",
                "Welcome!",
                "user",
                "user@email.com"
        );

        when(mailTrapClientFactory.createMailTrapClient(any())).thenReturn(client);
        when(client.sendingApi()).thenReturn(sendingApi);
        when(sendingApi.emails()).thenReturn(sendingEmails);
        when(sendingEmails.send(any())).thenReturn(sendResponse);
        when(sendResponse.isSuccess()).thenReturn(true);

        // when
        mailTrapEmailService.sendMail(emailContent);

        // then
        ArgumentCaptor<MailtrapMail> mailCaptor = ArgumentCaptor.forClass(MailtrapMail.class);
        verify(client.sendingApi().emails()).send(mailCaptor.capture());

        MailtrapMail sentMail = mailCaptor.getValue();
        List<Address> to = sentMail.getTo();
        assertEquals("user@email.com", to.get(0).getEmail());
        assertThat(sentMail.getSubject()).isEqualTo("Welcome!");
        assertThat(sentMail.getHtml()).isEqualTo("<p>Hello there!</p>");
    }

}