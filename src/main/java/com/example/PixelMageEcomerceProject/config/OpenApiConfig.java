package com.example.PixelMageEcomerceProject.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(info = @Info(title = "PixelMage E-commerce API", version = "2.0", description = "API documentation for PixelMage E-commerce Project", contact = @Contact(name = "PixelMage Team", email = "hoangtuanminh1104@gmail.com"), license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")), servers = {
                @Server(description = "Local Development Server", url = "http://localhost:8080"),
                @Server(description = "Production Server", url = "https://pixelmageecomerceproject-production-0c78.up.railway.app")
})
@SecurityScheme(name = "bearerAuth", description = "JWT Bearer Token Authentication", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {
}
