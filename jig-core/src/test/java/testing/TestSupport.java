package testing;

import org.dddjava.jig.domain.model.data.terms.*;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;
import org.dddjava.jig.infrastructure.javaproductreader.JigTypeFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TestSupport {

    public static URI defaultPackageClassURI() {
        try {
            // MEMO DefaultPackageClass を文字列でなくクラスの参照にしたいが、
            // デフォルトパッケージのクラスはデフォルトパッケージ外から参照できないため、文字列での記述としている。
            // importではnamed packageしか使用できないことはJSLに記述があるが、
            // クラスリテラルなどでunnamed packageのクラスがコンパイルエラーになると言う記述は見当たらない（Java6などにはあったようだが）
            // JEP463やJava Language Updateとかに classes in the unnamed package cannot be referenced explicitly by classes in named packages. などの記述はある
            // https://openjdk.org/jeps/463
            // デフォルトパッケージは特殊用途（小さかったり一時的だったり初期段階だったり）の利便性のためという位置付け。
            // https://docs.oracle.com/javase/specs/jls/se21/html/jls-7.html#jls-7.4.2
            var resource = TestSupport.class.getResource("/DefaultPackageClass.class");
            return Objects.requireNonNull(resource).toURI().resolve("./");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public static SourceBasePaths getRawSourceLocations() {
        return sourceLocationsFor("stub");
    }

    /**
     * jig-core の test ソースセット配下にある、単一の意図を持つ小さな fixture パッケージを解析対象にする。
     * `stub` のような巨大な共有コーパスを増やさず、テストごとに独立した入力を持たせたい場合に使う。
     *
     * @param packageRelativePath クラス出力・ソースの両方に共通するパッケージの相対パス（例: {@code "org/dddjava/jig/application/ut/domain/model"}）
     */
    public static SourceBasePaths sourceLocationsFor(String packageRelativePath) {
        return new SourceBasePaths(
                new SourceBasePath(Collections.singletonList(Paths.get(defaultPackageClassURI()).resolve(packageRelativePath))),
                new SourceBasePath(Collections.singletonList(getTestSourceRootPath().resolve(packageRelativePath)))
        );
    }

    private static final String TEST_SOURCE_ROOT_PROPERTY = "jig.core.testSourceRoot";

    /**
     * jig-core の {@code src/test/java} の絶対パス。jig-core/build.gradle の {@code test} タスクが
     * システムプロパティとして渡す。Gradle 経由の実行を前提としており、IDE から個別実行する場合は
     * このプロパティを別途渡す必要がある。
     */
    public static Path getTestSourceRootPath() {
        String configured = System.getProperty(TEST_SOURCE_ROOT_PROPERTY);
        if (configured == null) {
            throw new IllegalStateException(
                    "システムプロパティ " + TEST_SOURCE_ROOT_PROPERTY + " が未設定です。jig-core の test タスクから実行してください。");
        }
        return Paths.get(configured);
    }

    public static JigType buildJigType(Class<?> definitionClass) {
        AsmClassSourceReader sut = new AsmClassSourceReader();
        ClassDeclaration classDeclaration = sut.classDeclaration(getPathFromClass(definitionClass)).orElseThrow();
        return JigTypeFactory.createJigTypes(List.of(classDeclaration)).orderedStream().findFirst().orElseThrow();
    }

    private static Path getPathFromClass(Class<?> definitionClass) {
        var className = definitionClass.getName();
        String resourcePath = className.replace('.', '/') + ".class";
        URL url = Objects.requireNonNull(definitionClass.getResource('/' + resourcePath));
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public static JigMethod buildJigMethod(Class<?> sutClass, String methodName) {
        JigTypeMembers members = buildJigType(sutClass).jigTypeMembers();
        var matches = members.allJigMethodStream()
                .filter(jigMethod -> jigMethod.name().equals(methodName))
                .toList();
        if (matches.size() != 1) {
            throw new AssertionError("メソッド '" + methodName + "' が " + matches.size() + " 件見つかりました。一意に特定できません");
        }
        return matches.getFirst();
    }

    public static JigTypeHeader getJigTypeHeader(Class<?> clz) {
        return buildJigType(clz).jigTypeHeader();
    }

    /**
     * クラスオブジェクトからTypeIdを生成する。
     */
    public static TypeId getTypeIdFromClass(Class<?> clz) {
        return TypeId.valueOf(clz.getName());
    }

    public static JigType stubJigType(String fullyQualifiedName) {
        return stubJigType(TypeId.valueOf(fullyQualifiedName));
    }

    public static JigType stubJigType(TypeId typeId) {
        JigTypeHeader header = new JigTypeHeader(
                typeId,
                JavaTypeDeclarationKind.CLASS,
                new JigTypeAttributes(JigTypeVisibility.PUBLIC, List.of(), List.of(), List.of()),
                new JigBaseTypeDataBundle(Optional.of(JigTypeReference.fromId(TypeId.OBJECT)), List.of())
        );
        JigTypeMembers members = new JigTypeMembers(List.of(), List.of(), List.of(), List.of(), List.of());
        return new JigType(header, members);
    }

    public static Term termOf(Glossary glossary, String idText, TermKind termKind) {
        TermId termId = new TermId(idText);
        return glossary.terms().stream()
                .filter(term -> term.termKind() == termKind)
                .filter(term -> term.id().equals(termId))
                .findAny()
                .orElseGet(() -> {
                    return new Term(termId, "", "", termKind, TermOrigin.その他);
                });
    }
}
