package org.dddjava.jig.contract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 同じ入力からは同じ成果物が得られることの契約。
 *
 * 生成順序や並列処理に依存した揺れが入ると、利用者は差分レビューで無関係な変更を読むことになる。
 */
class SiteDeterminismContractTest {

    @Test
    void 同じ入力を二度解析しても成果物が変わらない(@TempDir Path workDirectory) {
        Path first = workDirectory.resolve("first");
        Path second = workDirectory.resolve("second");

        ShowcaseSite.generateTo(first);
        ShowcaseSite.generateTo(second);

        assertEquals(
                GeneratedSite.normalizedContents(first, GeneratedSite.MEASUREMENTS),
                GeneratedSite.normalizedContents(second, GeneratedSite.MEASUREMENTS));
    }
}
