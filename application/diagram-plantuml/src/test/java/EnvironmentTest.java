import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentTest {

    @Test
    public void test() throws Exception {
        List<String> testDotStrings = GraphvizUtils.getTestDotStrings(false);

        assertThat(testDotStrings)
                .contains("Installation seems OK. File generation OK");
    }
}
