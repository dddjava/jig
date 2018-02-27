package jig.infrastructure.plantuml;

import jig.domain.model.diagram.DiagramMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public DiagramMaker gramMaker() {
        return new DiagramMakerImpl();
    }
}
