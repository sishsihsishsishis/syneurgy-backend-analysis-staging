package com.aws.sync.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;


@Configuration
public class OpenApiConfig {
    @Bean(value = "indexApi")
    public Docket indexApi() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("Interface group").apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.aws.sync.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("sync Project API")
                .description("english")
                .version("1.0")
                .build();
    }

}