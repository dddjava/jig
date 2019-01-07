/**
 * GradleのtargetCompatibilityを1.8にしているので、JDK11でコンパイル／テストしてもJDK11のバイトコードを生成できない。
 * そのため別途本クラスをコンパイルした*.classをリソースとして保持しておく。
 * https://github.com/dddjava/Jig/issues/223
 */
public class CompiledJdk11NestingClass {
    class InnerClass {
    }

    class NestedClass {
    }
}
