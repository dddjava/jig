package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeId;
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
        String usingFieldTypesText = toJsonStringList(usingFieldTypes);
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "typeLabel": "%s", "usingFieldTypes": %s, "cyclomaticComplexity": %d, "path": "%s"}
                """.formatted(
                escape(entrypoint.packageId().asText()),
                escape(entrypoint.typeId().asSimpleText()),
                escape(entrypoint.jigMethod().simpleMethodSignatureText()),
                escape(entrypoint.jigMethod().returnType().simpleName()),
                escape(entrypoint.jigType().label()),
                usingFieldTypesText,
                entrypoint.jigMethod().instructions().cyclomaticComplexity(),
                escape(entrypoint.fullPathText()));
    }

    private String formatServiceJson(Usecase usecase) {
        String usingFieldTypesText = toJsonStringList(usecase.usingFields().jigFieldIds().stream()
                .map(JigFieldId::declaringTypeId)
                .map(TypeId::asSimpleText)
                .sorted()
                .toList());
        String parameterTypeLabels = toJsonStringList(usecase.serviceMethod().method().parameterTypeStream()
                .map(JigTypeReference::id)
                .map(jigDocumentContext::typeTerm)
                .map(term -> term.title())
                .toList());
        String usingServiceMethods = toJsonStringList(usecase.usingServiceMethods().stream()
                .map(methodCall -> methodCall.asSignatureAndReturnTypeSimpleText())
                .toList());
        String usingRepositoryMethods = toJsonStringList(usecase.usingRepositoryMethods().list().stream()
                .map(jigMethod -> jigMethod.simpleMethodSignatureText())
                .toList());
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "eventHandler": %s, "typeLabel": "%s", "methodLabel": "%s", "returnTypeLabel": "%s", "parameterTypeLabels": %s, "usingFieldTypes": %s, "cyclomaticComplexity": %d, "usingServiceMethods": %s, "usingRepositoryMethods": %s, "useNull": %s, "useStream": %s}
                """.formatted(
                escape(usecase.serviceMethod().declaringType().packageId().asText()),
                escape(usecase.serviceMethod().declaringType().asSimpleText()),
                escape(usecase.serviceMethod().method().simpleMethodSignatureText()),
                escape(usecase.serviceMethod().method().returnType().simpleName()),
                usecase.usingFromController(),
                escape(jigDocumentContext.typeTerm(usecase.serviceMethod().declaringType()).title()),
                escape(usecase.serviceMethod().method().aliasTextOrBlank()),
                escape(jigDocumentContext.typeTerm(usecase.serviceMethod().method().returnType().id()).title()),
                parameterTypeLabels,
                usingFieldTypesText,
                usecase.serviceMethod().method().instructions().cyclomaticComplexity(),
                usingServiceMethods,
                usingRepositoryMethods,
                usecase.useNull(),
                usecase.useStream());
    }

    private String formatRepositoryJson(DatasourceAngle datasourceAngle) {
        String parameterTypeLabels = toJsonStringList(datasourceAngle.methodParameterTypeStream()
                .map(JigTypeReference::id)
                .map(jigDocumentContext::typeTerm)
                .map(term -> term.title())
                .toList());
        String insertTables = toJsonStringList(datasourceAngle.insertTableNames());
        String selectTables = toJsonStringList(datasourceAngle.selectTableNames());
        String updateTables = toJsonStringList(datasourceAngle.updateTableNames());
        String deleteTables = toJsonStringList(datasourceAngle.deleteTableNames());
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "typeLabel": "%s", "returnTypeLabel": "%s", "parameterTypeLabels": %s, "cyclomaticComplexity": %d, "insertTables": %s, "selectTables": %s, "updateTables": %s, "deleteTables": %s, "callerTypeCount": %d, "callerMethodCount": %d}
                """.formatted(
                escape(datasourceAngle.packageText()),
                escape(datasourceAngle.typeSimpleName()),
                escape(datasourceAngle.simpleMethodSignatureText()),
                escape(datasourceAngle.methodReturnType().simpleNameWithGenerics()),
                escape(datasourceAngle.typeLabel()),
                escape(jigDocumentContext.typeTerm(datasourceAngle.methodReturnType().id()).title()),
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
                escape(jigPackage.packageId().asText()),
                escape(jigPackage.term().title()),
                jigPackage.jigTypes().size());
    }

    private String formatBusinessAllJson(JigType jigType, CoreTypesAndRelations coreTypesAndRelations, TypeRelationships allClassRelations) {
        boolean samePackageOnly = allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).packageIds().values()
                .equals(Set.of(jigType.packageId()));
        return """
                {"packageName": "%s", "typeName": "%s", "typeLabel": "%s", "businessRuleKind": "%s", "incomingBusinessRuleCount": %d, "outgoingBusinessRuleCount": %d, "incomingClassCount": %d, "nonPublic": %s, "samePackageOnly": %s, "incomingClassList": "%s"}
                """.formatted(
                escape(jigType.packageId().asText()),
                escape(jigType.id().asSimpleText()),
                escape(jigType.label()),
                escape(jigType.toValueKind().toString()),
                coreTypesAndRelations.internalTypeRelationships().filterTo(jigType.id()).size(),
                coreTypesAndRelations.internalTypeRelationships().filterFrom(jigType.id()).size(),
                allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).list().size(),
                jigType.visibility() != JigTypeVisibility.PUBLIC,
                samePackageOnly,
                escape(allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText()));
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
                escape(jigType.packageId().asText()),
                escape(jigType.id().asSimpleText()),
                escape(jigType.label()),
                escape(constants),
                escape(fields),
                allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).list().size(),
                escape(allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText()),
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
                escape(jigType.packageId().asText()),
                escape(jigType.id().asSimpleText()),
                escape(jigType.label()),
                escape(fieldTypes),
                allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).size(),
                escape(allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText()),
                jigType.instanceJigMethods().list().size(),
                escape(jigType.instanceJigMethods().stream()
                        .map(JigMethod::simpleMethodDeclarationText)
                        .sorted()
                        .collect(STREAM_COLLECTOR)));
    }

    private String formatBusinessValidationJson(Validation validation) {
        return """
                {"packageName": "%s", "typeName": "%s", "typeLabel": "%s", "memberName": "%s", "memberType": "%s", "annotationType": "%s", "annotationDescription": "%s"}
                """.formatted(
                escape(validation.typeId().packageId().asText()),
                escape(validation.typeId().asSimpleText()),
                escape(jigDocumentContext.typeTerm(validation.typeId()).title()),
                escape(validation.memberName()),
                escape(validation.memberType().asSimpleText()),
                escape(validation.annotationType().asSimpleText()),
                escape(validation.annotationDescription()));
    }

    private String formatBusinessMethodSmellJson(MethodSmell methodSmell) {
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "typeLabel": "%s", "notUseMember": %s, "primitiveInterface": %s, "referenceNull": %s, "nullDecision": %s, "returnsBoolean": %s, "returnsVoid": %s}
                """.formatted(
                escape(methodSmell.method().declaringType().packageId().asText()),
                escape(methodSmell.method().declaringType().asSimpleText()),
                escape(methodSmell.method().simpleMethodSignatureText()),
                escape(methodSmell.methodReturnType().asSimpleText()),
                escape(methodSmell.declaringJigType().label()),
                methodSmell.notUseMember(),
                methodSmell.primitiveInterface(),
                methodSmell.referenceNull(),
                methodSmell.nullDecision(),
                methodSmell.returnsBoolean(),
                methodSmell.returnsVoid());
    }

    private String toJsonStringList(List<String> values) {
        return values.stream()
                .map(this::escape)
                .map(value -> "\"" + value + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escape(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
