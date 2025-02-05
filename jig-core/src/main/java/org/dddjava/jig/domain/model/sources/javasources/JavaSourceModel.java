package org.dddjava.jig.domain.model.sources.javasources;

import org.dddjava.jig.domain.model.data.classes.method.MethodImplementation;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Term;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * javaファイル由来のソース
 */
public class JavaSourceModel {

    private final Term term;
    public List<MethodImplementation> methodImplementations;
    List<EnumModel> enumModels;

    private JavaSourceModel(Term term, List<MethodImplementation> methodImplementations, List<EnumModel> enumModels) {
        this.term = term;
        this.methodImplementations = methodImplementations;
        this.enumModels = enumModels;
    }

    public static JavaSourceModel from(Term term, List<MethodImplementation> methodImplementations, List<EnumModel> enumModels) {
        return new JavaSourceModel(term, methodImplementations, enumModels);
    }

    public static JavaSourceModel empty() {
        return new JavaSourceModel(null, List.of(), List.of());
    }

    public EnumModels enumModels() {
        return new EnumModels(enumModels);
    }

    public JavaSourceModel merge(JavaSourceModel other) {
        return new JavaSourceModel(
                null,
                Stream.concat(methodImplementations.stream(), other.methodImplementations.stream()).collect(Collectors.toList()),
                Stream.concat(enumModels.stream(), other.enumModels.stream()).collect(Collectors.toList())
        );
    }

    public Optional<Term> term() {
        return Optional.ofNullable(term);
    }
}
