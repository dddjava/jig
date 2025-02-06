package testing;

import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.JigTypeTerms;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.classsources.ClassDeclaration;
import org.dddjava.jig.domain.model.sources.classsources.ClassSource;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceBasePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceBasePaths;
import org.dddjava.jig.infrastructure.asm.AsmClassSourceReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
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

    private static ClassSource newClassSource(Path path, String className) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new ClassSource(bytes, className);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static ClassSource getClassSource(Class<?> clz) {
        var className = clz.getName();
        String resourcePath = className.replace('.', '/') + ".class";
        URL url = Objects.requireNonNull(clz.getResource('/' + resourcePath));
        try {
            Path path = Paths.get(url.toURI());
            return newClassSource(path, className);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static SourceBasePaths getRawSourceLocations() {
        return new SourceBasePaths(
                new ClassSourceBasePaths(Collections.singletonList(Paths.get(defaultPackageClassURI()).resolve("stub"))),
                new JavaSourceBasePaths(Collections.singletonList(getModuleRootPath().resolve("src").resolve("test").resolve("java").resolve("stub")))
        );
    }

    public static JigType buildJigType(Class<?> definitionClass) {
        AsmClassSourceReader sut = new AsmClassSourceReader();
        ClassDeclaration classDeclaration = sut.classDeclaration(getClassSource(definitionClass)).orElseThrow();
        return JigType.from(
                classDeclaration.jigTypeHeader(),
                classDeclaration.jigMemberBuilder().buildStaticMember(),
                classDeclaration.jigMemberBuilder().buildInstanceMember(),
                new JigTypeTerms(List.of())
        );
    }
}
