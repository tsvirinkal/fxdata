package com.niamedtech.expo.exposerversdk.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Priority {
        @JsonProperty("default")
        OK("default"),
        @JsonProperty("high")
        ERROR("high"),
        @JsonProperty("normal")
        NORMAL("normal");

        private final String priority;
        Priority(String priority) {
                this.priority = priority;
        }

        @Override
        public String toString(){
                return priority;
        }
}
