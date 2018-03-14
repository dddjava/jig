package jig.classlist.classlist;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseName;
import jig.domain.model.tag.JapaneseNameRepository;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.ThingTag;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class TypeListNavigator {

    private JapaneseNameRepository japaneseNameRepository;
    private final ThingTag thingTag;
    private RelationRepository relationRepository;

    public TypeListNavigator(ThingTag thingTag, RelationRepository relationRepository, JapaneseNameRepository japaneseNameRepository) {
        this.thingTag = thingTag;
        this.relationRepository = relationRepository;
        this.japaneseNameRepository = japaneseNameRepository;
    }

    public Name name() {
        return thingTag.name();
    }

    public JapaneseName japaneseName() {
        return japaneseNameRepository.get(name());
    }

    public Names usage() {
        Relations fieldRelation = relationRepository.findTo(name(), RelationType.FIELD);
        Relations methodReturnRelation = relationRepository.findTo(name(), RelationType.METHOD_RETURN_TYPE);
        Relations methodParameterRelation = relationRepository.findTo(name(), RelationType.METHOD_PARAMETER);

        return Stream.of(
                fieldRelation.list(),
                methodReturnRelation.list(),
                methodParameterRelation.list()
        ).flatMap(List::stream).map(Relation::from).collect(Names.collector());
    }

    public boolean isTag(Tag tag) {
        return thingTag.matches(tag);
    }

    public enum Concern {
        クラス名(condition -> condition.name().value()),
        クラス和名(condition -> condition.japaneseName().value()),
        使用箇所(condition -> condition.usage().asText()),
        振る舞い有り(condition -> Boolean.toString(condition.isTag(Tag.ENUM_BEHAVIOUR))),
        パラメーター有り(condition -> Boolean.toString(condition.isTag(Tag.ENUM_PARAMETERIZED))),
        多態(condition -> Boolean.toString(condition.isTag(Tag.ENUM_POLYMORPHISM))),;


        private final Function<TypeListNavigator, String> function;

        Concern(Function<TypeListNavigator, String> function) {
            this.function = function;
        }

        public String apply(TypeListNavigator navigator) {
            return function.apply(navigator);
        }
    }
}
