package org.tunnelflow.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TunnelCreationService {

    public void createTunnel(String clientId) {

        log.info("Creating tunnel for client {}", clientId);

    }
}