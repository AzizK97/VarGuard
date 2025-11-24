package tn.rnu.eniso.fwk.scan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "tn.rnu.eniso.fwk.scan",
        "tn.rnu.eniso.fwk.scan.core"
})
@EntityScan("tn.rnu.eniso.fwk.scan.core.infra.model")
@EnableJpaRepositories("tn.rnu.eniso.fwk.scan.core.dal.repository")
public class ScanApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScanApplication.class, args);
    }

}
