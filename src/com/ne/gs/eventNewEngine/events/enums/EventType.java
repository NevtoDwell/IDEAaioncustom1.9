/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.enums;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.eventNewEngine.events.xml.EventTemplate;


/**
 *
 * @author userd
 */
public enum EventType {

    E_DEFAULT(false),
    E_1x1(true),
    E_2x2(true),
    E_3x3(true),
    E_4x4(true),
    E_6x6(true),
    E_LHE(true),
    E_TVT(false);
    //-----------------------------//
    private final boolean isDone;

    private EventType(boolean isDone) {
        this.isDone = isDone;
    }

    public boolean IsDone() {
        return isDone;
    }

    public EventTemplate getEventTemplate() {
        return DataManager.F14EVENTS_DATA.getEventTemplate(this);
    }
}
