package br.com.divulgaifback.common.services;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import freemarker.template.Configuration;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.mail.from}")
    private String from;

    private final Configuration configuration;
    private final JavaMailSender mailSender;

    public void sendNewFeedbackAddedEmail(String to, String name, String workTitle, String feedback) throws IOException, TemplateException, MessagingException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();

        model.put("name", name);
        model.put("workTitle", workTitle);
        model.put("feedback", feedback);

        configuration.getTemplate("feedback.ftlh").process(model, stringWriter);
        var body = stringWriter.getBuffer().toString();
        sendMailHtml(to, null, "Prezado " + name + ", novo Feedback adicionado em seu trabalho: " + workTitle, body);
    }

    public void sendPasswordResetEmail(String to, String name, String resetLink, String language) throws IOException, TemplateException, MessagingException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();

        model.put("name", name);
        model.put("resetLink", resetLink);

        String templateName = "pt".equalsIgnoreCase(language) ? "password-reset-pt.ftlh" : "password-reset-en.ftlh";
        String subject = "pt".equalsIgnoreCase(language)
            ? "Redefinição de Senha - DivulgaIF"
            : "Password Reset - DivulgaIF";

        configuration.getTemplate(templateName).process(model, stringWriter);
        var body = stringWriter.getBuffer().toString();
        sendMailHtml(to, null, subject, body);
    }

    public void sendMailHtml(String to, String cc, String subject, String body) throws MessagingException {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setFrom(new InternetAddress(from));
        helper.setTo(to);
        if (Objects.nonNull(cc)) helper.setCc(cc);

        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(mimeMessage);
    }
}
