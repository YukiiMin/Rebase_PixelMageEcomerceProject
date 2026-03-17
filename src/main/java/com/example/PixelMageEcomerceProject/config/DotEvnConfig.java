package com.example.PixelMageEcomerceProject.config;

import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DotEvnConfig {
        public static void loadEnv() {
                Dotenv dotenv = Dotenv
                                .configure()
                                .filename(".env")
                                .ignoreIfMissing()
                                .load();
                dotenv
                                .entries()
                                .forEach(entry -> System.setProperty(
                                                entry.getKey(),
                                                entry.getValue()));
        }
}
