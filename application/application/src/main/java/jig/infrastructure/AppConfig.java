package jig.infrastructure;

import jig.infrastructure.plantuml.DiagramMakerImpl;
import jig.model.diagram.DiagramMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public DiagramMaker gramMaker() {
        return new DiagramMakerImpl();
    }
}
