package com.nick.mantis_jira_bridge.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "bridge.mantis")
public class MantisProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String apiToken;

    private String webhookSecret = "";

    private Poll poll = new Poll();

    @Getter
    @Setter
    public static class Poll {
        private boolean enabled = false;
        private long intervalMs = 300_000;
    }
}
