package org.tunnelflow.client.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;

@Component
@RequiredArgsConstructor
public class TunnelFlowRunner implements CommandLineRunner {

    private final RootCommand rootCommand;
    private final ApplicationContext applicationContext;

    @Override
    public void run(String... args) {

        CommandLine commandLine =
                new CommandLine(
                        rootCommand,
                        new PicocliSpringFactory(applicationContext)
                );

        commandLine.execute(args);

    }
}