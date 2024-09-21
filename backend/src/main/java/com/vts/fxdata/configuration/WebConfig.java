package com.vts.fxdata.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

//@EnableWebMvc
@Configuration
@ComponentScan({ "com.vts.fxdata" })
public class WebConfig implements WebMvcConfigurer {

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
//    @Bean
//    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
//        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().serializers(DateTimeFormatter.LOCAL_DATETIME_SERIALIZER)
//                .serializationInclusion(JsonInclude.Include.NON_NULL);
//        return new MappingJackson2HttpMessageConverter(builder.build());
//    }

}