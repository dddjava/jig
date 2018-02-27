package jig.presentation.controller;

import jig.application.service.AnalyzeService;
import jig.application.service.DiagramService;
import jig.domain.model.DiagramIdentifier;
import jig.domain.model.DiagramSource;
import jig.domain.model.jdeps.*;
import jig.domain.model.thing.ThingFormatter;
import jig.domain.model.thing.Things;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

@Controller
@RequestMapping("jar2image")
public class Jar2ImageController {

    @Autowired
    AnalyzeService analyzeService;

    @Autowired
    DiagramService service;

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            Path tempFile = Files.createTempFile("jar2imagecontroller", ".jar");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            String pattern = ".+\\.domain\\.model\\..+";
            Things things = analyzeService.toModels(
                    new AnalysisCriteria(
                            new SearchPaths(Collections.singletonList(tempFile)),
                            new AnalysisClassesPattern(pattern),
                            new DependenciesPattern(pattern),
                            AnalysisTarget.PACKAGE));
            ThingFormatter thingFormatter = analyzeService.modelFormatter(Paths.get(""));
            DiagramSource diagramSource = service.toDiagramSource(things, thingFormatter);
            DiagramIdentifier identifier = service.request(diagramSource);
            service.generate(identifier);
            return "redirect:/image/" + identifier.getIdentifier();
        }
    }
}
