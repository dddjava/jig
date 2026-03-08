package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.diagrams.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeKind;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngle;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmell;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmells;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.Usecase;
import org.dddjava.jig.domain.model.knowledge.validations.Validation;
import org.dddjava.jig.domain.model.knowledge.validations.Validations;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@HandleDocument
public class ListOutputAdapter {

    /**
     * 一覧出力で複数要素を文字列連結する際のコレクター
     */
    private static final Collector<CharSequence, ?, String> STREAM_COLLECTOR = Collectors.joining(", ", "[", "]");

    private final JigService jigService;
    private final TemplateEngine templateEngine;
    private final JigDocumentContext jigDocumentContext;

    public ListOutputAdapter(JigService jigService, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.ListOutput)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        InputAdapters inputAdapters = jigService.inputAdapters(repository);
        ServiceAngles serviceAngles = jigService.serviceAngles(repository);
        DatasourceAngles datasourceAngles = jigService.datasourceAngles(repository);
        MethodSmells methodSmells = jigService.methodSmells(repository);
        JigTypes jigTypes = jigService.jigTypes(repository);
        TypeRelationships allClassRelations = TypeRelationships.from(jigTypes);
        CoreTypesAndRelations coreTypesAndRelations = jigService.coreTypesAndRelations(repository);
        JigTypes coreDomainJigTypes = coreTypesAndRelations.coreJigTypes();
        JigTypes categoryTypes = jigService.categoryTypes(repository);

        JigPackages packages = jigService.packages(repository);
        Set<PackageId> coreDomainPackages = coreDomainJigTypes.stream()
                .map(JigType::packageId)
                .collect(Collectors.toUnmodifiableSet());
        List<JigPackage> jigTypePackages = packages.jigPackages().stream()
                .filter(jigPackage -> coreDomainPackages.contains(jigPackage.packageId()))
                .sorted(Comparator.comparing(JigPackage::packageId))
                .toList();
        String controllerJson = inputAdapters.listEntrypoint().stream()
                .map(this::formatControllerJson)
                .collect(Collectors.joining(",", "[", "]"));
        String serviceJson = serviceAngles.list().stream()
                .map(this::formatServiceJson)
                .collect(Collectors.joining(",", "[", "]"));
        String repositoryJson = datasourceAngles.list().stream()
                .map(this::formatRepositoryJson)
                .collect(Collectors.joining(",", "[", "]"));
        String packageJson = jigTypePackages.stream()
                .map(this::formatBusinessPackageJson)
                .collect(Collectors.joining(",", "[", "]"));
        String allJson = coreDomainJigTypes.list().stream()
                .map(jigType -> formatBusinessAllJson(jigType, coreTypesAndRelations, allClassRelations))
                .collect(Collectors.joining(",", "[", "]"));
        String enumJson = categoryTypes.list().stream()
                .map(jigType -> formatBusinessEnumJson(jigType, allClassRelations))
                .collect(Collectors.joining(",", "[", "]"));
        String collectionJson = coreDomainJigTypes.listCollectionType().stream()
                .map(jigType -> formatBusinessCollectionJson(jigType, allClassRelations))
                .collect(Collectors.joining(",", "[", "]"));
        String validationJson = Validations.from(jigTypes).list().stream()
                .map(this::formatBusinessValidationJson)
                .collect(Collectors.joining(",", "[", "]"));
        String methodSmellJson = methodSmells.list().stream()
                .map(this::formatBusinessMethodSmellJson)
                .collect(Collectors.joining(",", "[", "]"));

