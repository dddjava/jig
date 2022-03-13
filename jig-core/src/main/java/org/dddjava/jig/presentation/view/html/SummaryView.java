package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.models.jigobject.package_.JigPackage;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.presentation.view.handler.JigDocumentWriter;
import org.dddjava.jig.presentation.view.handler.JigView;
import org.thymeleaf.TemplateEngine;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class SummaryView extends AbstractThymeleafView implements JigView {

    JigDocumentContext jigDocumentContext;

    public SummaryView(TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        super(templateEngine);
        this.jigDocumentContext = jigDocumentContext;
    }

    @Override
    public void render(Object model, JigDocumentWriter jigDocumentWriter) {
        SummaryModel summaryModel = (SummaryModel) model;
        if (summaryModel.empty()) {
            jigDocumentWriter.markSkip();
            return;
        }
        Map<PackageIdentifier, List<JigType>> jigTypeMap = summaryModel.map();

        Map<PackageIdentifier, Set<PackageIdentifier>> packageMap = jigTypeMap.keySet().stream()
                .flatMap(packageIdentifier -> packageIdentifier.genealogical().stream())
                .collect(groupingBy(packageIdentifier -> packageIdentifier.parent(), toSet()));

        TreeComposite baseComposite = new TreeComposite(jigDocumentContext.jigPackage(PackageIdentifier.defaultPackage()));

        createTree(jigTypeMap, packageMap, baseComposite);

        List<JigType> jigTypes = jigTypeMap.values().stream().flatMap(List::stream)
                .sorted(Comparator.comparing(JigType::fqn))
                .collect(toList());

        List<JigPackage> jigPackages = packageMap.values().stream()
                .flatMap(Set::stream)
                .sorted(Comparator.comparing(PackageIdentifier::asText))
                .map(packageIdentifier -> jigDocumentContext.jigPackage(packageIdentifier))
                .collect(toList());

        Map<TypeIdentifier, CategoryType> categoriesMap = jigTypes.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分)
                .map(CategoryType::new)
                .collect(toMap(CategoryType::typeIdentifier, Function.identity()));

        putContext("baseComposite", baseComposite);
        putContext("jigPackages", jigPackages);
        putContext("jigTypes", jigTypes);
        putContext("categoriesMap", categoriesMap);
        putContext("enumModels", summaryModel.enumModels());
        write(jigDocumentWriter);
    }

    private void createTree(Map<PackageIdentifier, List<JigType>> jigTypeMap,
                            Map<PackageIdentifier, Set<PackageIdentifier>> packageMap,
                            TreeComposite baseComposite) {
        for (PackageIdentifier current : packageMap.getOrDefault(baseComposite.packageIdentifier(), Collections.emptySet())) {
            TreeComposite composite = new TreeComposite(jigDocumentContext.jigPackage(current));
            // add package
            baseComposite.addComponent(composite);
            // add class
            for (JigType jigType : jigTypeMap.getOrDefault(current, Collections.emptyList())) {
                composite.addComponent(new TreeLeaf(jigType));
            }
            createTree(jigTypeMap, packageMap, composite);
        }
    }
}
