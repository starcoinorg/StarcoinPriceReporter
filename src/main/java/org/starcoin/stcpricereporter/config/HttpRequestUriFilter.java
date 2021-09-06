package org.starcoin.stcpricereporter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class HttpRequestUriFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestUriFilter.class);

    private final static String[] URI_NOT_FILTER = new String[]{"/v1/priceFeeds", "/v1/exchangeRates",
            "/swagger", "/v3/api-docs", "/favicon.ico"};

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String uri = ((HttpServletRequest) request).getRequestURI();
        if (isLegalUri(uri)) {
            chain.doFilter(request, response);
        } else {
            LOG.info("Intercepted URI：{}", uri);
            doResponseFailure(response, uri);
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isLegalUri(String uri) {
        for (String str : URI_NOT_FILTER) {
            if (uri.indexOf(str) == 0) {
                return true;
            }
        }
        return false;
    }

    private void doResponseFailure(ServletResponse servletResponse, String uri) {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IOException exception) {
            LOG.error("Response sendError error. Request URI: " + uri, exception);
        }
    }
}
