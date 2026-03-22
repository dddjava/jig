package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.mermaid.TypeRelationMermaidDiagram;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
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
import org.dddjava.jig.domain.model.knowledge.module.JigPackageWithJigTypes;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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

        var packageList = JigPackageWithJigTypes.listWithParent(jigTypes);

        var json = buildJson(packageList, jigTypes.list(), enumModelMap, coreTypesAndRelations);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());
        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("domainData", json);
        return jigDocumentWriter.outputFilePaths();
    }

    private String buildJson(List<JigPackageWithJigTypes> jigPackages,
                             List<JigType> jigTypes,
                             Map<TypeId, EnumModel> enumModelMap,
                             CoreTypesAndRelations coreTypesAndRelations) {
        List<JsonObjectBuilder> packages = jigPackages.stream()
                .map(jigPackage -> buildPackageJson(jigPackage, coreTypesAndRelations))
                .toList();

        List<JsonObjectBuilder> types = jigTypes.stream()
                .map(jigType -> buildTypeJson(jigType, enumModelMap))
                .toList();

        return Json.object("packages", Json.arrayObjects(packages))
                .and("types", Json.arrayObjects(types))
                .build();
    }

    private JsonObjectBuilder buildPackageJson(JigPackageWithJigTypes jigPackage,
                                               CoreTypesAndRelations coreTypesAndRelations) {
        List<JsonObjectBuilder> types = jigPackage.jigTypes().stream()
                .map(JigType::id)
                .sorted(Comparable::compareTo)
                .map(typeId -> Json.object("fqn", typeId.fqn()))
                .toList();

        String diagram = new TypeRelationMermaidDiagram()
                .write(jigPackage, coreTypesAndRelations)
                .orElse("");

        return Json.object("fqn", jigPackage.packageId().asText())
                .and("types", Json.arrayObjects(types))
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
        EnumModel enumModel = enumModelMap.getOrDefault(jigType.id(), new EnumModel(jigType.id(), List.of(), List.of()));
        List<String> parameterNames = enumModel.constructorParameterNames();

        List<JsonObjectBuilder> constants = jigType.jigTypeMembers().enumConstantStream()
                .map(constant -> {
                    var name = constant.nameText();
                    return Json.object("name", name)
                            .and("params", Json.array(enumModel.paramOf(name)));
                })
                .toList();

        return Json.object("constants", Json.arrayObjects(constants))
                .and("parameterNames", Json.array(parameterNames));
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
}
