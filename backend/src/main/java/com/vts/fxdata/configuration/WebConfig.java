package com.vts.fxdata.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@ComponentScan({ "com.vts.fxdata" })
public class WebConfig implements WebMvcConfigurer {
    public static final String DATE_TIME_PATTERN = "HH:mm dd.MM.yyyy";
    public static final String DATE_PATTERN = "dd.MM.yyyy";

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateSerializer localDateSerializer = new LocalDateSerializer(formatter);
        LocalDateDeserializer localDateDeserializer = new LocalDateDeserializer(formatter);

        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDate.class, localDateSerializer);
        module.addDeserializer(LocalDate.class, localDateDeserializer);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        // add converter at the very front
        // if there are same type mappers in converters, setting in first mapper is used.
        converters.add(0, new MappingJackson2HttpMessageConverter(mapper));
    }
}