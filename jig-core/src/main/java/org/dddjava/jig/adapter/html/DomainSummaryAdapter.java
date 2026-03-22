package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.mermaid.TypeRelationMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.diagrams.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;
import org.dddjava.jig.domain.model.knowledge.module.JigPackageWithJigTypes;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * ドメイン概要
 */
public class DomainSummaryAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public DomainSummaryAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.DomainSummary)
    public List<Path> invoke(JigRepository jigRepository, JigDocument jigDocument) {
        var jigTypes = jigService.coreDomainJigTypes(jigRepository);
        if (jigTypes.empty()) {
            return List.of();
        }

        var enumModels = jigRepository.jigDataProvider().fetchEnumModels();
        var enumModelMap = enumModels.toMap();
        var coreTypesAndRelations = jigService.coreTypesAndRelations(jigRepository);

        var packageMap = buildPackageMap(jigTypes);
        var packageList = listPackages(packageMap);

        var json = buildJson(packageMap, packageList, jigTypes.list(), enumModelMap, coreTypesAndRelations);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());
        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("domainData", json);
        return jigDocumentWriter.outputFilePaths();
    }

    private String buildJson(Map<PackageId, Set<PackageId>> packageMap,
                             List<JigPackage> jigPackages,
                             List<JigType> jigTypes,
                             Map<TypeId, EnumModel> enumModelMap,
                             CoreTypesAndRelations coreTypesAndRelations) {
        Map<PackageId, List<JigType>> typesByPackage = jigTypes.stream()
                .collect(groupingBy(JigType::packageId));

        List<JsonObjectBuilder> packages = jigPackages.stream()
                .map(jigPackage -> buildPackageJson(jigPackage, typesByPackage, coreTypesAndRelations))
                .toList();

        List<JsonObjectBuilder> types = jigTypes.stream()
                .map(jigType -> buildTypeJson(jigType, enumModelMap))
                .toList();

        return Json.object("packages", Json.arrayObjects(packages))
                .and("types", Json.arrayObjects(types))
                .build();
    }

    private JsonObjectBuilder buildPackageJson(JigPackage jigPackage,
                                                Map<PackageId, List<JigType>> typesByPackage,
                                                CoreTypesAndRelations coreTypesAndRelations) {
        List<JsonObjectBuilder> children = typesByPackage
                .getOrDefault(jigPackage.packageId(), Collections.emptyList()).stream()
                .sorted(Comparator.comparing(t -> t.id().fqn()))
                .map(type -> Json.object("fqn", type.id().fqn()))
                .toList();

        String diagram = new TypeRelationMermaidDiagram()
                .write(jigPackage, coreTypesAndRelations)
                .orElse("");

        return Json.object("fqn", jigPackage.fqn())
                .and("types", Json.arrayObjects(children))
                .and("relationDiagram", diagram);
    }

    private JsonObjectBuilder buildTypeJson(JigType jigType, Map<TypeId, EnumModel> enumModelMap) {
        List<JsonObjectBuilder> fields = jigType.instanceJigFields().fields().stream()
                .map(this::buildFieldJson)
                .toList();

        List<JsonObjectBuilder> methods = jigType.instanceJigMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod()
                .listRemarkable()
                .stream()
                .map(this::buildMethodJson)
                .toList();

        List<JsonObjectBuilder> staticMethods = jigType.staticJigMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod()
                .listRemarkable()
                .stream()
                .map(this::buildMethodJson)
                .toList();

        JsonObjectBuilder builder = Json.object("fqn", jigType.fqn())
                .and("isDeprecated", jigType.isDeprecated())
                .and("fields", Json.arrayObjects(fields))
                .and("methods", Json.arrayObjects(methods))
                .and("staticMethods", Json.arrayObjects(staticMethods));

        if (isEnum(jigType)) {
            builder.and("enumInfo", buildEnumInfoJson(jigType, enumModelMap));
        }

        return builder;
    }

    private boolean isEnum(JigType jigType) {
        return jigType.toValueKind() == JigTypeValueKind.区分;
    }

    private JsonObjectBuilder buildEnumInfoJson(JigType jigType, Map<TypeId, EnumModel> enumModelMap) {
        List<JsonObjectBuilder> constants = jigType.jigTypeMembers().enumConstantStream()
                .map(JigField::term)
                .map(term -> Json.object("simpleText", term.simpleText())
                        .and("title", term.title())
                        .and("hasAlias", term.hasAlias()))
                .toList();

        EnumModel enumModel = enumModelMap.getOrDefault(jigType.id(), new EnumModel(jigType.id(), List.of(), List.of()));
        List<String> parameterNames = enumModel.constructorParameterNames();

        List<JsonObjectBuilder> parameterRows = jigType.jigTypeMembers().enumConstantStream()
                .map(JigField::nameText)
                .map(constantName -> Json.object("name", constantName)
                        .and("params", Json.array(enumModel.paramOf(constantName))))
                .toList();

        return Json.object("constants", Json.arrayObjects(constants))
                .and("parameterNames", Json.array(parameterNames))
                .and("parameterRows", Json.arrayObjects(parameterRows));
    }

    private JsonObjectBuilder buildFieldJson(JigField field) {
        return Json.object("name", field.nameText())
                .and("typeRef", buildTypeRef(field.jigTypeReference()))
                .and("isDeprecated", field.isDeprecated());
    }

    private JsonObjectBuilder buildMethodJson(JigMethod jigMethod) {
        return Json.object("fqn", jigMethod.fqn())
                .and("visibility", jigMethod.visibility())
                .and("parameterTypeRefs", Json.arrayObjects(jigMethod.parameterTypeStream()
                        .map(this::buildTypeRef)
                        .toList()))
                .and("returnTypeRef", buildTypeRef(jigMethod.returnType()));
    }

    private JsonObjectBuilder buildTypeRef(JigTypeReference jigTypeReference) {
        var obj = Json.object("fqn", jigTypeReference.fqn());
        // 型引数がない場合は fqn だけのオブジェクトにする
        if (jigTypeReference.typeArgumentList().isEmpty()) return obj;

        return obj.and("typeArgumentRefs", Json.arrayObjects(jigTypeReference.typeArgumentList().stream()
                .map(JigTypeArgument::jigTypeReference)
                .map(this::buildTypeRef)
                .toList()));
    }

    private Map<PackageId, Set<PackageId>> buildPackageMap(JigTypes jigTypes) {
        List<JigPackageWithJigTypes> jigPackageWithJigTypes = JigPackageWithJigTypes.from(jigTypes);
        return jigPackageWithJigTypes.stream()
                .map(JigPackageWithJigTypes::packageId)
                .flatMap(packageId -> packageId.genealogical().stream())
                .collect(groupingBy(PackageId::parent, toSet()));
    }

    private List<JigPackage> listPackages(Map<PackageId, Set<PackageId>> packageMap) {
        return packageMap.values().stream()
                .flatMap(Set::stream)
                .sorted()
                .map(this::jigPackage)
                .toList();
    }

    private JigPackage jigPackage(PackageId packageId) {
        return new JigPackage(packageId, jigDocumentContext.packageTerm(packageId));
    }
}
