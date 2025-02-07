package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigMethodHeader;

import java.util.Collection;

public record JigTypeMembers(
        Collection<JigFieldHeader> jigFieldHeaders,
        Collection<JigMethodHeader> jigMethodHeaders,
        // 互換のため
        JigStaticMember jigStaticMember,
        // 互換のため
        JigInstanceMember jigInstanceMember
) {
}
