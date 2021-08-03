package org.starcoin.stcpricereporter.config;


import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  private final CloseableHttpClient httpClient;

//  @Value("${ok.http.connect-timeout}")
//  private Integer connectTimeout;
//
//  @Value("${ok.http.read-timeout}")
//  private Integer readTimeout;
//
//  @Value("${ok.http.write-timeout}")
//  private Integer writeTimeout;
//
//  @Value("${ok.http.max-idle-connections}")
//  private Integer maxIdleConnections;
//
//  @Value("${ok.http.keep-alive-duration}")
//  private Long keepAliveDuration;

  @Autowired
  public RestTemplateConfig(CloseableHttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Bean
  public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setHttpClient(httpClient);
    return clientHttpRequestFactory;
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
            .requestFactory(this::clientHttpRequestFactory)
            //.requestFactory(() -> new OkHttp3ClientHttpRequestFactory())
            .errorHandler(new CustomClientErrorHandler())
            .interceptors(new CustomClientHttpRequestInterceptor())
            .build();
  }

//  private ClientHttpRequestFactory httpRequestFactory() {
//    return new OkHttp3ClientHttpRequestFactory(okHttpConfigClient());
//  }
//
//  private OkHttpClient okHttpConfigClient(){
//    return new OkHttpClient().newBuilder()
//            .connectionPool(pool())
//            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
//            .readTimeout(readTimeout, TimeUnit.SECONDS)
//            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
//            .hostnameVerifier((hostname, session) -> true)
//            // 设置代理
////              .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)))
//            // 拦截器
////                .addInterceptor()
//            .build();
//  }
//
//  private ConnectionPool pool() {
//    return new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
//  }

}