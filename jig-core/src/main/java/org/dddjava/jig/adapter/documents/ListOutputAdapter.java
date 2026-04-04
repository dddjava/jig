package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
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
import org.dddjava.jig.domain.model.information.inbound.Entrypoint;
import org.dddjava.jig.domain.model.information.inbound.InputAdapters;
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

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@HandleDocument
public class ListOutputAdapter {

    /**
     * 一覧出力で複数要素を文字列連結する際のコレクター
     */
    private static final Collector<CharSequence, ?, String> STREAM_COLLECTOR = Collectors.joining(", ", "[", "]");

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public ListOutputAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.ListOutput)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        String listJson = buildJson(repository, jigService);

        jigDocumentWriter.writeData("listData", listJson);
        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(JigRepository repository, JigService jigService) {
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
                .map(ListOutputAdapter::formatControllerJson)
                .collect(Collectors.joining(",", "[", "]"));
        String serviceJson = serviceAngles.list().stream()
                .map(ListOutputAdapter::formatServiceJson)
                .collect(Collectors.joining(",", "[", "]"));
        String repositoryJson = datasourceAngles.list().stream()
                .map(ListOutputAdapter::formatRepositoryJson)
                .collect(Collectors.joining(",", "[", "]"));
        String packageJson = jigTypePackages.stream()
                .map(ListOutputAdapter::formatBusinessPackageJson)
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
                .map(ListOutputAdapter::formatBusinessValidationJson)
                .collect(Collectors.joining(",", "[", "]"));
        String methodSmellJson = methodSmells.list().stream()
                .map(ListOutputAdapter::formatBusinessMethodSmellJson)
                .collect(Collectors.joining(",", "[", "]"));

        return """
                {"businessRules": {"packages": %s, "all": %s, "enums": %s, "collections": %s, "validations": %s, "methodSmells": %s}, "applications": {"controllers": %s, "services": %s, "repositories": %s}}
                """.formatted(packageJson, allJson, enumJson, collectionJson, validationJson, methodSmellJson, controllerJson, serviceJson, repositoryJson);
    }

    private static String formatControllerJson(Entrypoint entrypoint) {
        List<String> usingFieldTypes = entrypoint.jigMethod().usingFields().jigFieldIds().stream()
                .map(JigFieldId::declaringTypeId)
                .map(TypeId::asSimpleText)
                .sorted()
                .toList();
        return Json.object("packageName", entrypoint.packageId().asText())
                .and("typeName", entrypoint.typeId().asSimpleText())
                .and("methodSignature", entrypoint.jigMethod().simpleMethodSignatureText())
                .and("returnType", entrypoint.jigMethod().returnType().simpleName())
                .and("usingFieldTypes", Json.array(usingFieldTypes))
                .and("cyclomaticComplexity", entrypoint.jigMethod().instructions().cyclomaticComplexity())
                .and("path", entrypoint.fullPathText())
                .build();
    }

    private static String formatServiceJson(Usecase usecase) {
        List<String> usingFieldTypes = usecase.usingFields().jigFieldIds().stream()
                .map(JigFieldId::declaringTypeId)
                .map(TypeId::asSimpleText)
                .sorted()
                .toList();
        List<String> parameterTypeFqns = usecase.serviceMethod().method().parameterTypeStream()
                .map(JigTypeReference::id)
                .map(TypeId::fqn)
                .toList();
        List<String> usingServiceMethods = usecase.usingServiceMethods().stream()
                .map(methodCall -> methodCall.asSignatureAndReturnTypeSimpleText())
                .toList();
        List<String> usingRepositoryMethods = usecase.usingRepositoryMethods().stream()
                .map(jigMethod -> jigMethod.simpleMethodSignatureText())
                .toList();
        return Json.object("packageName", usecase.serviceMethod().declaringType().packageId().asText())
                .and("typeName", usecase.serviceMethod().declaringType().asSimpleText())
                .and("methodSignature", usecase.serviceMethod().method().simpleMethodSignatureText())
                .and("returnType", usecase.serviceMethod().method().returnType().simpleName())
                .and("eventHandler", usecase.usingFromController())
                .and("methodFqn", usecase.serviceMethod().method().fqn())
                .and("returnTypeFqn", usecase.serviceMethod().method().returnType().id().fqn())
                .and("parameterTypeFqns", Json.array(parameterTypeFqns))
                .and("usingFieldTypes", Json.array(usingFieldTypes))
                .and("cyclomaticComplexity", usecase.serviceMethod().method().instructions().cyclomaticComplexity())
                .and("usingServiceMethods", Json.array(usingServiceMethods))
                .and("usingRepositoryMethods", Json.array(usingRepositoryMethods))
                .and("useNull", usecase.useNull())
                .and("useStream", usecase.useStream())
                .build();
    }

    private static String formatRepositoryJson(DatasourceAngle datasourceAngle) {
        List<String> parameterTypeFqns = datasourceAngle.methodParameterTypeStream()
                .map(JigTypeReference::id)
                .map(TypeId::fqn)
                .toList();
        return Json.object("packageName", datasourceAngle.packageText())
                .and("typeName", datasourceAngle.typeSimpleName())
                .and("methodSignature", datasourceAngle.simpleMethodSignatureText())
                .and("returnType", datasourceAngle.methodReturnType().simpleNameWithGenerics())
                .and("returnTypeFqn", datasourceAngle.methodReturnType().id().fqn())
                .and("parameterTypeFqns", Json.array(parameterTypeFqns))
                .and("cyclomaticComplexity", datasourceAngle.cyclomaticComplexity())
                .and("insertTables", Json.array(datasourceAngle.insertTableNames()))
                .and("selectTables", Json.array(datasourceAngle.selectTableNames()))
                .and("updateTables", Json.array(datasourceAngle.updateTableNames()))
                .and("deleteTables", Json.array(datasourceAngle.deleteTableNames()))
                .and("callerTypeCount", datasourceAngle.callerMethods().typeCount())
                .and("callerMethodCount", datasourceAngle.callerMethods().size())
                .build();
    }

    private static String formatBusinessPackageJson(JigPackage jigPackage) {
        return Json.object("packageName", jigPackage.packageId().asText())
                .and("classCount", jigPackage.jigTypes().size())
                .build();
    }

    private static String formatBusinessAllJson(JigType jigType, CoreTypesAndRelations coreTypesAndRelations, TypeRelationships allClassRelations) {
        boolean samePackageOnly = allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).packageIds().values()
                .equals(Set.of(jigType.packageId()));
        return Json.object("packageName", jigType.packageId().asText())
                .and("typeName", jigType.id().asSimpleText())
                .and("businessRuleKind", jigType.toValueKind().toString())
                .and("incomingBusinessRuleCount", coreTypesAndRelations.internalTypeRelationships().filterTo(jigType.id()).size())
                .and("outgoingBusinessRuleCount", coreTypesAndRelations.internalTypeRelationships().filterFrom(jigType.id()).size())
                .and("incomingClassCount", allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).list().size())
                .and("nonPublic", jigType.visibility() != JigTypeVisibility.PUBLIC)
                .and("samePackageOnly", samePackageOnly)
                .and("incomingClassList", allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText())
                .build();
    }

    private static String formatBusinessEnumJson(JigType jigType, TypeRelationships allClassRelations) {
        String constants = jigType.jigTypeMembers().enumConstantStream()
                .map(jigField -> jigField.jigFieldHeader().name())
                .collect(STREAM_COLLECTOR);
        String fields = jigType.jigTypeMembers().instanceFields().stream()
                .map(jigField -> jigField.jigFieldHeader().simpleText())
                .collect(STREAM_COLLECTOR);
        return Json.object("packageName", jigType.packageId().asText())
                .and("typeName", jigType.id().asSimpleText())
                .and("constants", constants)
                .and("fields", fields)
                .and("usageCount", allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).list().size())
                .and("usagePlaces", allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText())
                .and("hasParameters", jigType.hasInstanceField())
                .and("hasBehavior", jigType.hasInstanceMethod())
                .and("isPolymorphic", jigType.typeKind() == TypeKind.抽象列挙型)
                .build();
    }

    private static String formatBusinessCollectionJson(JigType jigType, TypeRelationships allClassRelations) {
        List<String> fieldTypeList = jigType.jigTypeMembers().instanceFields().stream()
                .map(jigField -> jigField.jigTypeReference().simpleNameWithGenerics())
                .toList();
        String fieldTypes = fieldTypeList.size() == 1
                ? fieldTypeList.get(0)
                : fieldTypeList.stream().collect(STREAM_COLLECTOR);
        return Json.object("packageName", jigType.packageId().asText())
                .and("typeName", jigType.id().asSimpleText())
                .and("fieldTypes", fieldTypes)
                .and("usageCount", allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).size())
                .and("usagePlaces", allClassRelations.collectTypeIdWhichRelationTo(jigType.id()).asSimpleText())
                .and("methodCount", jigType.instanceJigMethods().list().size())
                .and("methods", jigType.instanceJigMethods().stream()
                        .map(JigMethod::simpleMethodDeclarationText)
                        .sorted()
                        .collect(STREAM_COLLECTOR))
                .build();
    }

    private static String formatBusinessValidationJson(Validation validation) {
        return Json.object("packageName", validation.typeId().packageId().asText())
                .and("typeName", validation.typeId().asSimpleText())
                .and("memberName", validation.memberName())
                .and("memberType", validation.memberType().asSimpleText())
                .and("annotationType", validation.annotationType().asSimpleText())
                .and("annotationDescription", validation.annotationDescription())
                .build();
    }

    private static String formatBusinessMethodSmellJson(MethodSmell methodSmell) {
        return Json.object("packageName", methodSmell.method().declaringType().packageId().asText())
                .and("typeName", methodSmell.method().declaringType().asSimpleText())
                .and("methodSignature", methodSmell.method().simpleMethodSignatureText())
                .and("returnType", methodSmell.methodReturnType().asSimpleText())
                .and("notUseMember", methodSmell.notUseMember())
                .and("primitiveInterface", methodSmell.primitiveInterface())
                .and("referenceNull", methodSmell.referenceNull())
                .and("nullDecision", methodSmell.nullDecision())
                .and("returnsBoolean", methodSmell.returnsBoolean())
                .and("returnsVoid", methodSmell.returnsVoid())
                .build();
    }

}
