package org.starcoin.stcpricereporter.config;


//import org.apache.http.impl.client.CloseableHttpClient;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestTemplateConfig {

    //private final CloseableHttpClient httpClient;

    @Value("${ok.http.connect-timeout}")
    private Integer connectTimeout;

    @Value("${ok.http.read-timeout}")
    private Integer readTimeout;

    @Value("${ok.http.write-timeout}")
    private Integer writeTimeout;

    @Value("${ok.http.max-idle-connections}")
    private Integer maxIdleConnections;

    @Value("${ok.http.keep-alive-duration}")
    private Long keepAliveDuration;

//  @Autowired
//  public RestTemplateConfig(CloseableHttpClient httpClient) {
//    this.httpClient = httpClient;
//  }

//  @Bean
//  public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
//    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
//    clientHttpRequestFactory.setHttpClient(httpClient);
//    return clientHttpRequestFactory;
//  }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .requestFactory(this::clientHttpRequestFactory)
                .errorHandler(new CustomClientErrorHandler())
                .interceptors(new CustomClientHttpRequestInterceptor())
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        return new OkHttp3ClientHttpRequestFactory(okHttpConfigClient());
    }

    private OkHttpClient okHttpConfigClient() {
        return new OkHttpClient().newBuilder()
                .connectionPool(pool())
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                // 设置代理
//              .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888)))
                // 拦截器
//                .addInterceptor()
                .build();
    }

    private ConnectionPool pool() {
        return new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS);
    }

}