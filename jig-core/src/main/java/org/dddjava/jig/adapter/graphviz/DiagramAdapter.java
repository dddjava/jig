package org.dddjava.jig.adapter.graphviz;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.diagrams.*;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;

import java.nio.file.Path;
import java.util.List;

/**
 * ダイアグラムのOutputAdapter
 *
 * {@link HandleDocument} で指定されたドキュメントを出力する際に各メソッドが実行される。
 */
@HandleDocument
public class DiagramAdapter {

    private final JigService jigService;
    private final GraphvizDiagramWriter graphvizDiagramWriter;

    public DiagramAdapter(JigService jigService, GraphvizDiagramWriter graphvizDiagramWriter) {
        this.jigService = jigService;
        this.graphvizDiagramWriter = graphvizDiagramWriter;
    }

    @HandleDocument(JigDocument.PackageRelationDiagram)
    public List<Path> packageRelation(JigRepository jigRepository, JigDocument jigDocument) {
        return write(jigDocument, PackageRelationDiagram.from(jigService.coreTypesAndRelations(jigRepository)));
    }

    @HandleDocument(JigDocument.BusinessRuleRelationDiagram)
    public List<Path> businessRuleRelation(JigRepository jigRepository, JigDocument jigDocument) {
        return write(jigDocument, new ClassRelationDiagram(jigService.coreTypesAndRelations(jigRepository)));
    }

    @HandleDocument(JigDocument.CategoryDiagram)
    public List<Path> categories(JigRepository jigRepository, JigDocument jigDocument) {
        return write(jigDocument, CategoryDiagram.create(jigService.categoryTypes(jigRepository)));
    }

    @HandleDocument(JigDocument.CategoryUsageDiagram)
    public List<Path> categoryUsages(JigRepository jigRepository, JigDocument jigDocument) {
        ServiceMethods serviceMethods = jigService.serviceMethods(jigRepository);
        CoreTypesAndRelations coreTypesAndRelations = jigService.coreTypesAndRelations(jigRepository);
        return write(jigDocument, new CategoryUsageDiagram(serviceMethods, coreTypesAndRelations));
    }

    @HandleDocument(JigDocument.ServiceMethodCallHierarchyDiagram)
    public List<Path> serviceMethodCallHierarchy(JigRepository jigRepository, JigDocument jigDocument) {
        return write(jigDocument, new ServiceMethodCallHierarchyDiagram(jigService.serviceAngles(jigRepository)));
    }

    private List<Path> write(JigDocument jigDocument, DiagramSourceWriter result) {
        return graphvizDiagramWriter.write(result, jigDocument);
    }
}
