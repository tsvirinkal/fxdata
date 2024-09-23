package com.vts.fxdata.configuration;

import com.vts.fxdata.filters.IpWhitelistFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<IpWhitelistFilter> ipWhitelistFilter() {
        FilterRegistrationBean<IpWhitelistFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new IpWhitelistFilter());

        // Apply the filter to the desired URL patterns (e.g., "/api/*")
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }
}
