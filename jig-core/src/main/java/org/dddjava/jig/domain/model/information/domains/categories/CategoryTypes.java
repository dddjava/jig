package org.dddjava.jig.domain.model.information.domains.categories;

import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.classes.type.JigTypeValueKind;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers.collector;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(List<CategoryType> list) {
        this.list = list;
    }

    public static CategoryTypes from(JigTypes jigTypes) {
        return jigTypes.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分)
                .sorted(Comparator.comparing(JigType::identifier))
                .map(CategoryType::new)
                .collect(collectingAndThen(Collectors.toList(), CategoryTypes::new));
    }

    public List<CategoryType> list() {
        return list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream()
                .map(CategoryType::typeIdentifier)
                .collect(collector());
    }

    public JigTypes jigTypes() {
        // TODO CategoryTypesからJigTypesへの変換は逆な感じなのでなくしたい
        return list.stream().map(categoryType -> categoryType.jigType()).collect(collectingAndThen(toList(), JigTypes::new));
    }
}
