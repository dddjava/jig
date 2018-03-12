package jig.domain.model.list;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum ModelKind {
    SERVICE(ServiceModelConcern.class),
    REPOSITORY(RepositoryModelConcern.class),;

    private final Class<? extends Enum> concern;

    ModelKind(Class<? extends Enum> concern) {
        this.concern = concern;
    }

    public List<String> headerLabel() {
        Enum[] arr = concernValues();
        return Arrays.stream(arr)
                .map(Enum::name)
                .collect(toList());
    }

    public List<String> row(ConverterCondition converterCondition) {
        Converter[] arr = concernValues();
        return Arrays.stream(arr)
                .map(converter -> converter.convert(converterCondition))
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <T> T[] concernValues() {
        try {
            Method values = concern.getMethod("values");
            return (T[]) values.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
