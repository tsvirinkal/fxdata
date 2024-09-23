package com.vts.fxdata.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class IpWhitelistFilter implements Filter {

    @Value("${whitelist.ip}")
    private String[] whitelist;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);

        // Check if the client IP is in the whitelist
        if (Arrays.asList(whitelist).contains(clientIp)) {
            chain.doFilter(request, response);  // Continue request if IP is whitelisted
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);  // Block request if IP is not whitelisted
            httpResponse.getWriter().write("Forbidden: IP not allowed");
        }
    }

    // Helper method to get the client IP address
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0];
        }
        return ip.trim();
    }
}
