package org.dddjava.jig.cli;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.domain.basic.FileWriteFailureException;
import org.dddjava.jig.domain.basic.Text;
import org.dddjava.jig.domain.model.controllers.ControllerAngle;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
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

    @Value("${outputDirectory}")
    String outputDirectory;

    @Autowired
    AngleService angleService;

    @Override
    public void invoke(ProjectData projectData) {

        MethodDeclarations repositoryMethods = projectData.characterizedMethods().repositoryMethods();
        MethodRelations methodRelations = projectData.methodRelations();

        MethodDeclarations controllerMethods = projectData.controllerMethods().declarations();

        Map<MethodIdentifier, List<MethodDeclaration>> apiMap = new HashMap<>();

        for (MethodDeclaration repositoryMethod : repositoryMethods.list()) {
            List<MethodDeclaration> userControllerMethods = new ArrayList<>();

            collectControllerMethods(userControllerMethods, repositoryMethod, methodRelations, controllerMethods);

            for (MethodDeclaration userControllerMethod : userControllerMethods) {
                MethodIdentifier identifier = userControllerMethod.identifier();
                apiMap.putIfAbsent(identifier, new ArrayList<>());
                apiMap.get(identifier).add(repositoryMethod);
            }
        }

        output(apiMap, projectData);
    }

    private void output(Map<MethodIdentifier, List<MethodDeclaration>> apiMap, ProjectData projectData) {
        Path outputPath = Paths.get(outputDirectory, "api-jpa-crud.txt");

        ControllerAngles controllerAngles = angleService.controllerAngles(projectData);

        TypeAnnotations typeAnnotations = projectData.typeAnnotations();

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            writer.append("ハンドラメソッド").append('\t')
                    .append("PATH").append('\t')
                    .append("使用しているリポジトリのメソッド").append('\t')
                    .append("READ")
                    .append('\n');

            for (ControllerAngle controllerAngle : controllerAngles.list()) {
                MethodDeclaration controllerMethodDeclaration = controllerAngle.method().declaration();

                List<MethodDeclaration> repositoryMethods = apiMap.getOrDefault(controllerMethodDeclaration.identifier(), Collections.emptyList());

                // TODO この形だとexistsやcountが無理なのでJpaRepository<T, ID>のTをとりたい。
                // READ
                String readTables = repositoryMethods.stream()
                        .filter(repositoryMethod -> repositoryMethod.methodSignature().methodName().startsWith("find"))
                        .map(repositoryMethod -> {
                            // 戻り値についているTableアノテーションを読む
                            TypeIdentifier typeIdentifier = repositoryMethod.returnType();
                            Annotations annotations = typeAnnotations.filter(typeIdentifier).annotations();
                            List<String> tableNames = annotations.descriptionTextsOf("name");
                            return tableNames;
                        })
                        .flatMap(List::stream)
                        .distinct()
                        .collect(Text.collectionCollector());

                writer.append(controllerMethodDeclaration.asFullNameText())
                        .append('\t')
                        .append(controllerAngle.controllerAnnotation().pathText())
                        .append('\t')
                        .append(new MethodDeclarations(repositoryMethods).asSimpleText())
                        .append('\t')
                        .append(readTables)
                        .append('\n');
            }

        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }

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
