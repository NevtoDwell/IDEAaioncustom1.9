/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.instance.handlers;

import com.ne.gs.eventNewEngine.events.enums.EventType;

/**
 *
 * @author userd
 */
public class GeneralEventHandler extends GeneralInstanceHandler {

    protected EventType eType = EventType.E_DEFAULT;

    public final EventType getEventType() {
        return eType;
    }

    public final void setEventType(EventType et) {
        if (eType == EventType.E_DEFAULT && et != EventType.E_DEFAULT) {
            eType = et;
        }
    }
}
