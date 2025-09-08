package br.com.divulgaifback.common.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WhiteListConfig {

    @Bean
    public RequestMatcher whiteListedRoutes() {
        List<RequestMatcher> matchers = Arrays.asList(
            new AntPathRequestMatcher("/error", "GET"),
            new AntPathRequestMatcher("/auth/**", "POST"),
            new AntPathRequestMatcher("/users", "POST"),
            new AntPathRequestMatcher("/works/list", "GET"),
            new AntPathRequestMatcher("/labels/list", "GET"),
            new AntPathRequestMatcher("/links/list", "GET"),
            new AntPathRequestMatcher("/works/{id}", "GET"),
            new AntPathRequestMatcher("/swagger-ui/**", "GET"),
            new AntPathRequestMatcher("/v3/api-docs/**", "GET")
        );
        return new OrRequestMatcher(matchers);
    }
}