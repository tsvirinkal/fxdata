package com.vts.fxdata.configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${fxdata.username}")
    private String username;

    @Value("${fxdata.password}")
    private String password;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.cacheControl(cache -> cache.disable()));
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/media/**","/api/**").permitAll()
                        .anyRequest().authenticated()
                )
//                .requiresChannel(channel -> channel
//                        .requestMatchers("/login").requiresSecure()
//                        .requestMatchers("/api/v2/fdata/states").requiresSecure()
//                        .requestMatchers("/api/v2/fdata/trades/all").requiresSecure()
//                        .requestMatchers("/api/v2/fdata/results").requiresSecure()
//                )
                .formLogin(form -> form.permitAll())
                .logout(logout -> logout.permitAll());

        http.csrf().disable();
        return http.build();
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
