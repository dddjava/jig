package jig;

import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import jig.infrastructure.plantuml.PlantumlNameFormatter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        System.setProperty("PLANTUML_LIMIT_SIZE", "65536");
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public DiagramConverter diagramConverter() {
        PlantumlNameFormatter nameFormatter = new PlantumlNameFormatter(new JapaneseNameDictionary());
        return new PlantumlDiagramConverter(nameFormatter);
    }
}
