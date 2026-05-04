package com.nick.mantis_jira_bridge.webhook;

import com.nick.mantis_jira_bridge.ingestion.IssueIngestionService;
import com.nick.mantis_jira_bridge.mapping.IssueMapping;
import com.nick.mantis_jira_bridge.mapping.SyncStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookSignatureFilterTest {

    static final String SECRET = "test-webhook-secret";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("bridge.jira.base-url", () -> "http://jira.test");
        registry.add("bridge.jira.email", () -> "test@example.com");
        registry.add("bridge.jira.api-token", () -> "test-token");
        registry.add("bridge.jira.project-key", () -> "PROJ");
        registry.add("bridge.mantis.base-url", () -> "http://mantis.test");
        registry.add("bridge.mantis.api-token", () -> "mantis-token");
        registry.add("bridge.mantis.webhook-secret", () -> SECRET);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IssueIngestionService ingestionService;

    @Test
    void validSignature_allowsRequest() throws Exception {
        when(ingestionService.ingest(any())).thenReturn(new IssueIngestionService.SyncResult(200L, "PROJ-1"));

        String body = payload(200L);
        String sig = "sha256=" + computeHmac(body, SECRET);

        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Mantis-Signature", sig)
                        .content(body))
                .andExpect(status().isAccepted());
    }

    @Test
    void missingSignature_returns401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(201L)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidSignature_returns401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Mantis-Signature",
                                "sha256=deadbeef00112233445566778899aabbccddeeff00112233445566778899aabb")
                        .content(payload(202L)))
                .andExpect(status().isUnauthorized());
    }

    private String payload(Long id) {
        return String.format(
                "{\"action\":\"issue_created\",\"issue\":{\"id\":%d,\"summary\":\"Test\",\"description\":\"Desc\"}}",
                id);
    }

    private String computeHmac(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
