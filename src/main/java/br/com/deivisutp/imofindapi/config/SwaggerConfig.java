package br.com.deivisutp.imofindapi.config;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final String BASE_PACKAGE = "br.com.deivisutp.brasileiraoapi.controller";
    private static final String API_TITULO = "Imóveis - Scraping";
    private static final String API_DESCRICAO = "API REST que obtem dados de  imóveis";
    private static final String API_VERSAO = "1.0.0";
    private static final String CONTATO_NOME = "Deivis Utpadel";
    private static final String CONTATO_GITHUB = "https://github.com/deivisutp";
    private static final String CONTATO_EMAIL = "deivisutp@gmail.com";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(basePackage(BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(buildApiInfo());
    }

    private ApiInfo buildApiInfo() {
        return new ApiInfoBuilder()
                .title(API_TITULO)
                .description(API_DESCRICAO)
                .version(API_VERSAO)
                .contact(new Contact(CONTATO_NOME, CONTATO_GITHUB, CONTATO_EMAIL))
                .build();
    }
}
