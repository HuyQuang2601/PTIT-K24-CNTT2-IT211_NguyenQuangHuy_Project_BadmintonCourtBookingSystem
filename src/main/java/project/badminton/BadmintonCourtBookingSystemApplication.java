package project.badminton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class BadmintonCourtBookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadmintonCourtBookingSystemApplication.class, args);
    }

}
