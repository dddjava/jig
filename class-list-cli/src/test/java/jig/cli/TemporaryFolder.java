package jig.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

public class TemporaryFolder implements ParameterResolver, AfterEachCallback {

    private static final Logger LOGGER = Logger.getLogger(TemporaryFolder.class.getCanonicalName());

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == Path.class;
    }

    @Override
    public Path resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> testClass = extensionContext.getTestClass().orElseThrow(AssertionError::new);
        Method testMethod = extensionContext.getTestMethod().orElseThrow(AssertionError::new);
        try {
            String prefix = testClass.getSimpleName() + "-" + testMethod.getName();
            LOGGER.info("CREATE TEMPORARY FOLDER FOR " + prefix);
            Path tempDirectory = Files.createTempDirectory(prefix);
            extensionContext.getStore(ExtensionContext.Namespace.create(TemporaryFolder.class))
                    .put(TemporaryFolder.class, tempDirectory);
            return tempDirectory;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Path path = context.getStore(ExtensionContext.Namespace.create(TemporaryFolder.class))
                .get(TemporaryFolder.class, Path.class);
        if (path != null) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) throw exc;

                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            Assertions.assertTrue(Files.notExists(path), path.toString());
        }
    }
}
