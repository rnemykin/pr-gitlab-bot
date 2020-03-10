package ru.rnemykin.gitlab.prtbot.config;

import org.gitlab4j.api.GitLabApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.rnemykin.gitlab.prtbot.config.properties.GitLabProperties;

import java.util.logging.Level;

@Configuration
public class GitLabClientConfiguration {
    @Bean
    public GitLabApi gitLabClient(GitLabProperties properties) {
        GitLabApi gitLabApi = new GitLabApi(properties.getUrl(), properties.getToken());
        gitLabApi.enableRequestResponseLogging(Level.ALL);
        return gitLabApi;
    }
}
