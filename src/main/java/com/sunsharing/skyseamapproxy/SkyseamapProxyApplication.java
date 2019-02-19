package com.sunsharing.skyseamapproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class SkyseamapProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkyseamapProxyApplication.class, args);
	}
}
