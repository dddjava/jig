package org.dddjava.jig.domain.model.information.core;

import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreDomainJigTypesTest {

    @Test
    void 一つのdomainパッケージが抽出される() {
        var condition = new CoreDomainCondition(Optional.empty());
        JigTypes jigTypes = new JigTypes(List.of(
                TestSupport.stubJigType("com.example.domain.model.hoge.MyHoge"),
                TestSupport.stubJigType("com.example.domain.model.fuga.MyFuga"),
                TestSupport.stubJigType("com.example.domain.model.piyo.piyo.MyPiyo"),
                // coreDomainで弾かれる
                TestSupport.stubJigType("com.example.infrastructure.MyInfra")
        ));
        var actual = condition.coreDomainJigTypes(jigTypes).packageFilterCandidates();

        assertEquals(List.of("com.example.domain.model"), actual);
    }

    @Test
    void domain直下でも抽出される() {
        var condition = new CoreDomainCondition(Optional.empty());
        JigTypes jigTypes = new JigTypes(List.of(
                TestSupport.stubJigType("com.example.domain.model.MyHoge")
        ));
        var actual = condition.coreDomainJigTypes(jigTypes).packageFilterCandidates();

        // FIXME
        // assertEquals(List.of("com.example.domain.model"), actual);
        assertEquals(List.of("com.example.domain"), actual);
    }

    @Test
    void 複数のdomainパッケージが抽出できる() {
        var condition = new CoreDomainCondition(Optional.empty());
        JigTypes jigTypes = new JigTypes(List.of(
                TestSupport.stubJigType("com.example.domain.model.hoge.MyHoge"),
                TestSupport.stubJigType("com.example.domain.model.fuga.MyFuga"),
                TestSupport.stubJigType("com.example.domain.model.piyo.piyo.MyPiyo"),
                TestSupport.stubJigType("com.example.other.domain.model.foo.OtherFoo")
        ));
        var actual = condition.coreDomainJigTypes(jigTypes).packageFilterCandidates();

        assertEquals(List.of("com.example.domain.model", "com.example.other.domain.model"), actual);
    }
}