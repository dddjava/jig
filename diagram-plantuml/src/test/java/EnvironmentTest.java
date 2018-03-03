import jig.infrastructure.plantuml.PlantumlDiagramMaker;
import org.junit.jupiter.api.Test;

public class EnvironmentTest {

    @Test
    public void 必要な環境が満たされていること() throws Exception {
        // コンストラクタで検証しているのでインスタンス生成が成功すればOK
        new PlantumlDiagramMaker();
    }
}
