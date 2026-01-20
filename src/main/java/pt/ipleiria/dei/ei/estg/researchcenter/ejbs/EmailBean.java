package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Logger;

@Stateless
public class EmailBean {
    
    private static final Logger logger = Logger.getLogger(EmailBean.class.getName());
    
    private static final String SMTP_HOST = System.getenv().getOrDefault("SMTP_HOST", "smtp");
    private static final String SMTP_PORT = System.getenv().getOrDefault("SMTP_PORT", "25");
    private static final String FROM_EMAIL = "noreply@research-center.pt";
    private static final String FROM_NAME = "Centro de Investigação XYZ";
    
    public void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        
        Session session = Session.getInstance(props);
        
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            
            Transport.send(message);
            logger.info("Email sent successfully to: " + to);
            
        } catch (Exception e) {
            logger.severe("Failed to send email to " + to + ": " + e.getMessage());
            throw new MessagingException("Failed to send email: " + e.getMessage(), e);
        }
    }
    
    public void sendPasswordResetEmail(String to, String userName, String resetToken, String resetUrl) 
            throws MessagingException {
        
        String subject = "Recuperação de Password - Centro de Investigação XYZ";
        
        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2563eb; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background-color: #f9fafb; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #2563eb; 
                              color: white; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { padding: 20px; text-align: center; font-size: 12px; color: #666; }
                    .token { background-color: #e5e7eb; padding: 10px; font-family: monospace; 
                             border-radius: 4px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Centro de Investigação XYZ</h1>
                    </div>
                    <div class="content">
                        <h2>Olá, %s!</h2>
                        <p>Recebemos um pedido para recuperar a sua password.</p>
                        <p>Se não fez este pedido, pode ignorar este email.</p>
                        <p>Para redefinir a sua password, use o seguinte código:</p>
                        <div class="token">%s</div>
                        <p>Ou clique no botão abaixo:</p>
                        <a href="%s" class="button">Redefinir Password</a>
                        <p><strong>Nota:</strong> Este código expira em 1 hora.</p>
                    </div>
                    <div class="footer">
                        <p>Este email foi enviado automaticamente. Por favor, não responda.</p>
                        <p>&copy; 2025 Centro de Investigação XYZ</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, resetToken, resetUrl);
        
        sendEmail(to, subject, htmlBody);
    }
}
