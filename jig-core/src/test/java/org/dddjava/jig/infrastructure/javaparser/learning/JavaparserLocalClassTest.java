package org.dddjava.jig.infrastructure.javaparser.learning;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JavaparserLocalClassTest {

    @BeforeAll
    static void setup() {
        // デフォルト（JAVA_11）だとローカルインタフェースは通らない
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    }

    @Test
    void バージョン確認() {
        ParserConfiguration configuration = StaticJavaParser.getParserConfiguration();
        ParserConfiguration.LanguageLevel languageLevel = configuration.getLanguageLevel();

        assertEquals(ParserConfiguration.LanguageLevel.JAVA_17, languageLevel);
    }

    @Test
    void ネストクラスがparseできる() {
        StaticJavaParser.parse("""
                class Hoge {
                    class Fuga {}
                }
                """);
    }

    @Test
    void メソッドのローカルクラスがparseできる() {
        StaticJavaParser.parse("""
                class Hoge {
                    void method() {
                        class Fuga {}
                    }
                }
                """);
    }

    @Test
    void メソッドのローカルinterfaceがparseできる() {
        interface Fuga {
        }

        StaticJavaParser.parse("""
                class Hoge {
                    void method() {
                        interface Fuga {}
                    }
                }
                """);
    }

    @Test
    void メソッドのローカルenumがparseできない() {
        // コンパイルできることを示す
        enum Fuga {}

        // https://github.com/javaparser/javaparser/issues/3990
        assertThrows(ParseProblemException.class, () -> {
            StaticJavaParser.parse("""
                    class Hoge {
                        void method() {
                            enum Fuga {}
                        }
                    }
                    """);
        });
    }
}
