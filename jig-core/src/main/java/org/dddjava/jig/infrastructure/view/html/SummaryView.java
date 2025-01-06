package org.dddjava.jig.infrastructure.view.html;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.application.JigView;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.summaries.Summaries;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.models.jigobject.package_.JigPackage;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

/**
 * 概要View
 *
 * 概要HTMLで出力するパッケージツリーと詳細のモデルを加工します。
 */
public class SummaryView implements JigView {

    protected final JigDocumentContext jigDocumentContext;
    private final JigDocument jigDocument;
    private final TemplateEngine templateEngine;
    private final Map<String, Object> contextMap;

    public SummaryView(JigDocument jigDocument, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.jigDocument = jigDocument;
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
        this.contextMap = new ConcurrentHashMap<>();
    }

    @Override
    public JigDocument jigDocument() {
        return jigDocument;
    }

    @Override
    public void render(Object model, JigDocumentWriter jigDocumentWriter) {
        if (model instanceof SummaryModel summaryModel) {
            summaryModel(summaryModel, jigDocumentWriter);
        } else if (model instanceof Summaries summaries) {
            var treeNode = TreeNode.from(summaries.listJigTypes());

            write(jigDocumentWriter);
        }
    }

    private void summaryModel(SummaryModel summaryModel, JigDocumentWriter jigDocumentWriter) {
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
        putContext("model", summaryModel);
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

    protected void write(JigDocumentWriter jigDocumentWriter) {
        contextMap.put("title", jigDocumentWriter.jigDocument().label());
        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
    }

    protected void putContext(String key, Object variable) {
        contextMap.put(key, variable);
    }
}
