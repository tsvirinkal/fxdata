package com.vts.fxdata.models.dto;

import com.vts.fxdata.models.ActionEnum;

public class Action {
    private ActionEnum action;
    private int progress;
    private int target;

    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
