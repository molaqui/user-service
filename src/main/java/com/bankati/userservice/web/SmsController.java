package com.bankati.userservice.web;


import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private VonageClient vonageClient;
    @PostMapping("/send")
    public String sendSms(@RequestParam String to, @RequestParam String text) {
        TextMessage message = new TextMessage(
                "Vonage APIs", // Nom de l'expéditeur
                to,           // Numéro du destinataire
                text          // Contenu du SMS
        );

        try {
            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);
            if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
                return "Message sent successfully!";
            } else {
                return "Message failed with error: " + response.getMessages().get(0).getErrorText();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}

