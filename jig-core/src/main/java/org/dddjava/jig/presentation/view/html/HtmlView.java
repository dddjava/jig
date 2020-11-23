package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.package_.JigPackage;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.*;

public class HtmlView implements JigView<BusinessRules> {

    AliasFinder aliasFinder;

    public HtmlView(AliasFinder aliasFinder) {
        this.aliasFinder = aliasFinder;
    }

    @Override
    public void render(BusinessRules businessRules, JigDocumentWriter jigDocumentWriter) throws IOException {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Map<PackageIdentifier, List<JigType>> jigTypeMap = businessRules.mapByPackage();
        Map<PackageIdentifier, Set<PackageIdentifier>> packageMap = jigTypeMap.keySet().stream()
                .flatMap(packageIdentifier -> packageIdentifier.genealogical().stream())
                .collect(groupingBy(packageIdentifier -> packageIdentifier.parent(), toSet()));

        TreeComposite baseComposite = new TreeComposite(PackageIdentifier.defaultPackage(), aliasFinder);

        createTree(jigTypeMap, packageMap, baseComposite);

        List<JigPackage> jigPackages = packageMap.values().stream()
                .flatMap(Set::stream)
                .map(packageIdentifier -> new JigPackage(packageIdentifier, aliasFinder.find(packageIdentifier)))
                .collect(toList());
        List<JigType> jigTypes = jigTypeMap.values().stream().flatMap(List::stream).collect(toList());

        // ThymeleafのContextに設定
        Map<String, Object> contextMap = new HashMap<>();
        contextMap.put("node", baseComposite.resolveRootComposite());
        contextMap.put("jigPackages", jigPackages);
        contextMap.put("jigTypes", jigTypes);

        Context context = new Context(Locale.ROOT, contextMap);
        String htmlText = templateEngine.process(
                jigDocumentWriter.jigDocument().fileName(), context);

        jigDocumentWriter.writeHtml(outputStream -> {
            outputStream.write(htmlText.getBytes(StandardCharsets.UTF_8));
        });
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
