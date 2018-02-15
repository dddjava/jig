package jig;

import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        Path path = Paths.get("hoge.png");
        try (OutputStream png = Files.newOutputStream(path)) {
            String source = "@startuml\n" +
                    "hoge ..> fuga\n" +
                    "@enduml\n";

            SourceStringReader reader = new SourceStringReader(source);
            String desc = reader.generateImage(png);

            if (desc == null) {
                throw new IllegalArgumentException(source);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
