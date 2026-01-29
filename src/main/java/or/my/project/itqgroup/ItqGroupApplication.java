package or.my.project.itqgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ItqGroupApplication  {

    public static void main(String[] args) {
        SpringApplication.run(ItqGroupApplication.class, args);
    }

}
