package jig.domain.model.usage;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class DependentTypes {
    List<Class<?>> list;

    public DependentTypes(List<Class<?>> list) {
        this.list = list;
    }

    public List<Class<?>> list() {
        return list;
    }

    public static DependentTypes from(Class<?> serviceClass) {
        return new DependentTypes(
                Arrays.stream(serviceClass.getDeclaredFields())
                        .map(Field::getType)
                        .collect(toList()));
    }

    public static DependentTypes empty() {
        return new DependentTypes(Collections.emptyList());
    }
}
