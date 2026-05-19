package com.bhavesh.ragbackend;

import com.bhavesh.ragbackend.config.LuceneProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(LuceneProperties.class)
public class RagBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagBackendApplication.class, args);
	}

}
