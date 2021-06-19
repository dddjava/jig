package org.dddjava.jig.domain.model.sources.file.text;

import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSources;
import org.dddjava.jig.domain.model.sources.file.text.javacode.PackageInfoSources;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSources;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSources;

import java.io.IOException;

public class CodeSource {
    @Deprecated
    JavaSources javaSources;
    @Deprecated
    KotlinSources kotlinSources;
    @Deprecated
    ScalaSources scalaSources;
    @Deprecated
    PackageInfoSources packageInfoSources;

    public CodeSource(JavaSources javaSources, KotlinSources kotlinSources, ScalaSources scalaSources, PackageInfoSources packageInfoSources) {
        this.javaSources = javaSources;
        this.kotlinSources = kotlinSources;
        this.scalaSources = scalaSources;
        this.packageInfoSources = packageInfoSources;
    }

    CodeSourceFile codeSourceFile;

    public CodeSource(CodeSourceFile codeSourceFile) {
        this.codeSourceFile = codeSourceFile;
        // TODO read bytes?
    }

    public TextSourceType textSourceType() {
        return codeSourceFile.textSourceType();
    }

    public byte[] readAllBytes() throws IOException {
        return codeSourceFile.getAllBytes();
    }

    public String location() {
        return codeSourceFile.path.toString();
    }
}
