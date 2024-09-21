package com.vts.fxdata.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll() // Allow login and static resources
//                        .anyRequest().authenticated() // Require authentication for other requests
//                )
//                .formLogin(form -> form
//                        .loginPage("/login") // Custom login page
//                        .defaultSuccessUrl("/index.html", true)
//                        .permitAll()
//                )
//                .logout(logout -> logout.permitAll())
//                .sessionManagement(session -> session
//                        .invalidSessionUrl("/login?invalid-session=true")
//                );
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()  // All requests require authentication
                )
                .formLogin(form -> form
                        .permitAll()  // Allow access to the default Spring Security login page
                )
                .logout(logout -> logout
                        .permitAll()  // Allow access to the default logout page
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
