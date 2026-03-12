package com.example.PixelMageEcomerceProject;

import com.example.PixelMageEcomerceProject.config.DotEvnConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PixelMageEcomerceProjectApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file before Spring starts
		DotEvnConfig.loadEnv();
		SpringApplication.run(PixelMageEcomerceProjectApplication.class, args);
	}
}
