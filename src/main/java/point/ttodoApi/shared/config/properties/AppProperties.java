package point.ttodoApi.shared.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
  /**
   * Base URL of the application (e.g., "https://ttodo.dev").
   */
  private String baseUrl = "https://ttodo.dev";

  /**
   * Email domain used for system and seed users (e.g., "ttodo.dev").
   */
  private String userDomain = "ttodo.dev";

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getUserDomain() {
    return userDomain;
  }

  public void setUserDomain(String userDomain) {
    this.userDomain = userDomain;
  }
}

