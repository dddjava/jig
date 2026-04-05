package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.JigDocumentAdapter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.module.JigPackageWithJigTypes;

import java.util.List;

/**
 * ドメインモデル（domain-data.js）
 */
public class DomainDataAdapter implements JigDocumentAdapter {

    private final JigService jigService;

    public DomainDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "domainData";
    }

    @Override
    public String dataFileName() {
        return "domain-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        var coreDomainJigTypes = jigService.coreDomainJigTypes(jigRepository);
        if (coreDomainJigTypes.empty()) {
            return "{}";
        }
        var jigTypes = coreDomainJigTypes.jigTypes();
        var packageList = JigPackageWithJigTypes.listWithParent(jigTypes);
        var enumModels = jigRepository.jigDataProvider().fetchEnumModels();
        return buildDomainJson(packageList, jigTypes, enumModels);
    }

    public static String buildDomainJson(List<JigPackageWithJigTypes> jigPackages,
                                  JigTypes jigTypes,
                                  EnumModels enumModels) {
        List<JsonObjectBuilder> packages = jigPackages.stream()
                .map(DomainDataAdapter::buildPackageJson)
                .toList();

        List<JsonObjectBuilder> types = jigTypes.stream()
                .map(jigType -> DomainDataAdapter.buildTypeJson(jigType, enumModels))
                .toList();

        return Json.object("packages", Json.arrayObjects(packages))
                .and("types", Json.arrayObjects(types))
                .build();
    }

    private static JsonObjectBuilder buildPackageJson(JigPackageWithJigTypes jigPackage) {
        List<JsonObjectBuilder> types = jigPackage.jigTypes().stream()
                .map(JigType::id)
                .sorted(Comparable::compareTo)
                .map(typeId -> Json.object("fqn", typeId.fqn()))
                .toList();

        return Json.object("fqn", jigPackage.packageId().asText())
                .and("types", Json.arrayObjects(types));
    }

    private static JsonObjectBuilder buildTypeJson(JigType jigType, EnumModels enumModels) {
        List<JsonObjectBuilder> fields = jigType.instanceJigFields().fields().stream()
                .map(JsonSupport::buildFieldJson)
                .toList();

        List<JsonObjectBuilder> methods = jigType.instanceJigMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod()
                .listRemarkable()
                .stream()
                .map(JsonSupport::buildMethodJson)
                .toList();

        List<JsonObjectBuilder> staticMethods = jigType.staticJigMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod()
                .listRemarkable()
                .stream()
                .map(JsonSupport::buildMethodJson)
                .toList();

        JsonObjectBuilder builder = Json.object("fqn", jigType.fqn())
                .and("fields", Json.arrayObjects(fields))
                .and("methods", Json.arrayObjects(methods))
                .and("staticMethods", Json.arrayObjects(staticMethods))
                .and("isDeprecated", jigType.isDeprecated());

        if (isEnum(jigType)) {
            builder.and("enumInfo", buildEnumInfoJson(jigType, enumModels));
        }

        return builder;
    }

    private static boolean isEnum(JigType jigType) {
        return jigType.toValueKind() == JigTypeValueKind.区分;
    }

    private static JsonObjectBuilder buildEnumInfoJson(JigType jigType, EnumModels enumModels) {
        EnumModel enumModel = enumModels.find(jigType.id())
                .orElseGet(() -> new EnumModel(jigType.id(), List.of(), List.of()));
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
}
