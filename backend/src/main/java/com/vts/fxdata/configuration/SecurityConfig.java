package com.vts.fxdata.configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${fxdata.username}")
    private String username;

    @Value("${fxdata.password}")
    private String password;

    @Bean
    @Profile("prod")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.cacheControl(cache -> cache.disable()));
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v2/fxdata/trades/all",
                                                 "/api/v2/fxdata/results").authenticated()
                        .requestMatchers("/media/**","/api/v2/fxdata/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.permitAll())
                .logout(logout -> logout.permitAll())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
                .csrf(csrf -> csrf.disable()); 
        return http.build();
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurity(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests()
                .anyRequest().permitAll();
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("http://localhost:4200");  
        configuration.addAllowedOriginPattern("https://fxdata.ddns.net"); 
        configuration.addAllowedMethod("*"); 
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true); 

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var admin = User.withUsername(username)
                .password(passwordEncoder().encode(password))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
