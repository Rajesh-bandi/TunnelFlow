package org.tunnelflow.tunnelflowserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class TunnelFlowServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TunnelFlowServerApplication.class, args);
    }
    @Bean
    CommandLineRunner mappings(RequestMappingHandlerMapping mapping) {
        return args -> mapping.getHandlerMethods()
                .forEach((info, method) ->
                        System.out.println(info + " -> " + method));
    }
}
