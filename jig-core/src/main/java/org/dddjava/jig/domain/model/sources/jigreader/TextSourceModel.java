package org.dddjava.jig.domain.model.sources.jigreader;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModel;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;

import java.util.List;

/**
 * テキストソースから読み取れること
 */
public class TextSourceModel {

    ClassAndMethodComments classAndMethodComments;
    List<EnumModel> enumModels;

    public TextSourceModel(ClassAndMethodComments classAndMethodComments, List<EnumModel> enumModels) {
        this.classAndMethodComments = classAndMethodComments;
        this.enumModels = enumModels;
    }

    public ClassAndMethodComments classAndMethodComments() {
        return classAndMethodComments;
    }

    public EnumModels enumModels() {
        return new EnumModels(enumModels);
    }

    public TextSourceModel addClassAndMethodComments(ClassAndMethodComments... others) {
        var temp = classAndMethodComments;
        for (ClassAndMethodComments other : others) {
            temp = temp.add(other);
        }
        return new TextSourceModel(temp, enumModels);
    }
}
