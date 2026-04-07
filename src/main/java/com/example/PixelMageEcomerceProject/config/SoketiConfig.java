package com.example.PixelMageEcomerceProject.config;

import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoketiConfig {

    @Value("${SOKETI_APP_ID}")
    private String appId;

    @Value("${SOKETI_KEY}")
    private String appKey;

    @Value("${SOKETI_SECRET}")
    private String appSecret;

    @Value("${SOKETI_HOST}")
    private String host;

    @Value("${SOKETI_PORT}")
    private int port;

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher(appId, appKey, appSecret);
        if (port != 80 && port != 443) {
            pusher.setHost(host + ":" + port);
        } else {
            pusher.setHost(host);
        }
        pusher.setEncrypted(port == 443); // set to true if Soketi is hosted with SSL
        return pusher;
    }
}
