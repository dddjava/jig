package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigTypesRepository;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;

public class CompositeAdapter {

    EnumMap<JigDocument, Adapter<?>> adapterInstanceMap = new EnumMap<>(JigDocument.class);
    EnumMap<JigDocument, MethodHandle> adapterMethodMap = new EnumMap<>(JigDocument.class);

    public void register(Adapter<?> adapter) {
        try {
            var lookup = MethodHandles.lookup();
            for (var method : adapter.getClass().getMethods()) {
                var annotation = method.getAnnotation(HandleDocument.class);
                if (annotation == null) continue;

                MethodHandle methodHandle = lookup.unreflect(method);
                for (var jigDocument : annotation.value()) {
                    adapterInstanceMap.put(jigDocument, adapter);
                    adapterMethodMap.put(jigDocument, methodHandle);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<Path> invoke(JigDocument jigDocument, JigTypesRepository jigTypesRepository) {
        Adapter<T> adapter = (Adapter<T>) adapterInstanceMap.get(jigDocument);
        MethodHandle adapterMethod = adapterMethodMap.get(jigDocument);

        try {
            Object result = adapterMethod.invoke(adapter, jigTypesRepository);
            T model = adapter.convertMethodResultToAdapterModel(result);
            return adapter.write(model, jigDocument);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
