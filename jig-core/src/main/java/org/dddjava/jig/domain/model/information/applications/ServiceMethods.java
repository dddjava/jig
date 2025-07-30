package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * サービスメソッド一覧
 */
public record ServiceMethods(List<Entry> entries) {
    public <T> Stream<T> streamAndMap(BiFunction<JigType, List<ServiceMethod>, T> biFunction) {
        return entries.stream().map(entry -> biFunction.apply(entry.jigType, entry.serviceMethodList));
    }

    private record Entry(JigType jigType, List<ServiceMethod> serviceMethodList) {
    }

    public static ServiceMethods from(JigTypes serviceJigTypes, CallerMethodsFactory callerMethodsFactory) {
        var list = serviceJigTypes.orderedStream()
                .map(jigType -> {
                    var serviceMethods = jigType.instanceJigMethodStream()
                            .sorted(Comparator.comparing(JigMethod::fqn))
                            .map(method -> ServiceMethod.from(method, callerMethodsFactory))
                            .toList();
                    return new Entry(jigType, serviceMethods);
                })
                .toList();
        return new ServiceMethods(list);
    }

    public boolean empty() {
        return list().isEmpty();
    }

    public boolean contains(JigMethodId jigMethodId) {
        return list().stream()
                .anyMatch(serviceMethod -> serviceMethod.method().jigMethodId().equals(jigMethodId));
    }

    public List<ServiceMethod> list() {
        return entries.stream()
                .map(Entry::serviceMethodList)
                .flatMap(Collection::stream)
                .toList();
    }
}
