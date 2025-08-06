package org.dddjava.jig.adapter.thymeleaf.dialect;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.jspecify.annotations.Nullable;
import org.thymeleaf.context.IExpressionContext;

/**
 * entrypoint向けのExpressionObject
 */
class JigEntrypointExpressionObject {
    public static final String NAME = "jigEntrypoint";
    private final IExpressionContext context;

    public JigEntrypointExpressionObject(IExpressionContext context) {
        this.context = context;
    }

    @Nullable
    public String handlePath(JigType jigType) {
        return jigType.annotationValueOf(TypeId.valueOf("org.springframework.web.bind.annotation.RequestMapping"), "value", "path")
                // 空文字列や何も設定されていない場合は "/" として扱う
                .map(value -> value.isBlank() ? "" : value)
                // Thymeleafのifでnullは空になるのでこれを返している。OptionalをThymeleafに処理させたい。
                .orElse(null);
    }

    @Nullable
    public String tagDescription(JigType jigType) {
        return jigType.annotationValueOf(TypeId.valueOf("io.swagger.v3.oas.annotations.tags.Tag"), "description")
                // Thymeleafのifでnullは空になるのでこれを返している。OptionalをThymeleafに処理させたい。
                .orElse(null);
    }
}
