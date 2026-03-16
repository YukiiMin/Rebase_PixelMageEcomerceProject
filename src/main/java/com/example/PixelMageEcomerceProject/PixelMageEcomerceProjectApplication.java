package com.example.PixelMageEcomerceProject;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.PixelMageEcomerceProject.config.DotEvnConfig;

@SpringBootApplication
public class PixelMageEcomerceProjectApplication {

	public static void main(String[] args) {
		// Load environment variables from .env file before Spring starts
		DotEvnConfig.loadEnv();
		SpringApplication.run(PixelMageEcomerceProjectApplication.class, args);
	}

	@Bean
	public ApplicationRunner testRedis(StringRedisTemplate redis) {
		return args -> {
			redis.opsForValue().set("test", "hello-railway");
			String val = redis.opsForValue().get("test");
			System.out.println("✅ Redis OK: " + val);
		};
	}
}
