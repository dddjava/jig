package org.dddjava.jig.domain.model.documents;

import java.util.List;

/**
 * 解析中に検出した、出力結果を読み解くために必要な事象。
 *
 * ログに流すだけでは生成物を受け取った人に伝わらないため、ドキュメントにも埋め込む。
 *
 * @param code        識別子。表示には使わない
 * @param error       解析そのものが成立していない場合にtrue
 * @param jigDocuments 影響するドキュメント。空の場合は全体に影響する
 * @param message     利用者向けの説明
 */
public record Diagnostic(String code, boolean error, List<JigDocument> jigDocuments, LocalizedMessage message) {
}
