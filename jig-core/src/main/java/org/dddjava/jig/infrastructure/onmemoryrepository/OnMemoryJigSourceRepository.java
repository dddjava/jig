package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.application.CommentRepository;
import org.dddjava.jig.application.JigSourceRepository;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.term.Term;
import org.dddjava.jig.domain.model.parts.term.TermIdentifier;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryJigSourceRepository implements JigSourceRepository {

    CommentRepository commentRepository;

    public OnMemoryJigSourceRepository(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    Map<TermIdentifier, Term> termMap = new HashMap<>();

    private void registerTerm(Term term) {
        termMap.put(term.identifier(), term);
    }

    @Override
    public Terms terms() {
        return new Terms(new ArrayList<>(termMap.values()));
    }

    @Override
    public void registerTextSourceModel(TextSourceModel textSourceModel) {
        for (ClassComment classComment : textSourceModel.classCommentList()) {
            // TODO typeFactsに登録したものを使用するようになれば要らなくなるはず
            commentRepository.register(classComment);

            registerTerm(Term.fromClass(classComment.typeIdentifier(), classComment.asTextOrIdentifierSimpleText(), classComment.documentationComment().bodyText()));
        }

        for (MethodComment methodComment : textSourceModel.methodCommentList()) {
            registerTerm(Term.fromMethod(methodComment.methodIdentifier(),
                    methodComment.asTextOrDefault(methodComment.methodIdentifier().methodSignature().methodName()), methodComment.documentationComment().bodyText()));
        }

        for (PackageComment packageComment : textSourceModel.packageComments()) {
            commentRepository.register(packageComment);
            registerTerm(Term.fromPackage(packageComment.packageIdentifier(), packageComment.summaryOrSimpleName(), packageComment.descriptionComment().bodyText()));
        }
    }

}
