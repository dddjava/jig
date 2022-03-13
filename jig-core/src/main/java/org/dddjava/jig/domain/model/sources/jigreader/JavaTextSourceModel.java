package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;

import java.util.List;

public class JavaTextSourceModel {
    List<EnumModel> enums;
    ClassAndMethodComments classAndMethodComments;

    public JavaTextSourceModel(List<EnumModel> enums, ClassAndMethodComments classAndMethodComments) {
        this.enums = enums;
        this.classAndMethodComments = classAndMethodComments;
    }

    public ClassAndMethodComments classAndMethodComments() {
        return classAndMethodComments;
    }

    public List<EnumModel> enumModels() {
        return enums;
    }
}
