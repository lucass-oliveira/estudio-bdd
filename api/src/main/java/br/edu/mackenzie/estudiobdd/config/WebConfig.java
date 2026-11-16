package br.edu.mackenzie.estudiobdd.config;

import java.net.URI;

import br.edu.mackenzie.estudiobdd.service.UsuarioService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String origemSpa;

    public WebConfig(@Value("${app.spa-origin}") String origemSpa) {
        URI origem = URI.create(origemSpa);
        if (!("http".equals(origem.getScheme()) || "https".equals(origem.getScheme()))
                || origem.getHost() == null || !origem.getRawPath().isEmpty() || origem.getRawUserInfo() != null
                || origem.getRawQuery() != null || origem.getRawFragment() != null) {
            throw new IllegalArgumentException("SPA_ORIGIN deve conter uma única origem HTTP(S), sem caminho ou barra final.");
        }
        this.origemSpa = origemSpa;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origemSpa)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders(HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, UsuarioService.CABECALHO)
                .exposedHeaders(HttpHeaders.CONTENT_DISPOSITION);
    }
}
