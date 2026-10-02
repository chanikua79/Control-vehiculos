package web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"web","advanced","service","security","database","control.vehiculos","camera"})
@EnableScheduling
public class ControlVehiculosApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                ControlVehiculosApplication.class,
                args
        );
    }
}
