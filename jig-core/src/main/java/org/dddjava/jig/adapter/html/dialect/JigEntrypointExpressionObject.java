package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * entrypoint向けのExpressionObject
 */
class JigEntrypointExpressionObject {

    public String handlePath(JigType jigType) {
        var annotations = jigType.annotationsOf(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"));
        return annotations.list().stream()
                // 複数はつけられないので一つで良い
                .findFirst()
                .map(annotation -> annotation.descriptionTextAnyOf("value", "path")
                        // 空文字列や何も設定されていない場合は "/" として扱う
                        .filter(value -> !value.isEmpty()).orElse("/"))
                .orElse(null); // Thymeleafのifでnullは空になるのでこれを返している。OptionalをThymeleafに処理させたい。
    }

    public String tagDescription(JigType jigType) {
        var annotations = jigType.annotationsOf(TypeIdentifier.valueOf("io.swagger.v3.oas.annotations.tags.Tag"));
        return annotations.list().stream().findFirst()
                .flatMap(annotation -> annotation.descriptionTextAnyOf("description"))
                .orElse(null); // Thymeleafのifでnullは空になるのでこれを返している。OptionalをThymeleafに処理させたい。
    }
}
