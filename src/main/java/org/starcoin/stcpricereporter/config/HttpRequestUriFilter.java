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

    private final static String[] ALLOWED_PATHS = new String[]{"/v1/priceFeeds", "/v1/exchangeRates", "/v1/priceGrowths",
            "/v1/toUsdPriceFeeds", "/v1/get", "/swagger", "/v3/api-docs", "/favicon.ico"};

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String uri = ((HttpServletRequest) request).getRequestURI();
        String contextPath = ((HttpServletRequest) request).getContextPath();
        if (isLegalUri(contextPath, uri)) {
            chain.doFilter(request, response);
        } else {
            LOG.info("Intercepted URI：{}", uri);
            doResponseFailure(response, uri);
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isLegalUri(String contextPath, String uri) {
        for (String allowedPath : ALLOWED_PATHS) {
            if (uri.indexOf(contextPath + allowedPath) == 0) {
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
