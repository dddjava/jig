package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JigPropertyLoaderTest")
class JigPropertyLoaderTest {

    @TempDir
    Path tempDir;

    Path userHomeBackup;
    Path userDirBackup;

    @BeforeEach
    void setUp() {
        userHomeBackup = Paths.get(System.getProperty("user.home"));
        userDirBackup = Paths.get(System.getProperty("user.dir"));
        System.setProperty("user.home", tempDir.toAbsolutePath().toString());
        System.setProperty("user.dir", tempDir.toAbsolutePath().toString());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.home", userHomeBackup.toAbsolutePath().toString());
        System.setProperty("user.dir", userDirBackup.toAbsolutePath().toString());
    }

    @Test
    void defaultInstance_domainPatternIsEmpty() {
        JigProperties defaultProps = JigProperties.defaultInstance();
        assertTrue(defaultProps.getDomainPattern().isEmpty());
    }

    @Test
    void load_primaryPropertyOverridesDefault() {
        // given
        JigProperties primaryProperties = new JigProperties(
                List.of(JigDocument.ApplicationList),
                Optional.of("com.example.primary.+"),
                tempDir.resolve("primary_output")
        );
        JigPropertyLoader loader = new JigPropertyLoader(primaryProperties);

        // when
        JigProperties loadedProperties = loader.load();

        // then
        assertEquals("com.example.primary.+", loadedProperties.getDomainPattern().get());
        assertEquals(List.of(JigDocument.ApplicationList), loadedProperties.jigDocuments);
    }

    @Test
    void load_homeDirectoryPropertyOverridesDefault() throws IOException {
        // given
        Path homeConfigDir = tempDir.resolve(".jig");
        Files.createDirectory(homeConfigDir);
        Properties homeProps = new Properties();
        homeProps.setProperty("jig.pattern.domain", "com.example.home.+");
        homeProps.setProperty("jig.document.types", "BusinessRuleList");
        homeProps.store(Files.newOutputStream(homeConfigDir.resolve("jig.properties")), "");

        JigPropertyLoader loader = new JigPropertyLoader(new JigProperties(List.of(), Optional.empty(), tempDir)); // Empty primary
        
        // when
        JigProperties loadedProperties = loader.load();

        // then
        assertEquals("com.example.home.+", loadedProperties.getDomainPattern().get());
        assertEquals(List.of(JigDocument.BusinessRuleList), loadedProperties.jigDocuments);
    }

    @Test
    void override_optionalEmptyInOverridePropertiesDoesNotOverrideExistingValue() {
        // given
        JigProperties baseProperties = new JigProperties(
                List.of(JigDocument.ApplicationList),
                Optional.of("com.example.base.+"),
                tempDir.resolve("base_output")
        );
        JigProperties overrideProperties = new JigProperties(
                List.of(), // jigDocuments will be empty, not overriding
                Optional.empty(), // domainPattern will be Optional.empty, should NOT override base
                null // outputDirectory will be null, not overriding
        );

        // when
        baseProperties.override(overrideProperties);

        // then
        assertEquals("com.example.base.+", baseProperties.getDomainPattern().get()); // Should remain unchanged
        assertEquals(List.of(JigDocument.ApplicationList), baseProperties.jigDocuments); // Should remain unchanged
    }

    @Test
    void override_optionalPresentInOverridePropertiesOverridesExistingValue() {
        // given
        JigProperties baseProperties = new JigProperties(
                List.of(JigDocument.ApplicationList),
                Optional.of("com.example.base.+"),
                tempDir.resolve("base_output")
        );
        JigProperties overrideProperties = new JigProperties(
                List.of(),
                Optional.of("com.example.overridden.+"), // This should override base
                null
        );

        // when
        baseProperties.override(overrideProperties);

        // then
        assertEquals("com.example.overridden.+", baseProperties.getDomainPattern().get()); // Should be overridden
    }
}
