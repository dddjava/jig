package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

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

    public Set<TypeId> associatedTypes() {
        return Stream.concat(
                instructions.associatedTypeStream(),
                header.associatedTypeStream()
        ).collect(toSet());
    }

    public Stream<JigTypeReference> argumentStream() {
        return header.argumentList().stream();
    }

    public TypeId declaringTypeId() {
        return header.id().tuple().declaringTypeId();
    }

}
