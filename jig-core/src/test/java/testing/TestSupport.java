package testing;

import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.domain.model.sources.SourceBasePath;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.classsources.ClassFile;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;
import org.dddjava.jig.infrastructure.javaproductreader.JigTypeFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TestSupport {

    public static URL[] getTestResourceRootURLs() {
        try {
            URL classRootUrl = defaultPackageClassURI().toURL();
            URL resourceRootUrl = resourceRootURI().toURL();
            return new URL[]{classRootUrl, resourceRootUrl};
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public static Path getModuleRootPath() {
        URI uri = defaultPackageClassURI();
        Path path = Paths.get(uri).toAbsolutePath();

        while (!path.endsWith("jig-core")) {
            path = path.getParent();
            if (path == null) {
                throw new IllegalStateException("モジュール名変わった？");
            }
        }
        return path;
    }

    public static URI defaultPackageClassURI() {
        try {
            return TestSupport.class.getResource("/DefaultPackageClass.class").toURI().resolve("./");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public static URI resourceRootURI() {
        try {
            return TestSupport.class.getResource("/marker.properties").toURI().resolve("./");
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public static ClassFile getClassSource(Class<?> clz) {
        var className = clz.getName();
        String resourcePath = className.replace('.', '/') + ".class";
        URL url = Objects.requireNonNull(clz.getResource('/' + resourcePath));
        try {
            Path path = Paths.get(url.toURI());
            return ClassFile.readFromPath(path);
        } catch (URISyntaxException | IOException e) {
            throw new AssertionError(e);
        }
    }

    public static SourceBasePaths getRawSourceLocations() {
        return new SourceBasePaths(
                new SourceBasePath(Collections.singletonList(Paths.get(defaultPackageClassURI()).resolve("stub"))),
                new SourceBasePath(Collections.singletonList(getTestSourceRootPath().resolve("stub")))
        );
    }

    public static Path getTestSourceRootPath() {
        return getModuleRootPath().resolve("src").resolve("test").resolve("java");
    }

    public static JigType buildJigType(Class<?> definitionClass) {
        AsmClassSourceReader sut = new AsmClassSourceReader();
        ClassDeclaration classDeclaration = sut.classDeclaration(getClassSource(definitionClass)).orElseThrow();
        return JigTypeFactory.createJigTypes(List.of(classDeclaration), new Glossary(List.of())).orderedStream().findFirst().orElseThrow();
    }

    private static JigMethodDeclaration JigMethodDeclaration準備(Class<?> sutClass, String methodName) {
        var members = buildJigType(sutClass).jigTypeMembers();
        return members.allJigMethodStream()
                .map(JigMethod::jigMethodDeclaration)
                .filter(jigMethodDeclaration -> jigMethodDeclaration.name().equals(methodName))
                .findAny().orElseThrow();
    }

    public static JigMethod JigMethod準備(Class<?> sutClass, String methodName) {
        JigTypeMembers members = buildJigType(sutClass).jigTypeMembers();
        return members.allJigMethodStream()
                .filter(jigMethod -> jigMethod.name().equals(methodName))
                .findFirst()
                .orElseThrow();
    }

    public static JigTypeHeader getJigTypeHeader(Class<?> clz) {
        return buildJigType(clz).jigTypeHeader();
    }
}
