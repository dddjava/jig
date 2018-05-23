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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

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
            RelationText relationText = new RelationText();
            for (ServiceAngle serviceAngle : angles) {
                for (MethodDeclaration methodDeclaration : serviceAngle.userServiceMethods().list()) {
                    relationText.add(methodDeclaration.asFullText(), serviceAngle.method().asFullText());
                }
            }

            // メソッドの表示方法
            String labelText = angles.stream()
                    .map(angle -> {
                        MethodDeclaration method = angle.method();
                        StringJoiner attribute = new StringJoiner(",", "[", "]");

                        if (method.isLambda()) {
                            attribute.add("label=\"(lambda)\"");
                        } else {
                            // ラベルを 和名 + method(ArgumentTypes) : ReturnType にする
                            String methodText = japaneseNameLineOf(method) + method.asSimpleTextWithReturnType();
                            attribute.add("label=\"" + methodText + "\"");
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
                                    .map(serviceAngle -> serviceAngle.method().asFullText())
                                    .map(text -> "\"" + text + "\";")
                                    .collect(joining("\n"))
                                    + "}")
                    .collect(joining("\n"));


            String graphText = new StringJoiner("\n", "digraph JIG {", "}")
                    .add("rankdir=LR;")
                    .add("node [shape=box,style=filled,color=lightgoldenrod];")
                    .add(relationText.asText())
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
