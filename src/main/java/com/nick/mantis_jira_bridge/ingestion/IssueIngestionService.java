package com.nick.mantis_jira_bridge.ingestion;

import com.nick.mantis_jira_bridge.common.BridgeException;
import com.nick.mantis_jira_bridge.config.JiraProperties;
import com.nick.mantis_jira_bridge.jira.JiraCallService;
import com.nick.mantis_jira_bridge.jira.JiraCreateIssueResponse;
import com.nick.mantis_jira_bridge.mantis.dto.MantisIssue;
import com.nick.mantis_jira_bridge.mapping.IssueMapping;
import com.nick.mantis_jira_bridge.mapping.IssueMappingRepository;
import com.nick.mantis_jira_bridge.mapping.MantisToJiraMapper;
import com.nick.mantis_jira_bridge.mapping.SyncStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueIngestionService {

    private final IssueMappingRepository repository;
    private final JiraCallService jiraCallService;
    private final MantisToJiraMapper mapper;
    private final JiraProperties jiraProperties;

    /**
     * Idempotent: if the Mantis issue is already CREATED in Jira, returns existing result without calling Jira again.
     * On failure, persists FAILED state (transaction commits even on exception via noRollbackFor).
     */
    @Transactional(noRollbackFor = BridgeException.class)
    public SyncResult ingest(MantisIssue issue) {
        Long mantisId = issue.getId();

        var existing = repository.findByMantisIssueId(mantisId);
        if (existing.isPresent() && existing.get().getStatus() == SyncStatus.CREATED) {
            log.info("Mantis issue {} already synced as Jira {}", mantisId, existing.get().getJiraIssueKey());
            return new SyncResult(mantisId, existing.get().getJiraIssueKey());
        }

        var mapping = existing.orElseGet(() -> repository.save(
                IssueMapping.builder()
                        .mantisIssueId(mantisId)
                        .status(SyncStatus.PENDING)
                        .build()
        ));

        try {
            var request = mapper.toJiraRequest(issue, jiraProperties);
            JiraCreateIssueResponse response = jiraCallService.createIssue(request);

            var updated = mapping.toBuilder()
                    .status(SyncStatus.CREATED)
                    .jiraIssueKey(response.getKey())
                    .jiraIssueId(response.getId())
                    .lastError(null)
                    .build();
            repository.save(updated);

            log.info("Mantis issue {} synced to Jira {}", mantisId, response.getKey());
            return new SyncResult(mantisId, response.getKey());

        } catch (Exception ex) {
            int attempts = mapping.getAttemptCount() + 1;
            var failed = mapping.toBuilder()
                    .status(SyncStatus.FAILED)
                    .attemptCount(attempts)
                    .lastError(ex.getMessage())
                    .build();
            repository.save(failed);

            log.error("Failed to sync Mantis issue {} (attempt {}): {}", mantisId, attempts, ex.getMessage());
            throw new BridgeException("Jira sync failed for Mantis issue " + mantisId, ex);
        }
    }

    public record SyncResult(Long mantisIssueId, String jiraIssueKey) {}
}
