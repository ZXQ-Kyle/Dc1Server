package space.ponyo.dc1.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages={"space.ponyo.dc1"})
public class StartTimeApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(StartTimeApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        new NettyServer().startNetty();
    }
}
