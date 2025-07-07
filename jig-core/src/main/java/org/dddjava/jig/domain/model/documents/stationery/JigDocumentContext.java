package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.nio.file.Path;
import java.util.List;

/**
 * ドキュメント出力時のコンテキスト
 *
 * ドキュメント出力の主体となるインスタンスが持っていない情報（特に用語）を提供する役割
 */
public interface JigDocumentContext {

    /**
     * パッケージに対応する用語
     */
    Term packageTerm(PackageId packageId);

    /**
     * 型に対応する用語
     */
    Term typeTerm(TypeIdentifier typeIdentifier);

    /**
     * ドキュメント出力先ディレクトリ
     */
    Path outputDirectory();

    /**
     * 処理対象となるJigDocument
     */
    List<JigDocument> jigDocuments();

    /**
     * ドキュメント内容にかかわるオプション
     */
    JigDiagramOption diagramOption();
}
