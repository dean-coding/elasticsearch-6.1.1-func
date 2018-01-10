package com.example.es.example;

import java.net.InetAddress;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class ESTransportClientFactoryBean implements FactoryBean<TransportClient>, InitializingBean, DisposableBean {

	private String clusterName;
	private String host;
	private int port;

	private TransportClient client;

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public TransportClient getObject() throws Exception {
		return client;
	}

	public Class<?> getObjectType() {
		return TransportClient.class;
	}

	public boolean isSingleton() {
		return false;
	}

	public void destroy() throws Exception {
		if (client != null)
			client.close();
	}

	@SuppressWarnings("resource")
	public void afterPropertiesSet() throws Exception {
		Settings settings = Settings.builder().put("cluster.name", this.clusterName).build();
		client = new PreBuiltTransportClient(settings)
				.addTransportAddress(new TransportAddress(InetAddress.getByName(this.host), this.port));
	}
}