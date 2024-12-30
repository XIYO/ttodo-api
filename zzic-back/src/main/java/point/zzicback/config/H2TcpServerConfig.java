package point.zzicback.config;

import org.h2.tools.Server;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@Profile("dev") // "dev" 프로파일에서만 활성화
public class H2TcpServerConfig implements CommandLineRunner {

    @Override
    public void run(String... args) throws SQLException {
        // H2 TCP 서버 시작
        Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-ifNotExists").start();
        System.out.println("H2 TCP server started on tcp://localhost:9092");
    }
}
