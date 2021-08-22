/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.debug;

/**
 *
 * @author userd
 */
public class DebugInfo<T extends Enum> {

    protected T state;
    protected String message = null;

    public DebugInfo(T state) {
        this.state = state;
    }

    public DebugInfo(T state, String message) {
        this.state = state;
        this.message = message;
    }

    public T getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}
