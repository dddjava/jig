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

        MethodDeclarations repositoryMethods = projectData.characterizedMethods().repositoryMethods();
        MethodRelations methodRelations = projectData.methodRelations();

        MethodDeclarations controllerMethods = projectData.controllerMethods().declarations();

        Map<MethodIdentifier, List<MethodDeclaration>> apiUseRepositoryMethodsMap = methodIdentifierListMap(repositoryMethods, methodRelations, controllerMethods);

        output(apiUseRepositoryMethodsMap, projectData);
    }

    private void output(Map<MethodIdentifier, List<MethodDeclaration>> apiMap, ProjectData projectData) {
        Path outputPath = Paths.get(outputDirectory, "api-jpa-crud.txt");

        ControllerAngles controllerAngles = angleService.controllerAngles(projectData);

        TypeAnnotations typeAnnotations = projectData.typeAnnotations();
        Types types = projectData.types();

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

                List<MethodDeclaration> repositoryMethods = apiMap.getOrDefault(controllerMethodDeclaration.identifier(), Collections.emptyList());

                Map<TypeIdentifier, String> repositoryTableMap = jpaRepositoryTableNameMap(typeAnnotations, types, repositoryMethods);

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
    private Map<TypeIdentifier, String> jpaRepositoryTableNameMap(TypeAnnotations typeAnnotations, Types types, List<MethodDeclaration> repositoryMethods) {
        Map<TypeIdentifier, String> repositoryTableMap = new HashMap<>();
        for (MethodDeclaration repositoryMethod : repositoryMethods) {
            TypeIdentifier repositoryTypeIdentifier = repositoryMethod.declaringType();
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
    private Map<MethodIdentifier, List<MethodDeclaration>> methodIdentifierListMap(MethodDeclarations repositoryMethods, MethodRelations methodRelations, MethodDeclarations controllerMethods) {
        Map<MethodIdentifier, List<MethodDeclaration>> apiUseRepositoryMethodsMap = new HashMap<>();

        for (MethodDeclaration repositoryMethod : repositoryMethods.list()) {
            List<MethodDeclaration> userControllerMethods = new ArrayList<>();

            collectControllerMethods(userControllerMethods, repositoryMethod, methodRelations, controllerMethods);

            for (MethodDeclaration userControllerMethod : userControllerMethods) {
                MethodIdentifier identifier = userControllerMethod.identifier();
                apiUseRepositoryMethodsMap.putIfAbsent(identifier, new ArrayList<>());
                apiUseRepositoryMethodsMap.get(identifier).add(repositoryMethod);
            }
        }
        return apiUseRepositoryMethodsMap;
    }

    /**
     * メソッドの呼び出し元をControllerまで探索していく
     */
    private void collectControllerMethods(List<MethodDeclaration> collector, MethodDeclaration methodDeclaration, MethodRelations methodRelations, MethodDeclarations controllerMethods) {
        MethodDeclarations methodDeclarations = methodRelations.userMethodsOf(methodDeclaration);
        List<MethodDeclaration> list = methodDeclarations.list();
        for (MethodDeclaration userMethod : list) {
            if (controllerMethods.contains(userMethod)) {
                // controllerなら追加
                collector.add(userMethod);
            } else if ( // 無限ループ対策（完全ではない）
                    !methodDeclaration.equals(userMethod)) {
                // controllerじゃない場合は再帰
                collectControllerMethods(collector, userMethod, methodRelations, controllerMethods);
            }
        }
    }
}
