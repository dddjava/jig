package org.dddjava.jig.cli;

/**
 * 動作モード
 */
public enum Mode {
    /**
     * Mavenのディレクトリ構成にします。
     */
    MAVEN,

    /**
     * 軽量（動作的な意味ではない）
     */
    LIGHT,

    /**
     * 何もしない
     */
    DEFAULT;
}
