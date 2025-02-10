package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public JigMemberVisibility jigMemberVisibility() {
        return header.jigMethodAttribute().jigMemberVisibility();
    }

    public boolean isAbstract() {
        return header.jigMethodAttribute().isAbstract();
    }

    public Set<TypeIdentifier> associatedTypes() {
        return Stream.concat(
                instructions.associatedTypeStream(),
                header.jigMethodAttribute().associatedTypeStream()
        ).collect(Collectors.toSet());
    }
}
