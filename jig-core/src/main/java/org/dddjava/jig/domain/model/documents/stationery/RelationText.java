package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * 関連
 */
public class RelationText {

    List<String> list;

    public RelationText() {
        this("");
    }

    public RelationText(String attribute) {
        list = new ArrayList<>();
        list.add(attribute);
    }

    private void add(String from, String to) {
        // "hoge" -> "fuga";
        String line = '"' + from + '"' + " -> " + '"' + to + '"' + ';';
        list.add(line);
    }

    public String dotText() {
        return String.join("\n", list);
    }

    // TODO なんか名前に違和感
    public String uniqueDotText() {
        return list.stream().sorted().distinct().collect(joining("\n"));
    }

    public void add(PackageId from, PackageId to) {
        add(from.asText(), to.asText());
    }

    public void add(JigMethodId from, TypeId to) {
        add(from.value(), to.fqn());
    }

    public void add(JigMethodId from, JigMethodId to) {
        add(from.value(), to.value());
    }
}
