package com.nick.mantis_jira_bridge.mapping;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class IssueMappingRepositoryTest {

    @Autowired
    private IssueMappingRepository repository;

    @Test
    void findByMantisIssueId_returnsExistingMapping() {
        var saved = repository.save(IssueMapping.builder()
                .mantisIssueId(100L)
                .status(SyncStatus.CREATED)
                .jiraIssueKey("PROJ-1")
                .build());

        var found = repository.findByMantisIssueId(100L);

        assertThat(found).isPresent();
        assertThat(found.get().getJiraIssueKey()).isEqualTo("PROJ-1");
        assertThat(found.get().getStatus()).isEqualTo(SyncStatus.CREATED);
    }

    @Test
    void findByMantisIssueId_returnsEmptyWhenNotFound() {
        var found = repository.findByMantisIssueId(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void uniqueConstraint_preventsDoubleSave() {
        repository.save(IssueMapping.builder()
                .mantisIssueId(200L)
                .status(SyncStatus.PENDING)
                .build());
        repository.flush();

        var duplicate = IssueMapping.builder()
                .mantisIssueId(200L)
                .status(SyncStatus.PENDING)
                .build();

        assertThatThrownBy(() -> {
            repository.saveAndFlush(duplicate);
        });
    }

    @Test
    void toBuilder_producesNewInstance() {
        var original = repository.save(IssueMapping.builder()
                .mantisIssueId(300L)
                .status(SyncStatus.PENDING)
                .build());

        var updated = original.toBuilder()
                .status(SyncStatus.CREATED)
                .jiraIssueKey("PROJ-5")
                .build();

        assertThat(updated.getStatus()).isEqualTo(SyncStatus.CREATED);
        assertThat(original.getStatus()).isEqualTo(SyncStatus.PENDING);
    }
}
