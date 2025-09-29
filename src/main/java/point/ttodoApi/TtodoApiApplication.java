package point.ttodoApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import point.ttodoApi.shared.config.properties.CorsProperties;

@SpringBootApplication
@EnableConfigurationProperties(CorsProperties.class)
public class TtodoApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(TtodoApiApplication.class, args);
  }
}
