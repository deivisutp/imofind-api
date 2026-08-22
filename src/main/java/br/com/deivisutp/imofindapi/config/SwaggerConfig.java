package br.com.deivisutp.imofindapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI imoFindOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Imóveis - Scraping")
                .description("API REST que obtém dados de imóveis")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Deivis Utpadel")
                        .url("https://github.com/deivisutp")
                        .email("deivisutp@gmail.com")));
    }
}
