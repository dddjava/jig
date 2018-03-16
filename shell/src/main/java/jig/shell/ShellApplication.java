package jig.shell;

import jig.application.service.DiagramService;
import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.jdeps.RelationAnalyzer;
import jig.infrastructure.jdeps.JdepsExecutor;
import jig.infrastructure.plantuml.DiagramRepositoryImpl;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import jig.infrastructure.plantuml.PlantumlDiagramMaker;
import jig.infrastructure.plantuml.PlantumlNameFormatter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication(scanBasePackages = "jig")
public class ShellApplication {

    public static void main(String[] args) {
        System.setProperty("PLANTUML_LIMIT_SIZE", "65536");
        SpringApplication.run(ShellApplication.class, args);
    }

    @Bean
    public RelationAnalyzer relationAnalyzer() {
        return new JdepsExecutor();
    }

    @Bean
    public DiagramConverter diagramConverter(JapaneseNameRepository japaneseNameRepository) {
        return new PlantumlDiagramConverter(
                new PlantumlNameFormatter(),
                japaneseNameRepository);
    }

    @Bean
    @Primary
    public DiagramService getDiagramService(DiagramConverter diagramConverter) {
        return new DiagramService(
                new DiagramRepositoryImpl(),
                new PlantumlDiagramMaker(),
                diagramConverter);
    }
}
