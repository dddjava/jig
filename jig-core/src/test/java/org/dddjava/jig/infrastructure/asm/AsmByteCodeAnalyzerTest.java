package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSources;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.infrastructure.DefaultLayout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyByteCodeAnalyzeContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.*;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaName;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AsmByteCodeAnalyzerTest {

    private static ProjectData projectData;

    @BeforeAll
    static void before() throws URISyntaxException {
        // 読み込む対象のソースを取得
        URI location = AsmByteCodeAnalyzerTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path value = Paths.get(location);
        LocalProject localProject = new LocalProject(new DefaultLayout(value.toString(), value.toString(), "not/read/resources", "not/read/sources"));
        ByteCodeSources byteCodeSources = localProject.getByteCodeSources();

        AsmByteCodeFactory implementationFactory = new AsmByteCodeFactory(new PropertyByteCodeAnalyzeContext());
        ByteCodes byteCodes = implementationFactory.readFrom(byteCodeSources);

        projectData = ProjectData.from(byteCodes, null);
    }

    @Test
    void 識別子() {
        assertThat(projectData.valueTypes().extract(ValueKind.IDENTIFIER).list())
                .extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(
                        SimpleIdentifier.class.getTypeName(),
                        FugaIdentifier.class.getTypeName(),
                        FugaName.class.getTypeName()
                );
    }

    @Test
    void 数値() {
        assertThat(projectData.valueTypes().extract(ValueKind.NUMBER).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleNumber.class.getTypeName());
    }

    @Test
    void 日付() {
        assertThat(projectData.valueTypes().extract(ValueKind.DATE).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleDate.class.getTypeName());
    }

    @Test
    void 期間() {
        assertThat(projectData.valueTypes().extract(ValueKind.TERM).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactly(SimpleTerm.class.getTypeName());
    }

    @Test
    void コレクション() {
        assertThat(projectData.valueTypes().extract(ValueKind.COLLECTION).list()).extracting(TypeIdentifier::fullQualifiedName)
                .containsExactlyInAnyOrder(
                        SimpleCollection.class.getTypeName(),
                        SetCollection.class.getTypeName());
    }

}
