package com.am9.okazx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class OkazxApplication {
	public static void main(String[] args) {
		SpringApplication.run(OkazxApplication.class, args);
	}
}
