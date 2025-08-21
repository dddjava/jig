package testing;

import org.dddjava.jig.adapter.JigDocumentGenerator;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.dddjava.jig.infrastructure.javaproductreader.DefaultJigRepositoryFactory;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@ExtendWith(JigTest.JigTestExtension.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JigTest {

    /**
     * テストクラスの変数でテスト用のインスタンスを受け取れるようにするJUnit拡張
     */
    class JigTestExtension implements ParameterResolver {

        private final Map<Class<?>, Supplier<Object>> parameterTypeSupplierMap;

        public JigTestExtension() throws Exception {
            Path tempDir = Files.createTempDirectory("jig");
            Configuration configuration = Configuration.from(
                    new JigProperties(JigDocument.canonical(), "stub.domain.model.+", tempDir));
            parameterTypeSupplierMap = Map.of(
                    Configuration.class, () -> configuration,
                    SourceBasePaths.class, () -> TestSupport.getRawSourceLocations(),
                    JigRepository.class, () -> {
                        DefaultJigRepositoryFactory factory = DefaultJigRepositoryFactory.init(configuration);
                        return factory.createJigRepository(TestSupport.getRawSourceLocations());
                    },
                    GlossaryRepository.class, () -> configuration.glossaryRepository(),
                    JigEventRepository.class, () -> configuration.jigEventRepository(),
                    JigProperties.class, () -> configuration.properties(),
                    JigDocumentGenerator.class, () -> configuration.jigDocumentGenerator(),
                    JigService.class, () -> configuration.jigService(),
                    JigDocumentContext.class, () -> configuration.jigDocumentContext()
            );
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            Class<?> parameterType = parameterContext.getParameter().getType();
            return parameterTypeSupplierMap.containsKey(parameterType);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            Class<?> parameterType = parameterContext.getParameter().getType();
            Supplier<Object> objectSupplier = Objects.requireNonNull(parameterTypeSupplierMap.get(parameterType));
            return objectSupplier.get();
        }
    }
}
