package com.example.api_gateway.config;
import com.example.api_gateway.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;
@Configuration
public class SecurityConfig {
    @Bean SecurityFilterChain security(HttpSecurity http, JwtAuthenticationFilter jwt) throws Exception {
        return http.csrf(c -> c.disable()).cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> res.sendError(401))
                    .accessDeniedHandler((req,res,ex) -> res.sendError(403)))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh-token", "/actuator/health", "/error").permitAll()
                // Quản trị người dùng / duyệt Seller
                .requestMatchers(HttpMethod.GET, "/api/auth/users", "/api/auth/seller-applications").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/auth/seller-applications/*/approve", "/api/auth/seller-applications/*/reject").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products/stats", "/api/inventory/stats", "/api/inventory", "/api/category/private",
                        "/api/inventory/reservation/**", "/api/orders", "/api/orders/stats", "/api/payments", "/api/payments/stats").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/shops/mine").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/category", "/api/inventory/*", "/api/shops", "/api/shops/*").permitAll()
                // ADMIN + SELLER: đăng sản phẩm, gian hàng, kho của mình (service kiểm tra quyền sở hữu)
                .requestMatchers(HttpMethod.POST, "/api/products", "/api/products/*/image", "/api/inventory/stock", "/api/shops").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.PUT, "/api/products/*", "/api/shops/*").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/*/image").hasAnyRole("ADMIN", "SELLER")
                .requestMatchers(HttpMethod.PATCH, "/api/inventory/stock/*").hasAnyRole("ADMIN", "SELLER")
                // Chỉ ADMIN
                .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/category", "/api/category/**", "/api/inventory/**",
                        "/api/payments/*/confirm", "/api/payments/*/refund").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/inventory/**", "/api/orders/*", "/api/payments/*").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean CorsConfigurationSource corsConfigurationSource() {
        var c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:5173"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*")); c.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**", c); return source;
    }
}
