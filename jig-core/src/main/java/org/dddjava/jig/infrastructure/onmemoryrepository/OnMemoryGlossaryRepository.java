package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.classes.type.JigTypeTerms;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;
import org.dddjava.jig.domain.model.sources.javasources.comment.PackageComment;

import java.util.*;

@Repository
public class OnMemoryGlossaryRepository implements GlossaryRepository {

    private final Collection<Term> terms = new ArrayList<>();

    final Map<TypeIdentifier, ClassComment> map = new HashMap<>();
    final Map<PackageIdentifier, PackageComment> packageMap = new HashMap<>();

    @Override
    public ClassComment get(TypeIdentifier typeIdentifier) {
        return map.getOrDefault(typeIdentifier, ClassComment.empty(typeIdentifier));
    }

    @Override
    public boolean exists(PackageIdentifier packageIdentifier) {
        return packageMap.containsKey(packageIdentifier);
    }

    @Override
    public PackageComment get(PackageIdentifier packageIdentifier) {
        return packageMap.getOrDefault(packageIdentifier, PackageComment.empty(packageIdentifier));
    }

    @Override
    public void register(ClassComment classComment) {
        map.put(classComment.typeIdentifier(), classComment);
    }

    @Override
    public void register(PackageComment packageComment) {
        packageMap.put(packageComment.packageIdentifier(), packageComment);
    }

    @Override
    public JigTypeTerms collectJigTypeTerms(TypeIdentifier typeIdentifier) {
        // 型に紐づくTermを収集する。
        // 現在本クラスは扱っていないが、フィールドおよびメソッドのコメントも含むようにする。
        List<Term> list = map.entrySet().stream()
                .filter(entry -> entry.getKey().equals(typeIdentifier))
                .map(Map.Entry::getValue)
                // GlossaryでTermを直接もつようになるまで一旦この変換
                .map(classComment -> Term.fromClass(
                        classComment.typeIdentifier(),
                        classComment.asTextOrIdentifierSimpleText(),
                        classComment.documentationComment().bodyText()))
                .toList();
        return new JigTypeTerms(list);
    }

    @Override
    public void register(Term term) {
        terms.add(term);
    }
}
