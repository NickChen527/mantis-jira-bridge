package com.nick.mantis_jira_bridge.poller;

import com.nick.mantis_jira_bridge.config.MantisProperties;
import com.nick.mantis_jira_bridge.ingestion.IssueIngestionService;
import com.nick.mantis_jira_bridge.mantis.MantisClient;
import com.nick.mantis_jira_bridge.mantis.dto.MantisIssueListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "bridge.mantis.poll.enabled", havingValue = "true")
public class MantisPollingScheduler {

    private static final int PAGE_SIZE = 50;

    private final MantisClient mantisClient;
    private final IssueIngestionService ingestionService;
    private final MantisProperties mantisProperties;

    @Scheduled(fixedDelayString = "${bridge.mantis.poll.interval-ms:300000}")
    public void poll() {
        log.info("Polling Mantis for new issues");
        int page = 1;
        int synced = 0;
        int errors = 0;

        while (true) {
            MantisIssueListResponse response;
            try {
                response = mantisClient.getIssues(PAGE_SIZE, page, null);
            } catch (Exception ex) {
                log.error("Failed to fetch issues from Mantis (page {}): {}", page, ex.getMessage());
                break;
            }

            if (response.getIssues() == null || response.getIssues().isEmpty()) {
                break;
            }

            for (var issue : response.getIssues()) {
                try {
                    ingestionService.ingest(issue);
                    synced++;
                } catch (Exception ex) {
                    log.warn("Failed to ingest Mantis issue #{}: {}", issue.getId(), ex.getMessage());
                    errors++;
                }
            }

            if (response.getIssues().size() < PAGE_SIZE) {
                break;
            }
            page++;
        }

        log.info("Poll complete: {} synced, {} errors", synced, errors);
    }
}
