package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringJoiner;

public class ServiceAngleToImageView implements JigView<ServiceAngles> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAngleToImageView.class);

    final JapaneseNameRepository japaneseNameRepository;

    public ServiceAngleToImageView(JapaneseNameRepository japaneseNameRepository) {
        this.japaneseNameRepository = japaneseNameRepository;
    }


    @Override
    public void render(ServiceAngles serviceAngles, OutputStream outputStream) {
        try {
            List<ServiceAngle> angles = serviceAngles.list();

            // メソッド間の関連
            String serviceMethodRelationText = angles.stream()
                    .filter(serviceAngle -> !serviceAngle.userServiceMethods().list().isEmpty())
                    .flatMap(serviceAngle ->
                            serviceAngle.userServiceMethods().list().stream().map(userServiceMethod ->
                                String.format("\"%s%s\" -> \"%s%s\";",
                                        japaneseNameLineOf(userServiceMethod),
                                        userServiceMethod.asFullText(),
                                        japaneseNameLineOf(serviceAngle.method()),
                                        serviceAngle.method().asFullText())
                            ))
                    .collect(joining("\n"));

            // メソッドの表示方法
            String labelText = angles.stream()
                    .map(angle -> {
                        MethodDeclaration method = angle.method();
                        StringJoiner attribute = new StringJoiner(",", "[", "]");

                        if (method.isLambda()) {
                            attribute.add("label=\"(lambda)\"");
                        } else {
                            // ラベルを method(ArgumentType) : ReturnType にする
                            attribute.add("label=\"" + method.asSimpleTextWithReturnType() + "\"");
                            // ハンドラを強調（赤色）
                            if (angle.usingFromController().isSatisfy()) {
                                attribute.add("color=red");
                            }
                        }
                        return String.format("\"%s\" " + attribute + ";",
                                method.asFullText(),
                                method.asSimpleText());
                    }).collect(joining("\n"));

            // クラス名でグルーピングする
            String subgraphText = angles.stream()
                    .collect(groupingBy(serviceAngle -> serviceAngle.method().declaringType()))
                    .entrySet().stream()
                    .map(entry ->
                            "subgraph \"cluster_" + entry.getKey().fullQualifiedName() + "\""
                                    + "{"
                                    + "label=\"" + japaneseNameLineOf(entry.getKey()) + entry.getKey().asSimpleText() + "\";"
                                    + entry.getValue().stream()
                                    .map(serviceAngle ->
                                            japaneseNameLineOf(serviceAngle.method()) + serviceAngle.method().asFullText())
                                    .map(text -> "\"" + text + "\";")
                                    .collect(joining("\n"))
                                    + "}")
                    .collect(joining("\n"));


            String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                    .add("rankdir=LR;")
                    .add("node [shape=box,style=filled,color=lightgoldenrod];")
                    .add(serviceMethodRelationText)
                    .add(labelText)
                    .add(subgraphText)
                    .toString();

            LOGGER.debug(graphText);

            Graphviz.fromString(graphText)
                    .render(Format.PNG)
                    .toOutputStream(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String japaneseNameLineOf(TypeIdentifier typeIdentifier) {
        String japaneseName = japaneseNameRepository.get(typeIdentifier).summarySentence();
        return japaneseName.isEmpty() ? "" : japaneseName + "\n";
    }

    private String japaneseNameLineOf(MethodDeclaration method) {
        String japaneseName = japaneseNameRepository.get(method).summarySentence();
        return japaneseName.isEmpty() ? "" : japaneseName + "\n";
    }
}
