package com.nick.mantis_jira_bridge.webhook;

import com.nick.mantis_jira_bridge.jira.JiraCallService;
import com.nick.mantis_jira_bridge.jira.JiraCreateIssueResponse;
import com.nick.mantis_jira_bridge.mapping.IssueMappingRepository;
import com.nick.mantis_jira_bridge.mapping.SyncStatus;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MantisWebhookControllerTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("bridge.jira.base-url", () -> "http://jira.test");
        registry.add("bridge.jira.email", () -> "test@example.com");
        registry.add("bridge.jira.api-token", () -> "test-token");
        registry.add("bridge.jira.project-key", () -> "PROJ");
        registry.add("bridge.mantis.base-url", () -> "http://mantis.test");
        registry.add("bridge.mantis.api-token", () -> "mantis-token");
        registry.add("bridge.mantis.webhook-secret", () -> "");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IssueMappingRepository repository;

    @MockBean
    private JiraCallService jiraCallService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void receiveIssue_createsJiraIssueAndReturns202() throws Exception {
        var jiraResponse = new JiraCreateIssueResponse();
        setField(jiraResponse, "id", "10001");
        setField(jiraResponse, "key", "PROJ-1");
        when(jiraCallService.createIssue(any())).thenReturn(jiraResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mantisPayload(101L, "Login fails", "Steps: 1. Open app")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jiraIssueKey").value("PROJ-1"));

        var mapping = repository.findByMantisIssueId(101L);
        assertThat(mapping).isPresent();
        assertThat(mapping.get().getStatus()).isEqualTo(SyncStatus.CREATED);
        assertThat(mapping.get().getJiraIssueKey()).isEqualTo("PROJ-1");
    }

    @Test
    void receiveIssue_duplicateWebhookIsIdempotent() throws Exception {
        var jiraResponse = new JiraCreateIssueResponse();
        setField(jiraResponse, "id", "10002");
        setField(jiraResponse, "key", "PROJ-2");
        when(jiraCallService.createIssue(any())).thenReturn(jiraResponse);

        String payload = mantisPayload(102L, "Duplicate test", "Desc");

        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted());

        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.jiraIssueKey").value("PROJ-2"));

        verify(jiraCallService, times(1)).createIssue(any());
    }

    @Test
    void receiveIssue_jiraThrowsException_returns502() throws Exception {
        when(jiraCallService.createIssue(any())).thenThrow(
                feign.FeignException.errorStatus("createIssue",
                        feign.Response.builder()
                                .status(500)
                                .reason("Internal Server Error")
                                .request(feign.Request.create(
                                        feign.Request.HttpMethod.POST,
                                        "http://jira.test/rest/api/3/issue",
                                        java.util.Collections.emptyMap(),
                                        null,
                                        null,
                                        null))
                                .build()));

        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mantisPayload(103L, "Error case", "Desc")))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false));

        var mapping = repository.findByMantisIssueId(103L);
        assertThat(mapping).isPresent();
        assertThat(mapping.get().getStatus()).isEqualTo(SyncStatus.FAILED);
    }

    @Test
    void receiveIssue_missingIssueField_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/webhooks/mantis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"issue_created\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private String mantisPayload(Long issueId, String summary, String description) {
        return String.format("""
                {
                  "action": "issue_created",
                  "issue": {
                    "id": %d,
                    "summary": "%s",
                    "description": "%s",
                    "priority": {"id": 4, "name": "normal", "label": "Normal"},
                    "project": {"id": 1, "name": "Test Project"}
                  }
                }
                """, issueId, summary, description);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
