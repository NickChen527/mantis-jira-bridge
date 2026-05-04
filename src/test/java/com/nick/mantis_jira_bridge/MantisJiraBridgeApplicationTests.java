package com.nick.mantis_jira_bridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "bridge.jira.base-url=http://jira.test",
        "bridge.jira.email=test@example.com",
        "bridge.jira.api-token=test-token",
        "bridge.jira.project-key=TEST",
        "bridge.mantis.base-url=http://mantis.test",
        "bridge.mantis.api-token=mantis-token"
})
class MantisJiraBridgeApplicationTests {

    @Test
    void contextLoads() {
    }
}
