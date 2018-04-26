package jig.diagram.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import jig.domain.model.angle.ServiceAngle;
import jig.domain.model.angle.ServiceAngles;
import jig.domain.model.declaration.method.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

public class ServiceMethodCallHierarchyWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMethodCallHierarchyWriter.class);

    public void write(ServiceAngles serviceAngles, OutputStream outputStream) {
        try {
            List<ServiceAngle> angles = serviceAngles.list();

            // メソッド間の関連
            String serviceMethodRelationText = angles.stream()
                    .filter(serviceAngle -> !serviceAngle.userServiceMethods().list().isEmpty())
                    .flatMap(serviceAngle ->
                            serviceAngle.userServiceMethods().list().stream().map(userServiceMethod ->
                                    String.format("\"%s\" -> \"%s\";",
                                            userServiceMethod.asFullText(),
                                            serviceAngle.method().asFullText())
                            ))
                    .collect(joining("\n"));

            // メソッドの表示方法
            String labelText = angles.stream()
                    .map(angle -> {
                        // ラベルを method(ArgumentType) : ReturnType にする
                        // ハンドラを強調（赤色）
                        MethodDeclaration method = angle.method();
                        StringJoiner attribute = new StringJoiner(",", "[", "]");
                        attribute.add("label=\"" + method.asSimpleTextWith(angle.returnType()) + "\"");
                        if (angle.usingFromController().isSatisfy()) {
                            attribute.add("color=red");
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
                                    + "label=\"" + entry.getKey().asSimpleText() + "\";"
                                    + entry.getValue().stream()
                                    .map(serviceAngle -> serviceAngle.method().asFullText())
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
}
