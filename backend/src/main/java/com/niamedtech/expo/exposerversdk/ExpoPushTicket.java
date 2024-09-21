package com.niamedtech.expo.exposerversdk;

import com.fasterxml.jackson.annotation.*;
import com.niamedtech.expo.exposerversdk.enums.Status;
import com.niamedtech.expo.exposerversdk.enums.TicketError;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"_debug"})
public class ExpoPushTicket  {

    private String id = null;
    private Status status = null;
    private String message = null;
    private ExpoPushTicket.Details details = null;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExpoPushTicket.Details getDetails() {
        return details;
    }

    public void setDetails(ExpoPushTicket.Details details) {
        this.details = details;
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
        if (!(o instanceof ExpoPushTicket that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(getStatus(), that.getStatus()) &&
                Objects.equals(getMessage(), that.getMessage()) &&
                Objects.equals(getDetails(), that.getDetails()) &&
                Objects.equals(getAdditionalProperties(), that.getAdditionalProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getStatus(), getMessage(), getDetails(), getAdditionalProperties());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties({"apns", "fcm"})
    public static class Details {
        private TicketError error;
        private Integer sentAt;
        @JsonIgnore
        private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

        public TicketError getError() {
            return error;
        }

        public ExpoPushTicket.Details setError(TicketError error) {
            this.error = error;
            return this;
        }

        public Integer getSentAt() {
            return sentAt;
        }

        public Details setSentAt(Integer sentAt) {
            this.sentAt = sentAt;
            return this;
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
            if (!(o instanceof Details details)) return false;
            return Objects.equals(getError(), details.getError()) &&
                    Objects.equals(getSentAt(), details.getSentAt()) &&
                    Objects.equals(getAdditionalProperties(), details.getAdditionalProperties());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getError(), getSentAt(), getAdditionalProperties());
        }
    }
}