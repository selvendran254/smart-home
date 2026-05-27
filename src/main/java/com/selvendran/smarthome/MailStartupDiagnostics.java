package com.selvendran.smarthome;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * Logs whether SMTP is configured; check console if emails never arrive.
 */
@Component
public class MailStartupDiagnostics implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MailStartupDiagnostics.class);

    private final Environment env;
    private final ObjectProvider<JavaMailSender> mailSender;

    public MailStartupDiagnostics(Environment env, ObjectProvider<JavaMailSender> mailSender) {
        this.env = env;
        this.mailSender = mailSender;
    }

    @Override
    public void afterPropertiesSet() {
        String host = env.getProperty("spring.mail.host", "");
        String user = env.getProperty("spring.mail.username", "");
        String pass = env.getProperty("spring.mail.password", "");
        String to = env.getProperty("smarthome.notify.email.to", "");
        boolean senderOk = mailSender.getIfAvailable() != null;

        log.info(
                "[mail-startup] hostConfigured={} usernameConfigured={} passwordLength={} notifyToConfigured={} javaMailSenderBean={}",
                !host.isBlank(),
                !user.isBlank(),
                pass != null ? pass.length() : 0,
                !to.isBlank(),
                senderOk);

        if (!host.isBlank() && (!senderOk || to.isBlank() || user.isBlank() || pass == null || pass.isBlank())) {
            log.warn("[mail-startup] Email alerts will NOT work until host, username, password, and smarthome.notify.email.to are all set. Open /door/mail-health after start.");
        } else if (!host.isBlank() && senderOk) {
            JavaMailSender bean = mailSender.getIfAvailable();
            if (bean instanceof JavaMailSenderImpl impl) {
                try {
                    impl.testConnection();
                    log.info("[mail-startup] SMTP testConnection() succeeded. Send test: /door/mail-test — also check Spam folder.");
                } catch (MessagingException e) {
                    log.error("[mail-startup] SMTP testConnection() FAILED — Gmail App Password or network issue: {}", e.getMessage());
                }
            }
        }
    }
}
