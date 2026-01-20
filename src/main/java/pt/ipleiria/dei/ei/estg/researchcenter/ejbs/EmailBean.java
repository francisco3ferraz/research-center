package pt.ipleiria.dei.ei.estg.researchcenter.ejbs;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.logging.Logger;

@Stateless
public class EmailBean {

    @Resource(name = "java:/jboss/mail/fakeSMTP")
    private Session mailSession;

    private static final Logger logger = Logger.getLogger("ejbs.EmailBean");

    public void send(String to, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        try {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            logger.warning("Erro a enviar email para " + to + ": " + e.getMessage());
            throw e;
        }
        logger.info("Email enviado com sucesso para " + to);
    }

    public void sendPasswordResetEmail(String to, String userName, String resetToken, String frontendUrl) 
            throws MessagingException {
        String subject = "Recuperação de Password";
        String body = "<html><body>" +
                "<h2>Olá " + userName + "!</h2>" +
                "<p>Recebemos um pedido para recuperar a sua password.</p>" +
                "<p>O seu código de recuperação é: <strong>" + resetToken + "</strong></p>" +
                "<p>Ou clique no link: <a href=\"" + frontendUrl + "\">" + frontendUrl + "</a></p>" +
                "<p>Este código expira em 1 hora.</p>" +
                "<p>Se não fez este pedido, ignore este email.</p>" +
                "</body></html>";
        send(to, subject, body);
    }
}
