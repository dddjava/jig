package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.domain.model.documents.Diagnostic;
import org.dddjava.jig.domain.model.documents.JigDocument;

import java.util.Collection;
import java.util.List;

/**
 * 解析中に検出した事象（diagnostics-data.js）
 *
 * 0件の理由をブラウザ側で示すために出力する。表示時に言語を切り替えられるよう、
 * ロケールで解決せず日英の両方を持たせる。
 */
public class DiagnosticsDataAdapter {

    public static String variableName() {
        return "diagnosticsData";
    }

    public static String dataFileName() {
        return "diagnostics-data";
    }

    public static String buildJson(Collection<Diagnostic> diagnostics) {
        List<JsonObjectBuilder> builders = diagnostics.stream()
                .map(diagnostic -> Json.object("code", diagnostic.code())
                        .and("error", diagnostic.error())
                        .and("jigDocuments", Json.array(diagnostic.jigDocuments().stream().map(JigDocument::name).toList()))
                        .and("ja", diagnostic.message().japanese())
                        .and("en", diagnostic.message().english()))
                .toList();

        return Json.object("diagnostics", Json.arrayObjects(builders)).build();
    }
}
