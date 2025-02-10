package org.dddjava.jig.adapter.diagram;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigTypesRepository;
import org.dddjava.jig.domain.model.documents.diagrams.ClassRelationDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;

import java.nio.file.Path;
import java.util.List;

@HandleDocument
public class DiagramAdapter implements Adapter<DiagramSourceWriter> {

    private final JigService jigService;
    private final GraphvizDiagramWriter graphvizDiagramWriter;

    public DiagramAdapter(JigService jigService, GraphvizDiagramWriter graphvizDiagramWriter) {
        this.jigService = jigService;
        this.graphvizDiagramWriter = graphvizDiagramWriter;
    }

    @HandleDocument(JigDocument.PackageRelationDiagram)
    public DiagramSourceWriter packageRelation(JigTypesRepository jigTypesRepository) {
        return jigService.packageDependencies(jigTypesRepository);
    }

    @HandleDocument(JigDocument.CompositeUsecaseDiagram)
    public DiagramSourceWriter CompositeUsecaseDiagram(JigTypesRepository jigTypesRepository) {
        return new CompositeUsecaseDiagram(jigService.serviceAngles(jigTypesRepository));
    }

    @HandleDocument(JigDocument.ArchitectureDiagram)
    public DiagramSourceWriter architectureDiagram(JigTypesRepository jigTypesRepository) {
        return jigService.architectureDiagram(jigTypesRepository);
    }

    @HandleDocument(JigDocument.BusinessRuleRelationDiagram)
    public DiagramSourceWriter businessRuleRelation(JigTypesRepository jigTypesRepository) {
        return new ClassRelationDiagram(jigService.coreDomainJigTypes(jigTypesRepository));
    }

    @HandleDocument(JigDocument.CategoryDiagram)
    public DiagramSourceWriter categories(JigTypesRepository jigTypesRepository) {
        return jigService.categories(jigTypesRepository);
    }

    @HandleDocument(JigDocument.CategoryUsageDiagram)
    public DiagramSourceWriter categoryUsages(JigTypesRepository jigTypesRepository) {
        return jigService.categoryUsages(jigTypesRepository);
    }

    @HandleDocument(JigDocument.ServiceMethodCallHierarchyDiagram)
    public DiagramSourceWriter serviceMethodCallHierarchy(JigTypesRepository jigTypesRepository) {
        return new ServiceMethodCallHierarchyDiagram(jigService.serviceAngles(jigTypesRepository));
    }

    @Override
    public List<Path> write(DiagramSourceWriter result, JigDocument jigDocument) {
        return graphvizDiagramWriter.write(result, jigDocument);
    }
}
