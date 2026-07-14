package org.tunnelflow.client.cli.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
@RequiredArgsConstructor
public class PicocliFactory implements CommandLine.IFactory {

    private final ApplicationContext applicationContext;

    @Override
    public <K> K create(Class<K> cls) throws Exception {

        return applicationContext.getBean(cls);

    }

}