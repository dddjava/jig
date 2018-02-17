package jig.presentation.controller;

import jig.analizer.dependency.JapaneseNameRepository;
import jig.analizer.dependency.Models;
import jig.analizer.jdeps.JdepsExecutor;
import jig.analizer.jdeps.JdepsResult;
import jig.analizer.plantuml.PlantUmlModelFormatter;
import jig.analizer.plantuml.PlantUmlModelNameFormatter;
import jig.application.service.DiagramService;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static jig.analizer.PackageDependency.DEFAULT_TARGET_PREFIX;

@Controller
@RequestMapping("jar2image")
public class Jar2ImageController {

    @Autowired
    DiagramService service;

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            Path tempFile = Files.createTempFile("jar2imagecontroller", ".jar");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            String targetPattern = DEFAULT_TARGET_PREFIX + "\\.(.*)";

            JdepsExecutor jdepsExecutor = new JdepsExecutor(targetPattern, targetPattern, tempFile.toAbsolutePath().toString());
            JdepsResult jdepsResult = jdepsExecutor.execute();

            Models models = jdepsResult.toModels();

            JapaneseNameRepository japaneseNameRepository = new JapaneseNameRepository();

            String text = models.format(new PlantUmlModelFormatter(new PlantUmlModelNameFormatter(targetPattern, japaneseNameRepository)));
            DiagramSource diagramSource = new DiagramSource("@startuml\n" + text + "\n@enduml");
            DiagramIdentifier identifier = service.generateImmediately(diagramSource);
            return "redirect:/image/" + identifier.getIdentifier();
        }
    }
}
