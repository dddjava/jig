package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;
import org.dddjava.jig.domain.model.knowledge.module.JigPackageWithJigTypes;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class ApplicationSummaryAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public ApplicationSummaryAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.ApplicationSummary)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var jigTypes = jigService.serviceTypes(repository);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        if (jigTypes.empty()) {
            jigDocumentWriter.markSkip();
            return List.of();
        }

        var jigPackageWithJigTypes = JigPackageWithJigTypes.from(jigTypes);
        var packageMap = jigPackageWithJigTypes.stream()
                .map(JigPackageWithJigTypes::packageId)
                .flatMap(packageId -> packageId.genealogical().stream())
                .collect(groupingBy(PackageId::parent, toSet()));

        var json = buildJson(jigDocument, jigTypes, packageMap);

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("applicationData", json);

        return jigDocumentWriter.outputFilePaths();
    }

    private String buildJson(JigDocument jigDocument, JigTypes jigTypes, Map<PackageId, Set<PackageId>> packageMap) {
        var rootComposite = resolveRoot(PackageId.defaultPackage(), packageMap);
        var treeJson = buildTreeNode(rootComposite, jigTypes, packageMap);

        var allPackageIds = packageMap.values().stream()
                .flatMap(Set::stream)
                .sorted()
                .toList();

        var packages = allPackageIds.stream()
                .map(packageId -> buildPackageJson(packageId, jigTypes, packageMap))
                .collect(Collectors.toList());

        var types = jigTypes.list().stream()
                .map(this::buildTypeJson)
                .collect(Collectors.toList());

        return Json.object("title", jigDocument.label())
                .and("tree", treeJson)
                .and("packages", Json.arrayObjects(packages))
                .and("types", Json.arrayObjects(types))
                .build();
    }

    private PackageId resolveRoot(PackageId packageId, Map<PackageId, Set<PackageId>> packageMap) {
        Set<PackageId> children = packageMap.getOrDefault(packageId, Set.of());
        if (children.size() == 1) {
            PackageId only = children.iterator().next();
            if (packageMap.containsKey(only)) {
                return resolveRoot(only, packageMap);
            }
        }
        return packageId;
    }

    private JsonObjectBuilder buildTreeNode(PackageId packageId, JigTypes jigTypes, Map<PackageId, Set<PackageId>> packageMap) {
        var jigPackage = jigPackage(packageId);
        var childNodes = new ArrayList<JsonObjectBuilder>();

        var childPackageIds = packageMap.getOrDefault(packageId, Set.of()).stream().sorted().toList();
        for (var childPackageId : childPackageIds) {
            childNodes.add(buildTreeNode(childPackageId, jigTypes, packageMap));
        }

        var classesInPackage = jigTypes.listMatches(jigType -> jigType.packageId().equals(packageId));
        for (var jigType : classesInPackage) {
            childNodes.add(Json.object("name", jigType.label())
                    .and("href", "#" + jigType.id().fqn())
                    .and("isPackage", false)
                    .and("isDeprecated", jigType.isDeprecated()));
        }

        var builder = Json.object("name", jigPackage.label())
                .and("href", "#" + jigPackage.fqn())
                .and("isPackage", true)
                .and("isDeprecated", false);
        if (!childNodes.isEmpty()) {
            builder.and("children", Json.arrayObjects(childNodes));
        }
        return builder;
    }

    private JsonObjectBuilder buildPackageJson(PackageId packageId, JigTypes jigTypes, Map<PackageId, Set<PackageId>> packageMap) {
        var jigPackage = jigPackage(packageId);

        var children = new ArrayList<JsonObjectBuilder>();

        var childPackageIds = packageMap.getOrDefault(packageId, Set.of()).stream().sorted().toList();
        for (var childPackageId : childPackageIds) {
            var childPackage = jigPackage(childPackageId);
            children.add(Json.object("name", childPackage.label())
                    .and("href", "#" + childPackage.fqn())
                    .and("isPackage", true));
        }

        var classesInPackage = jigTypes.listMatches(jigType -> jigType.packageId().equals(packageId));
        for (var jigType : classesInPackage) {
            children.add(Json.object("name", jigType.label())
                    .and("href", "#" + jigType.id().fqn())
                    .and("isPackage", false));
        }

        return Json.object("fqn", jigPackage.fqn())
                .and("label", jigPackage.label())
                .and("description", jigPackage.term().description())
                .and("children", Json.arrayObjects(children));
    }

    private JsonObjectBuilder buildTypeJson(JigType jigType) {
        var fields = jigType.instanceJigFields().fields().stream()
                .map(this::buildFieldJson)
                .collect(Collectors.toList());

        var instanceMethods = jigType.instanceJigMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod()
                .listRemarkable()
                .stream()
                .map(this::buildMethodJson)
                .collect(Collectors.toList());

        var staticMethods = jigType.staticJigMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod()
                .listRemarkable()
                .stream()
                .map(this::buildMethodJson)
                .collect(Collectors.toList());

        return Json.object("fqn", jigType.fqn())
                .and("label", jigType.label())
                .and("description", jigType.term().description())
                .and("isDeprecated", jigType.isDeprecated())
                .and("fields", Json.arrayObjects(fields))
                .and("instanceMethods", Json.arrayObjects(instanceMethods))
                .and("staticMethods", Json.arrayObjects(staticMethods));
    }

    private JsonObjectBuilder buildFieldJson(JigField field) {
        return Json.object("name", field.nameText())
                .and("typeHtml", linkText(field.jigTypeReference()))
                .and("isDeprecated", field.isDeprecated());
    }

    private JsonObjectBuilder buildMethodJson(JigMethod method) {
        var paramsHtml = method.jigMethodDeclaration().header().parameterTypeList().stream()
                .map(this::linkText)
                .collect(Collectors.toList());

        return Json.object("labelWithSymbol", method.labelTextWithSymbol())
                .and("paramsHtml", Json.array(paramsHtml))
                .and("returnTypeHtml", linkText(method.jigMethodDeclaration().header().returnType()))
                .and("description", method.term().description());
    }

    private String linkText(JigTypeReference jigTypeReference) {
        var typeArgumentList = jigTypeReference.typeArgumentList();
        String typeArgumentText = typeArgumentList.isEmpty() ? "" :
                typeArgumentList.stream()
                        .map(JigTypeArgument::typeId)
                        .map(this::typeIdToLinkText)
                        .collect(joining(", ", "&lt;", "&gt;"));

        return typeIdToLinkText(jigTypeReference.id()) + typeArgumentText;
    }

    private String typeIdToLinkText(TypeId typeId) {
        if (typeId.isJavaLanguageType()) {
            return String.format("<span class=\"weak\">%s</span>", typeId.asSimpleText());
        }
        return String.format("<a href=\"./domain.html#%s\">%s</a>", typeId.fqn(), jigDocumentContext.typeTerm(typeId).title());
    }

    private JigPackage jigPackage(PackageId packageId) {
        return new JigPackage(packageId, jigDocumentContext.packageTerm(packageId));
    }
}
