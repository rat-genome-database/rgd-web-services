package edu.mcw.rgd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Database access goes through rgdcore's DataSourceFactory (the Tomcat JNDI datasources, e.g. jdbc/rgd2),
 * so Spring Boot must not build its own pool from spring.datasource.* settings in a deployed
 * application.properties: an unusable one (such as an Oracle driver that is no longer bundled) stops the
 * whole application from starting.
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class RgdRestWebServicesApplication {
	public static void main(String[] args) {
		SpringApplication.run(RgdRestWebServicesApplication.class, args);
	}

}
