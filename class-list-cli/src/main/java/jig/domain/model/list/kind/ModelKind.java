package jig.domain.model.list.kind;

import jig.domain.model.list.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum ModelKind {
    SERVICE("Service", ServiceModelConcern.class),
    REPOSITORY("Repository", RepositoryModelConcern.class),;

    private final String suffix;
    private final Class<? extends Enum> concern;

    ModelKind(String suffix, Class<? extends Enum> concern) {
        this.suffix = suffix;
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

    public boolean correct(ModelType modelType) {
        return modelType.name().value().endsWith(suffix);
    }
}
