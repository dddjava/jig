package org.dddjava.jig.infrastructure.javaparser.learning;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.javasources.ReadableTextSource;
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
class JavaparserEnumTest {
    static Logger logger = LoggerFactory.getLogger(JavaparserEnumTest.class);

    @Test
    void test(Sources sources) throws Exception {
        ReadableTextSource source = sources.textSources().javaSources().list().stream().filter(s -> s.path().endsWith("RichEnum.java")).findAny().orElseThrow();

        CompilationUnit cu = StaticJavaParser.parse(source.toInputStream());
        // ここで出力されるものは読める
        logger.info("{}", cu.toString());

        class 列挙定数と引数リスト extends HashMap<String, List<String>> {
        }

        var 列挙定数と引数リスト = new 列挙定数と引数リスト();
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(EnumDeclaration n, Void arg) {
                logger.info("{}", n);
                super.visit(n, arg);
            }

            @Override
            public void visit(EnumConstantDeclaration n, Void arg) {
                logger.info("{}", n);
                列挙定数と引数リスト.put(n.getName().asString(),
                        n.getArguments().stream()
                                .map(expression -> expression.toString())
                                .collect(Collectors.toList()));
                super.visit(n, arg);
            }

            @Override
            public void visit(ConstructorDeclaration n, Void arg) {
                logger.info("{}", n);
                logger.info("{}", n.getParameters());
                logger.info("{}", n.getParameters().stream().map(e -> e.getName()).collect(Collectors.toList()));
                super.visit(n, arg);
            }
        }, null);

        assertEquals(2, 列挙定数と引数リスト.size());
        assertEquals(List.of(
                "111",
                "\"A-String-Parameter\"",
                "a -> a"
        ), 列挙定数と引数リスト.get("A"));
        assertEquals(List.of(
                "2222",
                "\"B-String-Parameter\"",
                "(b) -> b",
                "\"B-String-Parameter-2\""
        ), 列挙定数と引数リスト.get("B"));
    }
}