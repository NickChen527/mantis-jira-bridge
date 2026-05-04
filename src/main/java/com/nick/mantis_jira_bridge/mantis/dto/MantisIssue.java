package com.nick.mantis_jira_bridge.mantis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MantisIssue {

    private Long id;
    private String summary;
    private String description;
    private MantisEnum priority;
    private MantisEnum severity;
    private MantisEnum status;
    private MantisEnum category;
    private MantisProject project;
    private MantisAccount reporter;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MantisEnum {
        private Integer id;
        private String name;
        private String label;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MantisProject {
        private Long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MantisAccount {
        private Long id;
        private String name;
        @JsonProperty("real_name")
        private String realName;
    }
}
