package com.vts.fxdata.models.dto;

import com.vts.fxdata.models.ActionEnum;

public class Action {
    private ActionEnum action;
    private int target;

    public Action(ActionEnum action, int target) {
        this.action = action;
        this.target = target;
    }

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
