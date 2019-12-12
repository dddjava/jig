package org.dddjava.jig.domain.model.jigdocument;

/**
 * アーキテクチャドキュメント
 */
public enum ArchitectureDocument {

    /**
     * アーキテクチャダイアグラム
     */
    ArchitectureDiagram,

    /**
     * パッケージツリーダイアグラム
     *
     * パッケージ階層を可視化する。
     * @deprecated 廃止予定
     */
    @Deprecated
    PackageTreeDiagram;
}
