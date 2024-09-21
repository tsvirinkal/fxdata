package com.niamedtech.expo.exposerversdk;

import com.fasterxml.jackson.annotation.*;
import com.niamedtech.expo.exposerversdk.enums.Status;
import com.niamedtech.expo.exposerversdk.enums.TicketError;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"_debug"})
public class ExpoPushError {

    private String code = null;
    private String message = null;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpoPushError that)) return false;
        return Objects.equals(code, that.code) &&
                Objects.equals(getMessage(), that.getMessage()) &&
                Objects.equals(getAdditionalProperties(), that.getAdditionalProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, getMessage(), getAdditionalProperties());
    }
}