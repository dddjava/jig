package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.JigDocument;

import java.util.List;
import java.util.Map;

/**
 * JigDocumentに対するDataAdapterを管理する
 */
public class DataAdapterResolver {

    private final Map<JigDocument, List<DataAdapter>> adaptersMap;

    public DataAdapterResolver(JigService jigService) {
        var typeRelationsDataAdapter = new TypeRelationsDataAdapter(jigService);
        var glossaryDataAdapter = new GlossaryDataAdapter(jigService);
        this.adaptersMap = Map.of(
                JigDocument.DomainModel, List.of(new DomainDataAdapter(jigService), typeRelationsDataAdapter, glossaryDataAdapter),
                JigDocument.PackageRelation, List.of(new PackageDataAdapter(jigService), typeRelationsDataAdapter, glossaryDataAdapter),
                JigDocument.Glossary, List.of(glossaryDataAdapter),
                JigDocument.Insight, List.of(new InsightDataAdapter(jigService)),
                JigDocument.InboundInterface, List.of(new InboundDataAdapter(jigService), glossaryDataAdapter),
                JigDocument.OutboundInterface, List.of(new OutboundDataAdapter(jigService), glossaryDataAdapter),
                JigDocument.Usecase, List.of(new UsecaseDataAdapter(jigService), glossaryDataAdapter),
                JigDocument.ListOutput, List.of(new ListOutputDataAdapter(jigService), glossaryDataAdapter),
                JigDocument.LibraryDependency, List.of(new LibraryDependencyDataAdapter(jigService), glossaryDataAdapter)
        );
    }

    public List<DataAdapter> resolve(JigDocument jigDocument) {
        return adaptersMap.getOrDefault(jigDocument, List.of());
    }
}
