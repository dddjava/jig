package learning.javaparser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaparserLearningTest {

    @Test
    void name() {
        JavaParser javaParser = new JavaParser();

        ParseResult<CompilationUnit> parseResult = javaParser.parse("""
                enum TestEnum {
                    A, B;
                    int i;
                }
                """);
        assertTrue(parseResult.isSuccessful());

        CompilationUnit compilationUnit = parseResult.getResult().orElseThrow();

        interface TestMock {
            void assertion(String message);
        }
        TestMock testMock = Mockito.mock(TestMock.class);

        compilationUnit.accept(new VoidVisitorAdapter<>() {
            @Override
            public void visit(EnumDeclaration node, TestMock mock) {
                mock.assertion("name: %s".formatted(node.getNameAsString()));

                // ここで super を呼ばないと他の visit が実行されない
                super.visit(node, mock);
            }

            @Override
            public void visit(FieldDeclaration n, TestMock mock) {
                NodeList<VariableDeclarator> variables = n.getVariables();
                mock.assertion("fields: %d".formatted(variables.size()));

                VariableDeclarator variableDeclarator = variables.getFirst().orElseThrow();
                mock.assertion("field name: %s".formatted(variableDeclarator.getNameAsString()));

                super.visit(n, mock);
            }

            @Override
            public void visit(EnumConstantDeclaration n, TestMock mock) {
                mock.assertion("enum constant name: %s".formatted(n.getNameAsString()));

                super.visit(n, mock);
            }
        }, testMock);


        Mockito.verify(testMock).assertion("name: TestEnum");
        Mockito.verify(testMock).assertion("fields: 1");
        Mockito.verify(testMock).assertion("field name: i");
        Mockito.verify(testMock).assertion("enum constant name: A");
        Mockito.verify(testMock).assertion("enum constant name: B");
    }
}
