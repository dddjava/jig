package org.dddjava.jig.cli;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class CliConfigTest {

    @Test
    void testConfiguration() {
        CliConfig original = new CliConfig();
        CliConfig cli = new CliConfig();

        String[] args = {
                "--documentType=ServiceMethodCallHierarchy",
                "--outputDirectory=out",
                "--output.omit.prefix=omit.package",
                "--jig.model.pattern=.+\\.model\\..+",
                "--jig.repository.pattern=\\.DataSource",
                "--project.path=./projectDir",
                "--directory.classes=./classes",
                "--directory.resources=./resources",
                "--directory.sources=./sources",
                "--depth=5",
                "--jig.debug=true",
                "--jig.cli.extra=api-jpa-crud"

        };
        CommandLine.populateCommand(cli, args);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cli.documentTypeText).isEqualTo("ServiceMethodCallHierarchy")
                .isNotEqualTo(original.documentTypeText);
        softly.assertThat(cli.outputDirectory).isEqualTo("out")
                .isNotEqualTo(original.outputDirectory);
        softly.assertThat(cli.outputOmitPrefix).isEqualTo("omit.package")
                .isNotEqualTo(original.outputOmitPrefix);
        softly.assertThat(cli.modelPattern).isEqualTo(".+\\.model\\..+")
                .isNotEqualTo(original.modelPattern);
        softly.assertThat(cli.repositoryPattern).isEqualTo("\\.DataSource")
                .isNotEqualTo(original.repositoryPattern);
        softly.assertThat(cli.projectPath).isEqualTo("./projectDir")
                .isNotEqualTo(original.projectPath);
        softly.assertThat(cli.directoryClasses).isEqualTo("./classes")
                .isNotEqualTo(original.directoryClasses);
        softly.assertThat(cli.directoryResources).isEqualTo("./resources")
                .isNotEqualTo(original.directoryResources);
        softly.assertThat(cli.directorySources).isEqualTo("./sources")
                .isNotEqualTo(original.directorySources);
        softly.assertThat(cli.depth).isEqualTo(5)
                .isNotEqualTo(original.depth);
        softly.assertThat(cli.jigDebugMode).isEqualTo(true)
                .isNotEqualTo(original.jigDebugMode);
        softly.assertThat(cli.jigCliExtra).isEqualTo("api-jpa-crud")
                .isNotEqualTo(original.jigCliExtra);
        softly.assertAll();

    }

}
