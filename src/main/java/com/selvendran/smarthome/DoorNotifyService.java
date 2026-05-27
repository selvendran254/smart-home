package com.selvendran.smarthome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sends door events by email when {@code spring.mail.host} and {@code smarthome.notify.email.to} are set.
 * Uses MIME messages (UTF-8) for better Gmail compatibility than {@code SimpleMailMessage}.
 */
@Service
public class DoorNotifyService {

    private static final Logger log = LoggerFactory.getLogger(DoorNotifyService.class);
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String toAddress;
    private final String fromAddress;

    public DoorNotifyService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${smarthome.notify.email.to:}") String toAddress,
            @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.toAddress = toAddress != null ? toAddress.strip() : "";
        this.fromAddress = fromAddress != null ? fromAddress.strip() : "";
    }

    /** Call from browser: GET /door/mail-health */
    public Map<String, Object> mailHealth() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("javaMailSenderBeanPresent", mailSender.getIfAvailable() != null);
        m.put("notifyToConfigured", !toAddress.isEmpty());
        m.put("springMailUsernameConfigured", !fromAddress.isEmpty());
        m.put("readyToSend",
                mailSender.getIfAvailable() != null && !toAddress.isEmpty() && !fromAddress.isEmpty());
        return m;
    }

    /** Call from browser: GET /door/mail-test — sends one email. */
    public String sendTestMail() {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            return "FAIL: No JavaMailSender. Set spring.mail.host (and restart). See /door/mail-health";
        }
        if (toAddress.isEmpty()) {
            return "FAIL: smarthome.notify.email.to is empty in application-local.properties";
        }
        if (fromAddress.isEmpty()) {
            return "FAIL: spring.mail.username is empty (should be your Gmail address)";
        }
        try {
            sendMime(sender, "Smart Home - SMTP test", "If you see this message, Gmail SMTP is working.");
            return "OK: Test email sent. Check inbox and Spam for " + toAddress;
        } catch (Exception e) {
            log.error("[door-notify] Test mail failed", e);
            return "FAIL: " + e.getClass().getSimpleName() + " — " + e.getMessage()
                    + " (wrong App Password, 2FA off, or firewall blocking port 587?)";
        }
    }

    public void notifyPinUnlock(String profileLabel) {
        String who = (profileLabel == null || profileLabel.isBlank()) ? "A profile" : profileLabel.strip();
        String subject = "Smart Home - door unlocked (PIN)";
        String body = "Door unlock via PIN succeeded.\n\n"
                + "Profile: " + who + "\n"
                + "Time: " + FMT.format(ZonedDateTime.now()) + "\n";
        send(subject, body);
    }

    public void notifyPinUnlockFailed(String profileLabel) {
        String who = (profileLabel == null || profileLabel.isBlank()) ? "A profile" : profileLabel.strip();
        String subject = "Smart Home - ALERT: failed door unlock (PIN)";
        String body = "WARNING: Someone entered an incorrect PIN to unlock the door.\n\n"
                + "Profile selected: " + who + "\n"
                + "Time: " + FMT.format(ZonedDateTime.now()) + "\n"
                + "Please check your Smart Home dashboard or camera if this was unexpected.\n";
        send(subject, body);
    }

    public void notifyFaceUnlockStarted() {
        String subject = "Smart Home - face unlock started";
        String body = "Face verification was started from the dashboard.\n\n"
                + "Time: " + FMT.format(ZonedDateTime.now()) + "\n";
        send(subject, body);
    }

    public void notifyLoginSuccess(String username) {
        String u = username != null ? username.strip() : "";
        String subject = "Smart Home - login successful";
        String body = "Someone signed in to your Smart Home account.\n\n"
                + "Username: " + (u.isEmpty() ? "(unknown)" : u) + "\n"
                + "Time: " + FMT.format(ZonedDateTime.now()) + "\n";
        send(subject, body);
    }

    /**
     * @param reason {@code user_not_found}, {@code wrong_password}, {@code invalid_request}, or other
     */
    public void notifyLoginFailed(String usernameTried, String reason) {
        String name = usernameTried != null ? usernameTried.strip() : "";
        String reasonText = switch (reason != null ? reason : "") {
            case "user_not_found" -> "Username is not registered";
            case "wrong_password" -> "Wrong password";
            case "invalid_request" -> "Username was empty";
            default -> reason != null && !reason.isBlank() ? reason : "Unknown";
        };
        String subject = "Smart Home - failed login attempt";
        String body = "A failed login was attempted on the Smart Home app.\n\n"
                + "Username tried: " + (name.isEmpty() ? "(empty)" : name) + "\n"
                + "Reason: " + reasonText + "\n"
                + "Time: " + FMT.format(ZonedDateTime.now()) + "\n";
        send(subject, body);
    }

    private void send(String subject, String text) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("[door-notify] Email skipped (no JavaMailSender). Set spring.mail.host. Subject: {}", subject);
            return;
        }
        if (toAddress.isEmpty()) {
            log.warn("[door-notify] Email skipped (smarthome.notify.email.to empty). Subject: {}", subject);
            return;
        }
        if (fromAddress.isEmpty()) {
            log.warn("[door-notify] Email skipped (spring.mail.username empty). Subject: {}", subject);
            return;
        }
        try {
            sendMime(sender, subject, text);
            log.info("[door-notify] Sent email: {}", subject);
        } catch (Exception e) {
            log.error("[door-notify] Failed to send '{}'. Check Gmail App Password and port 587.", subject, e);
        }
    }

    private void sendMime(JavaMailSender sender, String subject, String body) throws Exception {
        var mime = sender.createMimeMessage();
        var helper = new MimeMessageHelper(mime, false, "UTF-8");
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(body, false);
        // Plain address only (no display name) — avoids encoding issues with some Gmail/JavaMail setups.
        helper.setFrom(fromAddress);
        sender.send(mime);
    }
}
