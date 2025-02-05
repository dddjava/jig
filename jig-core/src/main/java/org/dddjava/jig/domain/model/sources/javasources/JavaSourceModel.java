package org.dddjava.jig.domain.model.sources.javasources;

import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.enums.EnumModels;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * javaファイル由来のソース
 */
public class JavaSourceModel {

    List<EnumModel> enumModels;

    private JavaSourceModel(List<EnumModel> enumModels) {
        this.enumModels = enumModels;
    }

    public static JavaSourceModel from(List<EnumModel> enumModels) {
        return new JavaSourceModel(enumModels);
    }

    public static JavaSourceModel empty() {
        return new JavaSourceModel(List.of());
    }

    public EnumModels enumModels() {
        return new EnumModels(enumModels);
    }

    public JavaSourceModel merge(JavaSourceModel other) {
        return new JavaSourceModel(Stream.concat(enumModels.stream(), other.enumModels.stream()).collect(Collectors.toList()));
    }
}
