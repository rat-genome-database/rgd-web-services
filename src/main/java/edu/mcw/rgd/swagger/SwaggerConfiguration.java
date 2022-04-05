package edu.mcw.rgd.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by mtutaj on 12/30/2016.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select().apis(
                RequestHandlerSelectors.any()).paths(PathSelectors.any()).build()
                .apiInfo(apiInfo())
                .tags(new Tag("NIHdatacommons","NIH Data Commons"),new Tag("Gene", ""),new Tag("QTL",""),new Tag("Phenotype",""),new Tag("Lookup",""),new Tag("Annotation", ""),new Tag("Pathway",""), new Tag("Rat Strain",""),new Tag("AGR", "Alliance of Genome Resources"));
    }

    private ApiInfo apiInfo() {

        ApiInfoBuilder aib = new ApiInfoBuilder()
                .title("Rat Genome Database REST API")
                .description("The RGD REST API provides programmatic access to information and annotation stored in the Rat Genome Database")
                //"API TOS",
                //"jdepons@mcw.edu",
                .contact(new Contact("Rat Genome Database", "http://rgd.mcw.edu", "RGD.Data2@mcw.edu"))
                .license("Creative Commons")
                .version("1.1")
                .termsOfServiceUrl("http://rgd.mcw.edu/wg/citing-rgd")
                .licenseUrl("https://creativecommons.org/licenses/by/4.0/legalcode");



        ApiInfo apiInfo =  aib.build();



        return apiInfo;
    }
}
