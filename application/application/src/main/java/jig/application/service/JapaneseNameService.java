package jig.application.service;

import jig.infrastructure.javaparser.PackageInfoParser;
import jig.model.tag.JapaneseNameDictionary;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class JapaneseNameService {

    public JapaneseNameDictionary dictionaryFrom(Path sourceRootPath) {
        PackageInfoParser packageInfoParser = new PackageInfoParser(sourceRootPath);
        return packageInfoParser.parse();
    }
}
