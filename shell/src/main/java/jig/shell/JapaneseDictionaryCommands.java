package jig.shell;

import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.javaparser.PackageInfoReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Paths;

@ShellComponent
public class JapaneseDictionaryCommands {

    @Autowired
    JapaneseNameRepository repository;

    @ShellMethod("package-info.javaのJavadocコメントを読み込む")
    public void importPackageInfoJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {
        PackageInfoReader packageInfoReader = new PackageInfoReader(Paths.get(sourceDirectory));
        packageInfoReader.registerTo(repository);
    }

    @ShellMethod("classのJavadocコメントを読み込む")
    public void importClassJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {
        ClassCommentReader classCommentReader = new ClassCommentReader(Paths.get(sourceDirectory));
        classCommentReader.registerTo(repository);
    }

    @ShellMethod("取り込まれた一覧を表示")
    public String showJapaneseDictionary() {
        return repository.all().asText();
    }
}
