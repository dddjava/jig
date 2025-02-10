package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;

public record ClassDeclaration(JigMemberBuilder jigMemberBuilder, JigTypeHeader jigTypeHeader) {

    // JigMemberBuilderにコメントを登録する必要があるため、コンストラクタの時点でJigTypeMembersにすることはできない
    // JigTypeMembersからJigStaticMember,JigInstanceMemberをなくせたらこれを直接レコードコンポーネントにすることができる
    public JigTypeMembers jigTypeMembers() {
        return jigMemberBuilder.buildJigTypeMembers();
    }
}
