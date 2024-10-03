package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.presentation.view.graphviz.dot.DotCommandRunner;
import org.dddjava.jig.presentation.view.graphviz.dot.DotView;
import org.dddjava.jig.presentation.view.html.*;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;

public class ViewResolver {

    JigDiagramFormat diagramFormat;

    JigDocumentContext jigDocumentContext;
    DotCommandRunner dotCommandRunner;
    TemplateEngine templateEngine;

    public ViewResolver(JigDiagramFormat diagramFormat, JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
        this.diagramFormat = diagramFormat;

        // setup Graphviz
        this.dotCommandRunner = new DotCommandRunner();

        // setup Thymeleaf
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(new JigExpressionObjectDialect(jigDocumentContext));
        this.templateEngine = templateEngine;
    }

    JigView resolve(JigDocument jigDocument) {
        switch (jigDocument.jigDocumentType()) {
            case LIST:
                return new ModelReportsPoiView(jigDocumentContext);
            case DIAGRAM:
                return new DotView(diagramFormat, dotCommandRunner, jigDocumentContext);
            case SUMMARY:
                return new SummaryView(templateEngine, jigDocumentContext);
            case TABLE:
                return new TableView(templateEngine, jigDocumentContext);
        }

        throw new IllegalArgumentException("View未定義のJigDocumentを出力しようとしています: " + jigDocument);
    }

    IndexView indexView() {
        return new IndexView(templateEngine, diagramFormat);
    }

    public JigView resolve(Class<? extends JigView> clz) {
        if (clz == HtmlView.class) {
            return new HtmlView(templateEngine, jigDocumentContext);
        }
        throw new UnsupportedOperationException("unsupported view: " + clz);
    }
}

