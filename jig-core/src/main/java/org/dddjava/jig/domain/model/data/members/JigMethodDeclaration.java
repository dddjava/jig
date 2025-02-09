package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;

/**
 * メソッド定義
 *
 * @param header メソッドヘッダ。JLSのModifierを含む。
 * @param instructions メソッドで実行される命令
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.4">Method Declaration</a>
 */
public record JigMethodDeclaration(JigMethodHeader header, Instructions instructions) {
    public String name() {
        return header.name();
    }
}
