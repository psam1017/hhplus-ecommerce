package hhplus.ecommerce.server.interfaces.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.trace("XssFilter.doFilter");
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }
}
