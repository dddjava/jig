package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.domain.basic.FileWriteFailureException;
import org.dddjava.jig.domain.basic.Text;
import org.dddjava.jig.domain.model.controllers.ControllerAngle;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.type.*;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@ConditionalOnProperty(prefix = "jig.cli", name = "extra", havingValue = "api-jpa-crud")
public class ApiJpaCrudListingScript implements ExtraScript {

    static final Logger LOGGER = LoggerFactory.getLogger(ApiJpaCrudListingScript.class);

    @Autowired
    CliConfig cliConfig;

    @Override
    public void invoke(ProjectData projectData) {
        Configuration configuration = cliConfig.configuration();
        AngleService angleService = configuration.angleService();

        MethodRelations methodRelations = projectData.methodRelations();

        TypeIdentifiers repositories = projectData.repositories();
        TypeAnnotations typeAnnotations = projectData.typeAnnotations();

        Map<TypeIdentifier, String> entityTableMap = jpaEntityTableNameMap(typeAnnotations);
        Types types = projectData.types();
        Map<TypeIdentifier, String> repositoryTableMap = jpaRepositoryTableNameMap(types, repositories, entityTableMap);


        ControllerAngles controllerAngles = angleService.controllerAngles(projectData);

        Function<MethodDeclaration, MethodDeclarations> callMethodsResolver = methodRelations::usingMethodsOf;

        // @Repositoryのメソッドを収集対象にする
        Predicate<MethodDeclaration> isRepositoryMethod = e -> repositories.contains(e.declaringType());
        Map<MethodIdentifier, List<MethodDeclaration>> apiUseRepositoryMethodsMap = methodIdentifierListMap(controllerAngles, isRepositoryMethod, callMethodsResolver);

        // @Entityの他entity参照メソッドを収集対象にする
        MethodAnnotations methodAnnotations = projectData.methodAnnotations();
        Predicate<MethodDeclaration> isJpaEntityFetchMethod = e -> {
            Optional<MethodAnnotation> mayBeMethodAnnotation = methodAnnotations.findOne(e);
            return mayBeMethodAnnotation.map(
                    methodAnnotation ->
                            methodAnnotation.annotationType().equals(new TypeIdentifier("javax.persistence.OneToOne"))
                                    || methodAnnotation.annotationType().equals(new TypeIdentifier("javax.persistence.ManyToOne"))
                    // TODO XxxToMany の場合は戻り値のジェネリクス対応後になる
                    // || methodAnnotation.annotationType().equals(new TypeIdentifier("javax.persistence.OneToMany"))
                    // || methodAnnotation.annotationType().equals(new TypeIdentifier("javax.persistence.ManyToMany"))
            ).filter(b -> b).isPresent();
        };
        Map<MethodIdentifier, List<MethodDeclaration>> apiUseJpaEntityFetchMethodsMap = methodIdentifierListMap(controllerAngles, isJpaEntityFetchMethod, callMethodsResolver);

        Path outputPath = cliConfig.outputDirectory().resolve("api-jpa-crud.txt");


        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            writer.append("ハンドラメソッド")
                    .append('\t')
                    .append("PATH")
                    .append('\t')
                    .append("使用しているリポジトリのメソッド")
                    .append('\t')
                    .append("READ")
                    .append('\t')
                    .append("READ(EntityMethod)")
                    .append('\t')
                    .append("CREATE or UPDATE")
                    .append('\t')
                    .append("DELETE")
                    .append('\n');

            for (ControllerAngle controllerAngle : controllerAngles.list()) {
                MethodDeclaration controllerMethodDeclaration = controllerAngle.method().declaration();

                List<MethodDeclaration> repositoryMethods = apiUseRepositoryMethodsMap.get(controllerMethodDeclaration.identifier());

                // READ
                String readTables = repositoryMethods.stream()
                        .filter(repositoryMethod -> {
                            String methodName = repositoryMethod.methodSignature().methodName();
                            return methodName.startsWith("find") || methodName.startsWith("get") || methodName.startsWith("exists") || methodName.startsWith("count");
                        })
                        .map(MethodDeclaration::declaringType).distinct().map(repositoryTableMap::get)
                        .collect(Text.collectionCollector());
                // READ(EntityMethod)
                String lazyReadTables = apiUseJpaEntityFetchMethodsMap.get(controllerMethodDeclaration.identifier()).stream()
                        .map(MethodDeclaration::returnType)
                        .distinct().map(entityTableMap::get)
                        .collect(Text.collectionCollector());
                // CREATE or UPDATE
                String createOrUpdateTables = repositoryMethods.stream()
                        .filter(repositoryMethod -> repositoryMethod.methodSignature().methodName().startsWith("save"))
                        .map(MethodDeclaration::declaringType).distinct().map(repositoryTableMap::get)
                        .collect(Text.collectionCollector());
                // DELETE
                String deleteTables = repositoryMethods.stream()
                        .filter(repositoryMethod -> repositoryMethod.methodSignature().methodName().startsWith("delete"))
                        .map(MethodDeclaration::declaringType).distinct().map(repositoryTableMap::get)
                        .collect(Text.collectionCollector());

                writer.append(controllerMethodDeclaration.asFullNameText())
                        .append('\t')
                        .append(controllerAngle.requestHandler().pathText())
                        .append('\t')
                        .append(new MethodDeclarations(repositoryMethods).asSimpleText())
                        .append('\t')
                        .append(readTables)
                        .append('\t')
                        .append(lazyReadTables)
                        .append('\t')
                        .append(createOrUpdateTables)
                        .append('\t')
                        .append(deleteTables)
                        .append('\n');
            }

        } catch (
                IOException e) {
            throw new FileWriteFailureException(e);
        }

    }

    private Map<TypeIdentifier, String> jpaEntityTableNameMap(TypeAnnotations typeAnnotations) {
        Map<TypeIdentifier, String> map = new HashMap<>();
        for (TypeAnnotation typeAnnotation : typeAnnotations.list()) {
            if (!typeAnnotation.typeIs(new TypeIdentifier("javax.persistence.Table"))) {
                continue;
            }
            map.put(typeAnnotation.declaringType(), typeAnnotation.annotation().descriptionTextOf("name"));
        }
        return map;
    }

    /**
     * {@code @Repository} の TypeIdentifier をKey、
     * {@code JpaRepository<ENTITY, ID>} のENTITYに付与された {@code @Table} の name属性をValueとしたMap
     */
    private Map<TypeIdentifier, String> jpaRepositoryTableNameMap(Types types, TypeIdentifiers repositories, Map<TypeIdentifier, String> entityTableMap) {
        Map<TypeIdentifier, String> repositoryTableMap = new HashMap<>();
        for (TypeIdentifier repositoryTypeIdentifier : repositories.list()) {
            if (repositoryTableMap.containsKey(repositoryTypeIdentifier)) {
                continue;
            }

            Type repositoryType = types.get(repositoryTypeIdentifier);

            ParameterizedTypes parameterizedTypes = repositoryType.interfaceTypes();
            Optional<ParameterizedType> one = parameterizedTypes.findOne(new TypeIdentifier("org.springframework.data.jpa.repository.JpaRepository"));
            one.ifPresent(parameterizedType -> {
                // 一つ目がENTITY
                TypeParameter jpaEntityType = parameterizedType.typeParameters().get(0);
                repositoryTableMap.put(repositoryTypeIdentifier, entityTableMap.get(jpaEntityType.typeIdentifier()));
            });

            if (!one.isPresent()) {
                LOGGER.warn("{} is not JpaRepository.", repositoryTypeIdentifier.fullQualifiedName());
            }
        }
        return repositoryTableMap;
    }

    /**
     * ハンドラメソッド（{@code @RequestMapping}）の MethodIdentifier をKey、
     * isCollectTargetMethodに合致したMethodDeclarationをValueとしたMap
     */
    private Map<MethodIdentifier, List<MethodDeclaration>> methodIdentifierListMap(ControllerAngles controllerAngles, Predicate<MethodDeclaration> isCollectTargetMethod, Function<MethodDeclaration, MethodDeclarations> callMethodsResolver) {
        Map<MethodIdentifier, List<MethodDeclaration>> apiUseRepositoryMethodsMap = new HashMap<>();

        for (ControllerAngle controllerAngle : controllerAngles.list()) {
            List<MethodDeclaration> collector = new ArrayList<>();
            collectMethods(collector, isCollectTargetMethod, callMethodsResolver, controllerAngle.method().declaration());

            apiUseRepositoryMethodsMap.put(controllerAngle.method().declaration().identifier(), collector);
        }
        return apiUseRepositoryMethodsMap;
    }

    private void collectMethods(List<MethodDeclaration> collector,
                                Predicate<MethodDeclaration> isCollectTargetMethod,
                                Function<MethodDeclaration, MethodDeclarations> callMethodResolver,
                                MethodDeclaration method) {
        MethodDeclarations calledMethods = callMethodResolver.apply(method);

        for (MethodDeclaration calledMethod : calledMethods.list()) {
            // Repositoryなら追加
            if (isCollectTargetMethod.test(calledMethod)) {
                collector.add(calledMethod);
            }
            // 自己呼び出しはそれ以上読まない
            if (calledMethod.equals(method)) {
                continue;
            }
            // 再帰
            collectMethods(collector, isCollectTargetMethod, callMethodResolver, calledMethod);
        }
    }
}
