package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.presentation.view.graphviz.dot.DotView;
import org.dddjava.jig.presentation.view.html.SummaryView;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;

public class ViewResolver {

    JigDiagramFormat diagramFormat;

    JigDocumentContext jigDocumentContext;
    DotCommandRunner dotCommandRunner;

    public ViewResolver(JigDiagramFormat diagramFormat, JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
        this.diagramFormat = diagramFormat;
        this.dotCommandRunner = new DotCommandRunner();
    }

    public JigView resolve(JigDocument jigDocument) {
        switch (jigDocument.jigDocumentType()) {
            case LIST:
                return new ModelReportsPoiView(jigDocumentContext);
            case DIAGRAM:
                return new DotView(diagramFormat, dotCommandRunner, jigDocumentContext);
            case SUMMARY:
                return new SummaryView(jigDocumentContext);
        }

        throw new IllegalArgumentException("View未定義のJigDocumentを出力しようとしています: " + jigDocument);
    }
}

