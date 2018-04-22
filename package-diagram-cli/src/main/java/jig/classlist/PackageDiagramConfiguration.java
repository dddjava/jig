package jig.classlist;

import jig.diagram.graphvizj.GraphvizJavaDriver;
import jig.diagram.plantuml.PlantumlDriver;
import jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.dependency.PackageDependencyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PackageDiagramConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDiagramConfiguration.class);

    @Bean
    public PackageDependencyWriter packageDependencyWriter(@Value("${jig.diagram.writer}") String writer,
                                                           PackageIdentifierFormatter formatter,
                                                           JapaneseNameRepository repository) {
        if (writer.equals("plantuml") && hasPlantuml()) {
            return new PlantumlDriver(formatter, repository);
        }

        return new GraphvizJavaDriver(formatter, repository);
    }

    private boolean hasPlantuml() {
        try {
            Class.forName("net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("plantumlが見つからないのでgraphviz-javaで出力します");
            return false;
        }
        return true;
    }
}
