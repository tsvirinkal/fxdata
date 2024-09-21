package com.niamedtech.expo.exposerversdk.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Status {
        @JsonProperty("ok")
        OK("ok"),
        @JsonProperty("error")
        ERROR("error");

        private final String status;
        Status(String status) {
                this.status = status;
        }

        @Override
        public String toString(){
                return status;
        }
}
