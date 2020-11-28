package com.example.es.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan(basePackages = { "com.example.es" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class) })
public class ESConfig {

	// bean的id为transportClient
	@Bean
	public ESTransportClientFactoryBean transportClient() {
		ESTransportClientFactoryBean transportClientFactory = new ESTransportClientFactoryBean();
		transportClientFactory.setClusterName("xiaolang");
		transportClientFactory.setHost("192.168.1.108");
		transportClientFactory.setPort(9300);
		return transportClientFactory;
	}

}