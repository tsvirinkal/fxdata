package com.vts.fxdata.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonObjectMapper extends ObjectMapper {

    private static final ObjectMapper objectMapper = new JsonObjectMapper();

    public JsonObjectMapper() {
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public static ObjectMapper getMapper() {
        return objectMapper;
    }
}