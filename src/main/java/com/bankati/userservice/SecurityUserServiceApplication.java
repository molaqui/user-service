package com.bankati.userservice;

import com.bankati.userservice.config.RsakeysConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties(RsakeysConfig.class)
public class SecurityUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityUserServiceApplication.class, args);
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("admin"));
		System.out.println(encoder.encode("agent"));
		System.out.println(encoder.encode("client"));
	}

	@Bean
	public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
	}

}
