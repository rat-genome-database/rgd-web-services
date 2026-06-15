package edu.mcw.rgd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;

// Disable Spring Boot's Elasticsearch auto-configuration: it builds a RestClientTransport against an
// older elasticsearch-java API and breaks against the 9.4.0 client. We create our own client via rgdcore's
// ClientInit instead.
@SpringBootApplication(exclude = {
		ElasticsearchClientAutoConfiguration.class,
		ElasticsearchRestClientAutoConfiguration.class
})
public class RgdRestWebServicesApplication {
	public static void main(String[] args) {
		SpringApplication.run(RgdRestWebServicesApplication.class, args);
	}

}
