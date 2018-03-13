package sut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import sut.application.service.CanonicalService;
import sut.domain.model.fuga.Fuga;
import sut.domain.model.fuga.FugaIdentifier;

@SpringBootApplication
public class SutApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SutApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SutApplication.class, args);

        CanonicalService service = context.getBean(CanonicalService.class);
        Fuga fuga = service.fuga(new FugaIdentifier("ID-2"));
        LOGGER.info("fuga-name: {}", fuga.name());
    }
}
