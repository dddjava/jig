package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.domain.basic.FileWriteFailureException;
import org.dddjava.jig.domain.basic.Text;
import org.dddjava.jig.domain.model.controllers.ControllerAngle;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.declaration.annotation.Annotation;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.type.*;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@ConditionalOnProperty(prefix = "jig.cli", name = "extra", havingValue = "api-jpa-crud")
public class ApiJpaCrudListingScript implements ExtraScript {

    static final Logger LOGGER = LoggerFactory.getLogger(ApiJpaCrudListingScript.class);

    @Value("${outputDirectory}")
    String outputDirectory;

    @Autowired
    AngleService angleService;

    @Override
    public void invoke(ProjectData projectData) {

        MethodDeclarations allRepositoryMethods = projectData.characterizedMethods().repositoryMethods();
        MethodRelations methodRelations = projectData.methodRelations();

        TypeIdentifiers repositories = projectData.repositories();
        TypeAnnotations typeAnnotations = projectData.typeAnnotations();
        Types types = projectData.types();
        Map<TypeIdentifier, String> repositoryTableMap = jpaRepositoryTableNameMap(typeAnnotations, types, repositories);

        ControllerAngles controllerAngles = angleService.controllerAngles(projectData);

        Map<MethodIdentifier, List<MethodDeclaration>> apiUseRepositoryMethodsMap = methodIdentifierListMap(allRepositoryMethods, methodRelations, controllerAngles, repositories);

        Path outputPath = Paths.get(outputDirectory, "api-jpa-crud.txt");


        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            writer.append("ハンドラメソッド")
                    .append('\t')
                    .append("PATH")
                    .append('\t')
                    .append("使用しているリポジトリのメソッド")
                    .append('\t')
                    .append("READ")
                    .append('\t')
                    .append("CREATE or UPDATE")
                    .append('\t')
                    .append("DELETE")
                    .append('\n');

            for (ControllerAngle controllerAngle : controllerAngles.list()) {
                MethodDeclaration controllerMethodDeclaration = controllerAngle.method().declaration();

                List<MethodDeclaration> repositoryMethods = apiUseRepositoryMethodsMap.getOrDefault(controllerMethodDeclaration.identifier(), Collections.emptyList());

                // READ
                String readTables = repositoryMethods.stream()
                        .filter(repositoryMethod -> {
                            String methodName = repositoryMethod.methodSignature().methodName();
                            return methodName.startsWith("find") || methodName.startsWith("get") || methodName.startsWith("exists") || methodName.startsWith("count");
                        })
                        .map(MethodDeclaration::declaringType).distinct().map(repositoryTableMap::get)
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
                        .append(controllerAngle.controllerAnnotation().pathText())
                        .append('\t')
                        .append(new MethodDeclarations(repositoryMethods).asSimpleText())
                        .append('\t')
                        .append(readTables)
                        .append('\t')
                        .append(createOrUpdateTables)
                        .append('\t')
                        .append(deleteTables)
                        .append('\n');
            }

        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }

    /**
     * {@code @Repository} の TypeIdentifier をKey、
     * {@code JpaRepository<ENTITY, ID>} のENTITYに付与された {@code @Table} の name属性をValueとしたMap
     */
    private Map<TypeIdentifier, String> jpaRepositoryTableNameMap(TypeAnnotations typeAnnotations, Types types, TypeIdentifiers repositories) {
        Map<TypeIdentifier, String> repositoryTableMap = new HashMap<>();
        for (TypeIdentifier repositoryTypeIdentifier : repositories.list()) {
            if (repositoryTableMap.containsKey(repositoryTypeIdentifier)) {
                continue;
            }

            Type repositoryType = types.get(repositoryTypeIdentifier);

            ParameterizedTypes parameterizedTypes = repositoryType.interfaceTypes();
            Optional<ParameterizedType> one = parameterizedTypes.findOne(new TypeIdentifier("org.springframework.data.jpa.repository.JpaRepository"));
            one.ifPresent(parameterizedType -> {
                TypeParameter jpaEntityType = parameterizedType.typeParameters().get(0);

                Annotation annotation = typeAnnotations.filter(jpaEntityType.typeIdentifier()).annotations().findOne(new TypeIdentifier("javax.persistence.Table"));
                String tableName = annotation.descriptionTextOf("name");
                repositoryTableMap.put(repositoryTypeIdentifier, tableName);
            });

            if (!one.isPresent()) {
                LOGGER.warn("{} is not JpaRepository.", repositoryTypeIdentifier.fullQualifiedName());
            }
        }
        return repositoryTableMap;
    }

    /**
     * ハンドラメソッド（{@code @RequestMapping}）の MethodIdentifier をKey、
     * 使用している{@code @Repository} のMethodDeclarationをValueとしたMap
     */
    private Map<MethodIdentifier, List<MethodDeclaration>> methodIdentifierListMap(MethodDeclarations repositoryMethods, MethodRelations methodRelations, ControllerAngles controllerAngles, TypeIdentifiers repositories) {
        Map<MethodIdentifier, List<MethodDeclaration>> apiUseRepositoryMethodsMap = new HashMap<>();

        for (ControllerAngle controllerAngle : controllerAngles.list()) {
            List<MethodDeclaration> collector = new ArrayList<>();
            Predicate<MethodDeclaration> isRepositoryMethod = e -> repositories.contains(e.declaringType());
            Function<MethodDeclaration, MethodDeclarations> callMethods = methodRelations::usingMethodsOf;
            collectCallRepositoryMethods(collector, isRepositoryMethod, callMethods, controllerAngle.method().declaration());

            apiUseRepositoryMethodsMap.put(controllerAngle.method().declaration().identifier(), collector);
        }
        return apiUseRepositoryMethodsMap;
    }

    private void collectCallRepositoryMethods(List<MethodDeclaration> collector,
                                              Predicate<MethodDeclaration> isRepositoryMethod,
                                              Function<MethodDeclaration, MethodDeclarations> callMethodResolver,
                                              MethodDeclaration method) {
        MethodDeclarations calledMethods = callMethodResolver.apply(method);

        for (MethodDeclaration calledMethod : calledMethods.list()) {
            // Repositoryなら追加
            if (isRepositoryMethod.test(calledMethod)) {
                collector.add(calledMethod);
            }
            // 自己呼び出しはそれ以上読まない
            if (calledMethod.equals(method)) {
                continue;
            }
            // 再帰
            collectCallRepositoryMethods(collector, isRepositoryMethod, callMethodResolver, calledMethod);
        }
    }
}
