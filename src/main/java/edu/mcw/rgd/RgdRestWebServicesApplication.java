package edu.mcw.rgd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Database access goes through rgdcore's DataSourceFactory (the Tomcat JNDI datasources, e.g. jdbc/rgd2),
 * so Spring Boot must not build its own pool from spring.datasource.* settings in a deployed
 * application.properties: an unusable one (such as an Oracle driver that is no longer bundled) stops the
 * whole application from starting.
 *
 * Elasticsearch access likewise goes through rgdcore's ClientInit, so the Elasticsearch autoconfiguration
 * is excluded as well. Merely having elasticsearch-java on the classpath switches it on, and it would
 * otherwise build a client against the default localhost:9200. Worse, Spring Boot 3.1.x was compiled
 * against the 8.x client and calls RestClientTransport(RestClient, JsonpMapper, TransportOptions), a
 * constructor whose third parameter is RestClientOptions in the 9.x client this application bundles: the
 * resulting NoSuchMethodError stops the whole application from starting.
 */
@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		ElasticsearchRestClientAutoConfiguration.class,
		ElasticsearchClientAutoConfiguration.class
})
public class RgdRestWebServicesApplication {
	public static void main(String[] args) {
		SpringApplication.run(RgdRestWebServicesApplication.class, args);
	}

}
