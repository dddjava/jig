package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * entrypoint向けのExpressionObject
 */
class JigEntrypointExpressionObject {

    public String handlePath(JigType jigType) {
        return jigType.annotationValueOf(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"), "value", "path")
                // 空文字列や何も設定されていない場合は "/" として扱う
                .map(value -> value.isBlank() ? "" : value)
                // Thymeleafのifでnullは空になるのでこれを返している。OptionalをThymeleafに処理させたい。
                .orElse(null);
    }

    public String tagDescription(JigType jigType) {
        return jigType.annotationValueOf(TypeIdentifier.valueOf("io.swagger.v3.oas.annotations.tags.Tag"), "description")
                // Thymeleafのifでnullは空になるのでこれを返している。OptionalをThymeleafに処理させたい。
                .orElse(null);
    }
}