        String listJson = """
                {"businessRules": {"packages": %s, "all": %s, "enums": %s, "collections": %s, "validations": %s, "methodSmells": %s}, "applications": {"controllers": %s, "services": %s, "repositories": %s}}
                """.formatted(packageJson, allJson, enumJson, collectionJson, validationJson, methodSmellJson, controllerJson, serviceJson, repositoryJson);

        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());
        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "listJson", listJson
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }

    private String formatControllerJson(Entrypoint entrypoint) {
        List<String> usingFieldTypes = entrypoint.jigMethod().usingFields().jigFieldIds().stream()
                .map(JigFieldId::declaringTypeId)
                .map(TypeId::asSimpleText)
                .sorted()
                .toList();
        String usingFieldTypesText = JsonSupport.toJsonStringList(usingFieldTypes);
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "typeLabel": "%s", "usingFieldTypes": %s, "cyclomaticComplexity": %d, "path": "%s"}
                """.formatted(
                JsonSupport.escape(entrypoint.packageId().asText()),
                JsonSupport.escape(entrypoint.typeId().asSimpleText()),
                JsonSupport.escape(entrypoint.jigMethod().simpleMethodSignatureText()),
                JsonSupport.escape(entrypoint.jigMethod().returnType().simpleName()),
                JsonSupport.escape(entrypoint.jigType().label()),
                usingFieldTypesText,
                entrypoint.jigMethod().instructions().cyclomaticComplexity(),
                JsonSupport.escape(entrypoint.fullPathText()));
    }

    private String formatServiceJson(Usecase usecase) {
        String usingFieldTypesText = JsonSupport.toJsonStringList(usecase.usingFields().jigFieldIds().stream()
                .map(JigFieldId::declaringTypeId)
                .map(TypeId::asSimpleText)
                .sorted()
                .toList());
        String parameterTypeLabels = JsonSupport.toJsonStringList(usecase.serviceMethod().method().parameterTypeStream()
                .map(JigTypeReference::id)
                .map(jigDocumentContext::typeTerm)
                .map(term -> term.title())
                .toList());
        String usingServiceMethods = JsonSupport.toJsonStringList(usecase.usingServiceMethods().stream()
                .map(methodCall -> methodCall.asSignatureAndReturnTypeSimpleText())
                .toList());
        String usingRepositoryMethods = JsonSupport.toJsonStringList(usecase.usingRepositoryMethods().list().stream()
                .map(jigMethod -> jigMethod.simpleMethodSignatureText())
                .toList());
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "eventHandler": %s, "typeLabel": "%s", "methodLabel": "%s", "returnTypeLabel": "%s", "parameterTypeLabels": %s, "usingFieldTypes": %s, "cyclomaticComplexity": %d, "usingServiceMethods": %s, "usingRepositoryMethods": %s, "useNull": %s, "useStream": %s}
                """.formatted(
                JsonSupport.escape(usecase.serviceMethod().declaringType().packageId().asText()),
                JsonSupport.escape(usecase.serviceMethod().declaringType().asSimpleText()),
                JsonSupport.escape(usecase.serviceMethod().method().simpleMethodSignatureText()),
                JsonSupport.escape(usecase.serviceMethod().method().returnType().simpleName()),
                usecase.usingFromController(),
                JsonSupport.escape(jigDocumentContext.typeTerm(usecase.serviceMethod().declaringType()).title()),
                JsonSupport.escape(usecase.serviceMethod().method().aliasTextOrBlank()),
                JsonSupport.escape(jigDocumentContext.typeTerm(usecase.serviceMethod().method().returnType().id()).title()),
                parameterTypeLabels,
                usingFieldTypesText,
                usecase.serviceMethod().method().instructions().cyclomaticComplexity(),
                usingServiceMethods,
                usingRepositoryMethods,
                usecase.useNull(),
                usecase.useStream());
    }

    private String formatRepositoryJson(DatasourceAngle datasourceAngle) {
        String parameterTypeLabels = JsonSupport.toJsonStringList(datasourceAngle.methodParameterTypeStream()
                .map(JigTypeReference::id)
                .map(jigDocumentContext::typeTerm)
                .map(term -> term.title())
                .toList());
        String insertTables = JsonSupport.toJsonStringList(datasourceAngle.insertTableNames());
        String selectTables = JsonSupport.toJsonStringList(datasourceAngle.selectTableNames());
        String updateTables = JsonSupport.toJsonStringList(datasourceAngle.updateTableNames());
        String deleteTables = JsonSupport.toJsonStringList(datasourceAngle.deleteTableNames());
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "typeLabel": "%s", "returnTypeLabel": "%s", "parameterTypeLabels": %s, "cyclomaticComplexity": %d, "insertTables": %s, "selectTables": %s, "updateTables": %s, "deleteTables": %s, "callerTypeCount": %d, "callerMethodCount": %d}
                """.formatted(
                JsonSupport.escape(datasourceAngle.packageText()),
                JsonSupport.escape(datasourceAngle.typeSimpleName()),
                JsonSupport.escape(datasourceAngle.simpleMethodSignatureText()),
                JsonSupport.escape(datasourceAngle.methodReturnType().simpleNameWithGenerics()),
                JsonSupport.escape(datasourceAngle.typeLabel()),
                JsonSupport.escape(jigDocumentContext.typeTerm(datasourceAngle.methodReturnType().id()).title()),
                parameterTypeLabels,
                datasourceAngle.cyclomaticComplexity(),
                insertTables,
                selectTables,
                updateTables,
                deleteTables,
                datasourceAngle.callerMethods().typeCount(),
                datasourceAngle.callerMethods().size());
    }

    private String formatBusinessPackageJson(JigPackage jigPackage) {
        return """
                {"packageName": "%s", "packageLabel": "%s", "classCount": %d}
                """.formatted(
                JsonSupport.escape(jigPackage.packageId().asText()),
                JsonSupport.escape(jigPackage.term().title()),
                jigPackage.jigTypes().size());
    }

    private String formatBusinessAllJson(JigType jigType, CoreTypesAndRelations coreTypesAndRelations, TypeRelationships allClassRelations) {
        boolean samePackageOnly = allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).packageIds().values()
                .equals(Set.of(jigType.packageId()));
        return """
                {"packageName": "%s", "typeName": "%s", "typeLabel": "%s", "businessRuleKind": "%s", "incomingBusinessRuleCount": %d, "outgoingBusinessRuleCount": %d, "incomingClassCount": %d, "nonPublic": %s, "samePackageOnly": %s, "incomingClassList": "%s"}
                """.formatted(
                JsonSupport.escape(jigType.packageId().asText()),
                JsonSupport.escape(jigType.id().asSimpleText()),
                JsonSupport.escape(jigType.label()),
                JsonSupport.escape(jigType.toValueKind().toString()),
                coreTypesAndRelations.internalTypeRelationships().filterTo(jigType.id()).size(),
                coreTypesAndRelations.internalTypeRelationships().filterFrom(jigType.id()).size(),
                allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).list().size(),
                jigType.visibility() != JigTypeVisibility.PUBLIC,
                samePackageOnly,
                JsonSupport.escape(allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText()));
    }

    private String formatBusinessEnumJson(JigType jigType, TypeRelationships allClassRelations) {
        String constants = jigType.jigTypeMembers().enumConstantStream()
                .map(jigField -> jigField.jigFieldHeader().name())
                .collect(STREAM_COLLECTOR);
        String fields = jigType.jigTypeMembers().instanceFields().stream()
                .map(jigField -> jigField.jigFieldHeader().simpleText())
                .collect(STREAM_COLLECTOR);
        return """
                {"packageName": "%s", "typeName": "%s", "typeLabel": "%s", "constants": "%s", "fields": "%s", "usageCount": %d, "usagePlaces": "%s", "hasParameters": %s, "hasBehavior": %s, "isPolymorphic": %s}
                """.formatted(
                JsonSupport.escape(jigType.packageId().asText()),
                JsonSupport.escape(jigType.id().asSimpleText()),
                JsonSupport.escape(jigType.label()),
                JsonSupport.escape(constants),
                JsonSupport.escape(fields),
                allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).list().size(),
                JsonSupport.escape(allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText()),
                jigType.hasInstanceField(),
                jigType.hasInstanceMethod(),
                jigType.typeKind() == TypeKind.抽象列挙型);
    }

    private String formatBusinessCollectionJson(JigType jigType, TypeRelationships allClassRelations) {
        List<String> fieldTypeList = jigType.jigTypeMembers().instanceFields().stream()
                .map(jigField -> jigField.jigTypeReference().simpleNameWithGenerics())
                .toList();
        String fieldTypes = fieldTypeList.size() == 1
                ? fieldTypeList.get(0)
                : fieldTypeList.stream().collect(STREAM_COLLECTOR);
        return """
                {"packageName": "%s", "typeName": "%s", "typeLabel": "%s", "fieldTypes": "%s", "usageCount": %d, "usagePlaces": "%s", "methodCount": %d, "methods": "%s"}
                """.formatted(
                JsonSupport.escape(jigType.packageId().asText()),
                JsonSupport.escape(jigType.id().asSimpleText()),
                JsonSupport.escape(jigType.label()),
                JsonSupport.escape(fieldTypes),
                allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).size(),
                JsonSupport.escape(allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText()),
                jigType.instanceJigMethods().list().size(),
                JsonSupport.escape(jigType.instanceJigMethods().stream()
                        .map(JigMethod::simpleMethodDeclarationText)
                        .sorted()
                        .collect(STREAM_COLLECTOR)));
    }

    private String formatBusinessValidationJson(Validation validation) {
        return """
                {"packageName": "%s", "typeName": "%s", "typeLabel": "%s", "memberName": "%s", "memberType": "%s", "annotationType": "%s", "annotationDescription": "%s"}
                """.formatted(
                JsonSupport.escape(validation.typeId().packageId().asText()),
                JsonSupport.escape(validation.typeId().asSimpleText()),
                JsonSupport.escape(jigDocumentContext.typeTerm(validation.typeId()).title()),
                JsonSupport.escape(validation.memberName()),
                JsonSupport.escape(validation.memberType().asSimpleText()),
                JsonSupport.escape(validation.annotationType().asSimpleText()),
                JsonSupport.escape(validation.annotationDescription()));
    }

    private String formatBusinessMethodSmellJson(MethodSmell methodSmell) {
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "typeLabel": "%s", "notUseMember": %s, "primitiveInterface": %s, "referenceNull": %s, "nullDecision": %s, "returnsBoolean": %s, "returnsVoid": %s}
                """.formatted(
                JsonSupport.escape(methodSmell.method().declaringType().packageId().asText()),
                JsonSupport.escape(methodSmell.method().declaringType().asSimpleText()),
                JsonSupport.escape(methodSmell.method().simpleMethodSignatureText()),
                JsonSupport.escape(methodSmell.methodReturnType().asSimpleText()),
                JsonSupport.escape(methodSmell.declaringJigType().label()),
                methodSmell.notUseMember(),
                methodSmell.primitiveInterface(),
                methodSmell.referenceNull(),
                methodSmell.nullDecision(),
                methodSmell.returnsBoolean(),
                methodSmell.returnsVoid());
    }

}
