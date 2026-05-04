package com.nick.mantis_jira_bridge.mapping;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IssueMappingRepository extends JpaRepository<IssueMapping, Long> {

    Optional<IssueMapping> findByMantisIssueId(Long mantisIssueId);
}
