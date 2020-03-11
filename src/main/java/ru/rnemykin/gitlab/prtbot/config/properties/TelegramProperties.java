package ru.rnemykin.gitlab.prtbot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("app.telegram")
public class TelegramProperties {
    private String token;
    private String botName;

    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;
}
