package com.fundquest.email_service.service;

import com.fundquest.email_service.config.EmailProperties;
import com.fundquest.email_service.dto.EmailResponse;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.GraphServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final GraphServiceClient graphServiceClient;
    private final EmailProperties emailProperties;

    @Autowired
    public EmailService(GraphServiceClient graphServiceClient, EmailProperties emailProperties) {
        this.graphServiceClient = graphServiceClient;
        this.emailProperties = emailProperties;
    }

    public void sendWelcomeEmail(String toEmail) {
        try {
            logger.info("Attempting to send welcome email to: {}", toEmail);

            // Create the message
            Message message = new Message();
            message.subject = "Welcome to Microsoft Email Testing Integration";

            // Set message body
            ItemBody body = new ItemBody();
            body.contentType = BodyType.HTML;
            body.content = createWelcomeEmailBody();
            message.body = body;

            // Set recipients
            Recipient toRecipient = new Recipient();
            EmailAddress toEmailAddress = new EmailAddress();
            toEmailAddress.address = toEmail;
            toRecipient.emailAddress = toEmailAddress;
            message.toRecipients = Arrays.asList(toRecipient);

            // Set sender information
            Recipient fromRecipient = new Recipient();
            EmailAddress fromEmailAddress = new EmailAddress();
            fromEmailAddress.address = emailProperties.getFromAddress();
            fromEmailAddress.name = emailProperties.getFromName();
            fromRecipient.emailAddress = fromEmailAddress;
            message.from = fromRecipient;

            // Set reply-to
            Recipient replyToRecipient = new Recipient();
            EmailAddress replyToEmailAddress = new EmailAddress();
            replyToEmailAddress.address = emailProperties.getReplyTo();
            replyToRecipient.emailAddress = replyToEmailAddress;
            message.replyTo = Arrays.asList(replyToRecipient);

            // Send the email using application permissions
            // Note: Since we're using application permissions, we need to send on behalf of a user
            // For this example, we'll use the service account or a designated sender
            String userId = emailProperties.getFromAddress(); // Using the from address as the user ID

            var sendMailParameters = new UserSendMailParameterSet();
            sendMailParameters.message = message;
            sendMailParameters.saveToSentItems = false;

            graphServiceClient
                    .users(userId)
                    .sendMail(sendMailParameters)
                    .buildRequest()
                    .post();

            logger.info("Email sent successfully to: {}", toEmail);
            //return new EmailResponse(true, "Email sent successfully");

        } catch (Exception e) {
            logger.error("Failed to send email to: {}. Error: {}", toEmail, e.getMessage(), e);
            //return new EmailResponse(false, "Failed to send email: " + e.getMessage());
        }
    }

    private String createWelcomeEmailBody() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to Microsoft Email Testing Integration</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background-color: #f8f9fa; padding: 30px; border-radius: 10px; text-align: center;">
                    <h1 style="color: #0078d4; margin-bottom: 20px;">Welcome to Microsoft Email Testing Integration!</h1>
                    
                    <p style="font-size: 16px; margin-bottom: 20px;">
                        Congratulations! You have successfully integrated with Microsoft Graph API for email sending.
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-bottom: 20px;">
                        This email was sent using Microsoft Graph API through Azure Active Directory application permissions.
                    </p>
                    
                    <div style="background-color: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 0; font-size: 14px;">
                            <strong>Integration Details:</strong><br>
                            • Powered by Microsoft Graph API<br>
                            • Sent from FundQuest Email Service<br>
                            • Using Application Permissions
                        </p>
                    </div>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 30px;">
                        If you have any questions, please don't hesitate to contact our support team.
                    </p>
                    
                    <hr style="border: none; height: 1px; background-color: #ddd; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #999;">
                        Best regards,<br>
                        <strong>FundQuest Support Team</strong>
                    </p>
                </div>
            </body>
            </html>
            """;
    }
}