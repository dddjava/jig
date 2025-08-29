package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;

public class CompositeAdapter {

    EnumMap<JigDocument, Object> adapterInstanceMap = new EnumMap<>(JigDocument.class);
    EnumMap<JigDocument, MethodHandle> adapterMethodMap = new EnumMap<>(JigDocument.class);

    public void register(Object adapter) {
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

    public List<Path> invoke(JigDocument jigDocument, JigRepository jigRepository) {
        Object adapter = adapterInstanceMap.get(jigDocument);
        MethodHandle adapterMethod = adapterMethodMap.get(jigDocument);

        try {
            Object result = adapterMethod.invoke(adapter, jigRepository);
            if (result instanceof List<?> list) {
                // uncheckedの警告を抑止しないならこういう書き方になるが無駄な感じが否めない
                return list.stream().map(Path.class::cast).toList();
            }
            if (adapter instanceof Adapter writableAdapter) {
                return writableAdapter.write(result, jigDocument);
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
