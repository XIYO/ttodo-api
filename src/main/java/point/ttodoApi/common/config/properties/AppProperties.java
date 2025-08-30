package point.ttodoApi.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    /**
     * Email domain used for system and seed users (e.g., "ttodo.dev").
     */
    private String userDomain = "ttodo.dev";

    public String getUserDomain() {
        return userDomain;
    }

    public void setUserDomain(String userDomain) {
        this.userDomain = userDomain;
    }
}

