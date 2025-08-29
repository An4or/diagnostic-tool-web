package com.intervale.diagnostictool.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI diagnosticToolOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Diagnostic Tool API")
                        .description("API для системы управления диагностикой оборудования")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@intervale.ru")
                                .url("https://www.intervale.ru"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
