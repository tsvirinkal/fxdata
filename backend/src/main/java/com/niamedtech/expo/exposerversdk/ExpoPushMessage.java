package com.niamedtech.expo.exposerversdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"_debug"})
public class ExpoPushMessage extends ExpoPushMessageCustomData<Object> {

    public ExpoPushMessage() {
        super();
    }

    public ExpoPushMessage(ExpoPushMessage _message) {
        super(_message);
    }

    public ExpoPushMessage(List<String> _to, ExpoPushMessageCustomData<Object> _message) {
        super(_to, _message);
    }

    public ExpoPushMessage(List<String> _to) {
        super(_to);
    }

    public ExpoPushMessage(String _to) {
        super(_to);
    }

    @Override
    public Object clone() {
        return new ExpoPushMessage(this);
    }
}

