package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.jigmodel.categories.CategoryType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.jigmodel.jigtype.package_.JigPackage;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.summary.SummaryModel;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class SummaryView implements JigView {

    AliasFinder aliasFinder;

    public SummaryView(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
    }

    @Override
    public void render(Object model, JigDocumentWriter jigDocumentWriter) {
        SummaryModel summaryModel = (SummaryModel) model;
        if (summaryModel.empty()) {
            jigDocumentWriter.markSkip();
            return;
        }
        Map<PackageIdentifier, List<JigType>> jigTypeMap = summaryModel.map();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(jigDialect());

        Map<PackageIdentifier, Set<PackageIdentifier>> packageMap = jigTypeMap.keySet().stream()
                .flatMap(packageIdentifier -> packageIdentifier.genealogical().stream())
                .collect(groupingBy(packageIdentifier -> packageIdentifier.parent(), toSet()));

        TreeComposite baseComposite = new TreeComposite(PackageIdentifier.defaultPackage(), aliasFinder);

        createTree(jigTypeMap, packageMap, baseComposite);

        List<JigType> jigTypes = jigTypeMap.values().stream().flatMap(List::stream).collect(toList());

        List<JigPackage> jigPackages = packageMap.values().stream()
                .flatMap(Set::stream)
                .map(packageIdentifier -> new JigPackage(packageIdentifier, aliasFinder.find(packageIdentifier)))
                .collect(toList());

        Map<TypeIdentifier, CategoryType> categoriesMap = jigTypes.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分)
                .map(CategoryType::new)
                .collect(toMap(CategoryType::typeIdentifier, Function.identity()));

        // ThymeleafのContextに設定
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("baseComposite", baseComposite);
        contextMap.put("jigPackages", jigPackages);
        contextMap.put("jigTypes", jigTypes);
        contextMap.put("categoriesMap", categoriesMap);

        Context context = new Context(Locale.ROOT, contextMap);
        String htmlText = templateEngine.process(
                jigDocumentWriter.jigDocument().fileName(), context);

        jigDocumentWriter.writeHtml(outputStream -> {
            outputStream.write(htmlText.getBytes(StandardCharsets.UTF_8));
        });
    }

    class JigDialectObject {
        public String labelText(TypeIdentifier typeIdentifier) {
            TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
            return typeAlias.asTextOrIdentifierSimpleText();
        }
    }

    private IDialect jigDialect() {
        return new IExpressionObjectDialect() {
            @Override
            public String getName() {
                return "jig-dialect";
            }

            @Override
            public IExpressionObjectFactory getExpressionObjectFactory() {
                return new IExpressionObjectFactory() {
                    @Override
                    public Set<String> getAllExpressionObjectNames() {
                        return Collections.singleton("jig");
                    }

                    @Override
                    public Object buildObject(IExpressionContext context, String expressionObjectName) {
                        return new JigDialectObject();
                    }

                    @Override
                    public boolean isCacheable(String expressionObjectName) {
                        return true;
                    }
                };
            }
        };
    }

    private void createTree(Map<PackageIdentifier, List<JigType>> jigTypeMap,
                            Map<PackageIdentifier, Set<PackageIdentifier>> packageMap,
                            TreeComposite baseComposite) {
        for (PackageIdentifier current : packageMap.getOrDefault(baseComposite.packageIdentifier(), Collections.emptySet())) {
            TreeComposite composite = new TreeComposite(current, aliasFinder);
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
