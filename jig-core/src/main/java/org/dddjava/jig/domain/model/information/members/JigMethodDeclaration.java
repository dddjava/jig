package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
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
        return header.jigMemberVisibility();
    }

    public boolean isAbstract() {
        return header.isAbstract();
    }

    public Set<TypeIdentifier> associatedTypes() {
        return Stream.concat(
                instructions.associatedTypeStream(),
                header.associatedTypeStream()
        ).collect(Collectors.toSet());
    }

    public String nameAndArgumentSimpleText() {
        return header.nameAndArgumentSimpleText();
    }

    public Stream<JigTypeReference> argumentStream() {
        return header.argumentList().stream();
    }

    public TypeIdentifier declaringTypeIdentifier() {
        return header.id().tuple().declaringTypeIdentifier();
    }
}
