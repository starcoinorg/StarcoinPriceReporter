package org.starcoin.stcpricereporter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class CustomClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Logger LOG = LoggerFactory.getLogger(CustomClientHttpRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] bytes, ClientHttpRequestExecution execution) throws IOException {
        // log the http request
        LOG.debug("URI: {}", request.getURI());
        LOG.debug("HTTP Method: {}", request.getMethodValue());
        LOG.debug("HTTP Headers: {}", request.getHeaders());

        return execution.execute(request, bytes);
    }
}
