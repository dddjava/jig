package org.dddjava.jig.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommandLineApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(CommandLineApplication.class, args);

        var cliRunner = context.getBean(CliRunner.class);
        cliRunner.run();

        System.exit(SpringApplication.exit(context));
    }
}
