package point.zzicback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ZzicBackApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZzicBackApplication.class, args);
  }
}
