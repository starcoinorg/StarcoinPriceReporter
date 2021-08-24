package org.starcoin.stcpricereporter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class HttpMessageConverterConfig implements WebMvcConfigurer {

    // 使用 JSON 序列化 Spring MVC RESTful API 返回的字符串（带上双引号）：
    // * 方法返回类型前需要加上 @ResponseBody 注解。
    // * 在 Message Converters 配置中移除 StringHttpMessageConverter。
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(c -> c.getClass().equals(StringHttpMessageConverter.class));
    }

}
