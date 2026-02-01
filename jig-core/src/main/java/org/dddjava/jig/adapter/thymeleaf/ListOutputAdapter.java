package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngle;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.Usecase;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@HandleDocument
public class ListOutputAdapter {

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
        String controllerJson = inputAdapters.listEntrypoint().stream()
                .map(this::formatControllerJson)
                .collect(Collectors.joining(",", "[", "]"));
        String serviceJson = serviceAngles.list().stream()
                .map(this::formatServiceJson)
                .collect(Collectors.joining(",", "[", "]"));
        String repositoryJson = datasourceAngles.list().stream()
                .map(this::formatRepositoryJson)
                .collect(Collectors.joining(",", "[", "]"));

        String listJson = """
                {"controllers": %s, "services": %s, "repositories": %s}
                """.formatted(controllerJson, serviceJson, repositoryJson);

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
