package edu.mcw.rgd.configuration;



import java.net.UnknownHostException;
import java.util.List;

import edu.mcw.rgd.services.RgdContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

//    @Value("${rgd.openapi.dev-url}")
//    private String devUrl;
//
//    @Value("${rgd.openapi.prod-url}")
//    private String prodUrl;

    @Bean
    public OpenAPI myOpenAPI() throws UnknownHostException {
      Server server=new Server();
        if(RgdContext.isProduction()){
            server.setUrl("https://rest.rgd.mcw.edu/rgdws");
            server.description("Production Server");
        }else{
            server.setUrl(RgdContext.getHostname()+"/rgdws");
            server.description("Internal Server");

        }

        Contact contact = new Contact();
        contact.setEmail("RGD.Data2@mcw.edu");
        contact.setName("Rat Genome Database");
        contact.setUrl("https://rgd.mcw.edu");

        License creativeCommonsLicense = new License().name("Creative Commons License").url("https://creativecommons.org/licenses/by/4.0/legalcode");

        Info info = new Info()
                .title("Rat Genome Database REST API")
                .version("1.0")
                .contact(contact)
                .description("The RGD REST API provides programmatic access to information and annotation stored in the Rat Genome Database.").termsOfService("http://rgd.mcw.edu/wg/citing-rgd")
                .license(creativeCommonsLicense);

        return new OpenAPI().info(info).servers(List.of(server));
    }
}

