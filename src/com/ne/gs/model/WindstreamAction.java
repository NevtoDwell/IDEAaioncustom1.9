package com.ne.gs.model;

import com.ne.commons.annotations.Nullable;

/**
 * @author ViAl
 */
public enum WindstreamAction {

    ENTER(0),
    START(1),
    NORMAL_END(2),
    INTERRUPT(3),
    INTERRUPT_RELATED(4),
    BOOST_START(7),
    BOOST_END(8);

    private final int id;

    private WindstreamAction(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    @Nullable
    public static WindstreamAction getById(int actionId) {
        for (WindstreamAction action : values()) {
            if (action.getId() == actionId) {
                return action;
            }
        }
        return null;
    }
}
