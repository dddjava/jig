package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSource;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSources;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.models.jigobject.components.ComponentRelations;

import java.util.StringJoiner;

/**
 * コンポーネント関連図
 *
 * コンポーネント（フレームワークなどで識別されるもの）の関連を示します。
 */
public class ComponentRelationDiagram implements DiagramSourceWriter {

    ComponentRelations componentRelations;

    public ComponentRelationDiagram(ComponentRelations componentRelations) {
        this.componentRelations = componentRelations;
    }

    @Override
    public DiagramSources sources(JigDocumentContext jigDocumentContext) {

        DocumentName documentName = DocumentName.of(JigDocument.ComponentRelationDiagram);
        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {\n", "\n}")
                .add("label=\"" + documentName.label() + "\";")
                .add("layout=fdp;")
                //.add("splines=ortho;")
                ;
        graph.add(componentRelations.dotText());
        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
