package jig.domain.model.list;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseName;
import jig.domain.model.tag.JapaneseNameRepository;
import jig.domain.model.thing.Name;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class MethodRelationNavigator {

    private Relation methodRelation;
    private RelationRepository relationRepository;
    private JapaneseNameRepository japaneseNameRepository;

    public MethodRelationNavigator(Relation methodRelation, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.methodRelation = methodRelation;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Name className() {
        return methodRelation.from();
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(methodRelation.from());
    }

    public Name methodName() {
        return methodRelation.to();
    }

    public Name returnTypeName() {
        Relation relation = relationRepository.get(methodRelation.to(), RelationType.METHOD_RETURN_TYPE);
        return relation.to();
    }

    public List<Name> instructFields() {
        Relations relations = relationRepository.find(methodRelation.to(), RelationType.METHOD_USE_TYPE);
        return relations.list().stream().map(Relation::to).collect(toList());
    }

    public enum Concern {
        クラス名(condition -> condition.className().value()),
        クラス和名(condition -> condition.japaneseName().value()),
        メソッド(condition -> condition.methodName().shortText()),
        メソッド戻り値の型(condition -> condition.returnTypeName().value()),
        使用しているフィールドの型(condition -> condition.instructFields().stream().map(Name::value).collect(joining(",")));

        private final Function<MethodRelationNavigator, String> function;

        Concern(Function<MethodRelationNavigator, String> function) {
            this.function = function;
        }

        public String apply(MethodRelationNavigator navigator) {
            return function.apply(navigator);
        }
    }
}
