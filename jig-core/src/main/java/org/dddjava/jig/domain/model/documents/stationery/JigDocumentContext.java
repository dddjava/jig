package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
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

    Term packageTerm(PackageIdentifier packageIdentifier);

    Term typeTerm(TypeIdentifier typeIdentifier);

    Path outputDirectory();

    List<JigDocument> jigDocuments();

    JigDiagramOption diagramOption();
}
