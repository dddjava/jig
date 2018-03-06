package jig.shell;

import jig.domain.model.tag.JapaneseNameDictionary;
import jig.infrastructure.javaparser.ClassCommentLibrary;
import jig.infrastructure.javaparser.PackageInfoLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Paths;

@ShellComponent
public class JapaneseDictionaryCommands {

    @Autowired
    JapaneseNameDictionary dictionary;

    @ShellMethod("package-info.javaのJavadocコメントを読み込む")
    public void importPackageInfoJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {
        PackageInfoLibrary packageInfoLibrary = new PackageInfoLibrary(Paths.get(sourceDirectory));
        dictionary.merge(packageInfoLibrary.borrow());
    }

    @ShellMethod("classのJavadocコメントを読み込む")
    public void importClassJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {
        ClassCommentLibrary packageInfoLibrary = new ClassCommentLibrary(Paths.get(sourceDirectory));
        dictionary.merge(packageInfoLibrary.borrow());
    }

    @ShellMethod("取り込まれた一覧を表示")
    public String showJapaneseDictionary() {
        return dictionary.asText();
    }
}
