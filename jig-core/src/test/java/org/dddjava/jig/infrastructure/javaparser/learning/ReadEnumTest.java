package org.dddjava.jig.infrastructure.javaparser.learning;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.file.text.ReadableTextSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import testing.JigTestExtension;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JigTestExtension.class)
class ReadEnumTest {
    static Logger logger = LoggerFactory.getLogger(ReadEnumTest.class);

    @Test
    void test(Sources sources) throws Exception {
        ReadableTextSource source = sources.textSources().javaSources().list().stream().filter(s -> s.path().endsWith("RichEnum.java")).findAny().orElseThrow();

        CompilationUnit cu = StaticJavaParser.parse(source.toInputStream());
        // ここで出力されるものは読める
        logger.info("{}", cu.toString());

        // 定数と引数リストのPair
        class Res extends HashMap<String, List<String>> {
        }

        Res res = new Res();
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(EnumConstantDeclaration n, Void arg) {
                res.put(n.getName().asString(),
                        n.getArguments().stream()
                                .map(expression -> expression.toString())
                                .collect(Collectors.toList()));
                super.visit(n, arg);
            }

        }, null);

        assertEquals(2, res.size());
        assertEquals(List.of("\"A\""), res.get("A"));
        assertEquals(List.of("\"B\""), res.get("B"));
    }
}