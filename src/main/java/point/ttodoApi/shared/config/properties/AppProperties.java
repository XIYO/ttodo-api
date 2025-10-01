package point.ttodoApi.shared.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
  /**
   * Base URL of the application (e.g., "https://ttodo.dev").
   */
  private String baseUrl = "https://ttodo.dev";

  /**
   * Email domain used for system and seed users (e.g., "ttodo.dev").
   */
  private String userDomain = "ttodo.dev";
}

