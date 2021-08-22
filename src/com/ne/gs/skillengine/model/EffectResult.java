package com.ne.gs.skillengine.model;

public enum EffectResult {

    NORMAL(0),
    ABSORBED(1),
    CONFLICT(2);
    private int id;

    private EffectResult(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
