package jig.shell;

import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.jdeps.RelationAnalyzer;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.infrastructure.jdeps.JdepsExecutor;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import jig.infrastructure.plantuml.PlantumlNameFormatter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "jig")
public class ShellApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShellApplication.class, args);
    }

    @Bean
    public RelationAnalyzer relationAnalyzer() {
        return new JdepsExecutor();
    }

    @Bean
    public DiagramConverter diagramConverter() {
        return new PlantumlDiagramConverter(
                new PlantumlNameFormatter(
                        new JapaneseNameDictionary()
                )
        );
    }
}
