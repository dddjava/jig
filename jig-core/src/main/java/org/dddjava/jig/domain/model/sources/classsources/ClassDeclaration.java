package org.dddjava.jig.domain.model.sources.classsources;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryGlossaryRepository;

import java.util.Collection;

public record ClassDeclaration(JigTypeHeader jigTypeHeader,
                               Collection<JigFieldHeader> jigFieldHeaders,
                               Collection<JigMethodDeclaration> jigMethodDeclarations) {

    // テスト用
    public JigTypeMembers jigTypeMembers() {
        return jigTypeMembers(new OnMemoryGlossaryRepository());
    }

    public JigTypeMembers jigTypeMembers(GlossaryRepository glossaryRepository) {
        Collection<JigMethod> jigMethods = jigMethodDeclarations.stream()
                .map(jigMethodDeclaration -> new JigMethod(jigMethodDeclaration,
                        glossaryRepository.getMethodTermPossiblyMatches(jigMethodDeclaration.header().id())))
                .toList();
        return new JigTypeMembers(jigFieldHeaders, jigMethods);
    }
}
