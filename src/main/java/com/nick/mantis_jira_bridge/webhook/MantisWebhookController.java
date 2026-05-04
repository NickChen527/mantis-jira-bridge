package com.nick.mantis_jira_bridge.webhook;

import com.nick.mantis_jira_bridge.common.ApiResponse;
import com.nick.mantis_jira_bridge.ingestion.IssueIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhooks/mantis")
@RequiredArgsConstructor
public class MantisWebhookController {

    private final IssueIngestionService ingestionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SyncResponse>> receiveIssue(
            @Valid @RequestBody MantisWebhookPayload payload) {

        var issue = payload.getIssue();
        log.info("Received Mantis webhook for issue #{}", issue.getId());

        var result = ingestionService.ingest(issue);

        return ResponseEntity.accepted()
                .body(ApiResponse.success(new SyncResponse(result.mantisIssueId(), result.jiraIssueKey())));
    }

    public record SyncResponse(Long mantisIssueId, String jiraIssueKey) {}
}
