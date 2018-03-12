import jig.domain.model.diagram.DiagramConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
public class EnvironmentTest {

    @Test
    public void 必要な環境が満たされていること() {
        // 起動できればOK
    }

    @Configuration
    @ComponentScan("jig")
    static class TestConfig {

        @Bean
        DiagramConverter diagramConverter() {
            return relations -> null;
        }
    }
}
