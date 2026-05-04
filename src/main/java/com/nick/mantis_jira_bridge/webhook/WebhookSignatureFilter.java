package com.nick.mantis_jira_bridge.webhook;

import com.nick.mantis_jira_bridge.config.MantisProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookSignatureFilter extends OncePerRequestFilter {

    private static final String SIGNATURE_HEADER = "X-Mantis-Signature";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String WEBHOOK_PATH = "/webhooks/mantis";

    private final MantisProperties mantisProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(WEBHOOK_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String secret = mantisProperties.getWebhookSecret();

        var cached = new CachedBodyHttpServletRequest(request);

        if (secret == null || secret.isBlank()) {
            filterChain.doFilter(cached, response);
            return;
        }

        String signature = request.getHeader(SIGNATURE_HEADER);
        if (signature == null || signature.isBlank()) {
            log.warn("Missing webhook signature header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing signature");
            return;
        }

        if (!isValidSignature(cached.getCachedBody(), signature, secret)) {
            log.warn("Invalid webhook signature");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid signature");
            return;
        }

        filterChain.doFilter(cached, response);
    }

    private boolean isValidSignature(byte[] body, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] expected = mac.doFinal(body);
            String raw = signature.startsWith("sha256=") ? signature.substring(7) : signature;
            byte[] actual = HexFormat.of().parseHex(raw);
            return MessageDigest.isEqual(expected, actual);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalArgumentException e) {
            log.error("Signature verification error", e);
            return false;
        }
    }
}
