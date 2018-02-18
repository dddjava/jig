package jig.infrastructure;

import jig.domain.model.DiagramMaker;
import jig.infrastructure.plantuml.DiagramMakerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMvcConfig {

    @Bean
    public DiagramMaker gramMaker() {
        return new DiagramMakerImpl();
    }
}
