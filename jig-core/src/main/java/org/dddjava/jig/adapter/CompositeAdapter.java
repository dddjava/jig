package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;

public class CompositeAdapter {

    EnumMap<JigDocument, Adapter<?>> adapterInstanceMap = new EnumMap<>(JigDocument.class);
    EnumMap<JigDocument, Method> adapterMethodMap = new EnumMap<>(JigDocument.class);

    public void register(Adapter<?> adapter) {
        for (var method : adapter.getClass().getMethods()) {
            var annotation = method.getAnnotation(HandleDocument.class);
            if (annotation == null) continue;

            for (var jigDocument : annotation.value()) {
                adapterInstanceMap.put(jigDocument, adapter);
                adapterMethodMap.put(jigDocument, method);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<Path> invoke(JigDocument jigDocument, JigDataProvider jigDataProvider) {
        Adapter<T> adapter = (Adapter<T>) adapterInstanceMap.get(jigDocument);
        Method adapterMethod = adapterMethodMap.get(jigDocument);

        try {
            T model = adapter.convertMethodResultToAdapterModel(adapterMethod.invoke(adapter, jigDataProvider));
            return adapter.write(model, jigDocument);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
