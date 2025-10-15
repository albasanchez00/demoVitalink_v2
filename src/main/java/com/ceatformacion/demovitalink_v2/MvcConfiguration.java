package com.ceatformacion.demovitalink_v2;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer {
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/inicioSesion").setViewName("inicioSesion");
    }
    @Override public void addCorsMappings(CorsRegistry reg) {
        reg.addMapping("/api/**")
                .allowedOrigins("http://localhost:5500","http://127.0.0.1:5500")
                .allowedMethods("GET");
    }
    // ⬇️ NUEVO: formateo global para fechas
    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();

        // Para <input type="date"> con LocalDate
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // (Opcional) Para <input type="datetime-local"> con LocalDateTime
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        // Si prefieres ISO por defecto:
        // registrar.setUseIsoFormat(true);

        registrar.registerFormatters(registry);
    }
}