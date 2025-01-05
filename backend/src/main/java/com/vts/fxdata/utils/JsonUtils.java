package com.vts.fxdata.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils<T>
{
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private final Class<T> type;

    public JsonUtils(Class<T> type) {
        this.type = type;
    }

    public T deserialize(String json) throws Exception {
        log.info(json);
        return new ObjectMapper().readValue(json, type);
    }
}

