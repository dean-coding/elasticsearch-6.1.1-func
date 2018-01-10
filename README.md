# springboot elasticsearch 集成

## 1.依赖：

	覆盖<elasticsearch.version>6.1.1</elasticsearch.version>版本属性
	
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.5.9.RELEASE</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>
	
	<dependency>
		<groupId>org.elasticsearch</groupId>
		<artifactId>elasticsearch</artifactId>
	</dependency>
	
	<dependency>
		<groupId>org.elasticsearch.client</groupId>
		<artifactId>transport</artifactId>
		<version>6.1.1</version>
	</dependency>


## 2.集成 ESTransportClientFactoryBean
	
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


## 3.配置
	
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

## 4.测试
	
	package com.example.es.example;
	
	import java.util.ArrayList;
	import java.util.Date;
	import java.util.List;
	import java.util.concurrent.ExecutionException;
	import java.util.concurrent.atomic.AtomicInteger;
	
	import org.apache.commons.lang3.RandomStringUtils;
	import org.elasticsearch.action.delete.DeleteResponse;
	import org.elasticsearch.action.index.IndexResponse;
	import org.elasticsearch.action.search.SearchResponse;
	import org.elasticsearch.action.search.SearchType;
	import org.elasticsearch.action.update.UpdateRequest;
	import org.elasticsearch.client.transport.TransportClient;
	import org.elasticsearch.common.xcontent.XContentType;
	import org.elasticsearch.search.SearchHit;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RequestParam;
	import org.springframework.web.bind.annotation.RestController;
	
	import com.fasterxml.jackson.core.JsonProcessingException;
	import com.fasterxml.jackson.databind.ObjectMapper;
	
	import lombok.AllArgsConstructor;
	import lombok.Data;
	import lombok.NoArgsConstructor;
	
	@RestController
	public class DemoCtrl {
	
		@Autowired
		private TransportClient client;
	
		private final static ObjectMapper mapper = new ObjectMapper();
		private AtomicInteger no = new AtomicInteger(1000);
	
		private final static String TEST_INDEX = "es-docs";
		private final static String TEST_TYPE = "cargo";
	
		@AllArgsConstructor
		@NoArgsConstructor
		@Data
		public static class User {
			private String no, name;
			private Date createdDate;
		}
	
		@RequestMapping("/add")
		public IndexResponse add() throws JsonProcessingException {
			String userStr = mapper
					.writeValueAsString(new User(String.valueOf(no), RandomStringUtils.randomAlphabetic(6), new Date()));
			return client.prepareIndex(TEST_INDEX, TEST_TYPE).setSource(userStr, XContentType.JSON).get();
		}
	
		@RequestMapping("/delete")
		public DeleteResponse delete(@RequestParam("id") String id) {
			return client.prepareDelete(TEST_INDEX, TEST_TYPE, id).get();
		}
	
		@RequestMapping("/update")
		public Object update(@RequestParam("id") String id, @RequestParam("id") String name)
				throws JsonProcessingException, InterruptedException, ExecutionException {
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.index(TEST_INDEX);
			updateRequest.type(TEST_TYPE);
			updateRequest.id(id);
			User user = new User();
			user.setName(name);
			String userStr = mapper.writeValueAsString(user);
			updateRequest.doc(userStr);
			return client.update(updateRequest).get();
		}
	
		@RequestMapping("/get")
		public Object get(@RequestParam("id") String id) {
			/**
			 * setOperationThreaded：operationThreaded被设置为true表示在不同的线程上执行操作。这是一个例子，它将其设置为
			 * false：
			 */
			return client.prepareGet(TEST_INDEX, TEST_TYPE, id).setOperationThreaded(false).get();
		}
	
		@RequestMapping("/query")
		public Object query(@RequestParam("max") Integer max,
				@RequestParam("min") Integer min) throws JsonProcessingException {
			 SearchResponse searchResponse = client.prepareSearch(TEST_INDEX).setTypes(TEST_TYPE).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	//				.setQuery(QueryBuilders.termQuery("multi", "test")) // Query
	//				.setPostFilter(QueryBuilders.rangeQuery("no").from(min).to(max)) // Filter
					.setFrom(0).setSize(20)
					.setExplain(true)
					.get();
			 SearchHit[] hits = searchResponse.getHits().getHits();
			 List<String> list = new ArrayList<>();
			 for (int i = 0; i < hits.length; i++) {
				 SearchHit searchHit = hits[i];
				 list.add(searchHit.getSourceAsString());
			}
			 return list;
		}
	}

## 5 结果：
DemoCtrl ... crud..