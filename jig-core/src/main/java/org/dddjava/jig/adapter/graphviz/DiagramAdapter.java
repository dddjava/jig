package org.dddjava.jig.adapter.graphviz;

import org.dddjava.jig.adapter.Adapter;
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
public class DiagramAdapter implements Adapter<DiagramSourceWriter> {

    private final JigService jigService;
    private final GraphvizDiagramWriter graphvizDiagramWriter;

    public DiagramAdapter(JigService jigService, GraphvizDiagramWriter graphvizDiagramWriter) {
        this.jigService = jigService;
        this.graphvizDiagramWriter = graphvizDiagramWriter;
    }

    @HandleDocument(JigDocument.PackageRelationDiagram)
    public DiagramSourceWriter packageRelation(JigRepository jigRepository) {
        return PackageRelationDiagram.from(jigService.coreTypesAndRelations(jigRepository));
    }

    @HandleDocument(JigDocument.BusinessRuleRelationDiagram)
    public DiagramSourceWriter businessRuleRelation(JigRepository jigRepository) {
        return new ClassRelationDiagram(jigService.coreTypesAndRelations(jigRepository));
    }

    @HandleDocument(JigDocument.CategoryDiagram)
    public DiagramSourceWriter categories(JigRepository jigRepository) {
        return CategoryDiagram.create(jigService.categoryTypes(jigRepository));
    }

    @HandleDocument(JigDocument.CategoryUsageDiagram)
    public DiagramSourceWriter categoryUsages(JigRepository jigRepository) {
        ServiceMethods serviceMethods = jigService.serviceMethods(jigRepository);
        CoreTypesAndRelations coreTypesAndRelations = jigService.coreTypesAndRelations(jigRepository);
        return new CategoryUsageDiagram(serviceMethods, coreTypesAndRelations);
    }

    @HandleDocument(JigDocument.ServiceMethodCallHierarchyDiagram)
    public DiagramSourceWriter serviceMethodCallHierarchy(JigRepository jigRepository) {
        return new ServiceMethodCallHierarchyDiagram(jigService.serviceAngles(jigRepository));
    }

    @Override
    public List<Path> write(DiagramSourceWriter result, JigDocument jigDocument) {
        return graphvizDiagramWriter.write(result, jigDocument);
    }
}
