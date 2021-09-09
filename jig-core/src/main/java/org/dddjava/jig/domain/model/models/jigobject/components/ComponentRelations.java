package org.dddjava.jig.domain.model.models.jigobject.components;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComponentRelations {

    Map<ComponentType, List<SpecifiedComponent>> componentMap;
    Map<ComponentRelation, List<SpecifiedComponentRelation>> relationListMap;

    public ComponentRelations(Map<ComponentType, List<SpecifiedComponent>> componentMap, Map<ComponentRelation, List<SpecifiedComponentRelation>> relationMap) {
        this.componentMap = componentMap;
        this.relationListMap = relationMap;
    }

    public static ComponentRelations from(JigTypes allJigTypes, ClassRelations allClassRelations) {
        // JigType単独で判別可能なもの
        SpringComponentFactory componentResolver = new SpringComponentFactory();
        Map<ComponentType, List<SpecifiedComponent>> temp = allJigTypes.list().stream().map(componentResolver::create)
                .collect(Collectors.groupingBy(SpecifiedComponent::componentType));
        List<SpecifiedComponent> excluded = temp.remove(ComponentType.EXCLUDE);

        List<SpecifiedComponent> 実装している可能性のあるComponent = temp.values().stream()
                .flatMap(List::stream)
                .filter(SpecifiedComponent::implementer)
                .collect(Collectors.toList());
        Predicate<SpecifiedComponent> Componentで実装されている = specifiedComponent ->
                実装している可能性のあるComponent.stream().anyMatch(implementer -> implementer.implementing(specifiedComponent.identifier()));

        Map<ComponentType, List<SpecifiedComponent>> componentMap = Stream.concat(
                        temp.values().stream().flatMap(List::stream),
                        // Componentで実装されているものをComponentとして扱う
                        excluded.stream()
                                .filter(Componentで実装されている)
                                .map(specifiedComponent -> specifiedComponent.with(ComponentType.IMPLEMENTED)))
                .collect(Collectors.groupingBy(SpecifiedComponent::componentType));

        Map<TypeIdentifier, SpecifiedComponent> resolver = componentMap.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toMap(SpecifiedComponent::identifier, v -> v, (v1, v2) -> v1));
        Map<ComponentRelation, List<SpecifiedComponentRelation>> relationMap = allClassRelations.list().stream()
                .filter(classRelation -> resolver.containsKey(classRelation.from()) && resolver.containsKey(classRelation.to()))
                .map(classRelation -> new SpecifiedComponentRelation(resolver.get(classRelation.from()), resolver.get(classRelation.to())))
                .collect(Collectors.groupingBy(ComponentRelation::new));
        return new ComponentRelations(componentMap, relationMap);
    }

    public String dotText() {
        StringJoiner text = new StringJoiner("\n");
        text.add("node [style=filled shape=box];");

        componentMap.values().stream().flatMap(Collection::stream)
                .map(SpecifiedComponent::nodeText)
                .forEach(text::add);

        for (ComponentRelation componentRelation : relationListMap.keySet()) {
            if (componentRelation.selfRelation()) continue;
            int count = relationListMap.get(componentRelation).size();
            text.add(componentRelation.edgeTextWithNumber(count));
        }
        return text.toString();
    }
}
