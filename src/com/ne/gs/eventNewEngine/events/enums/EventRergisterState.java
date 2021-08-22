/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.enums;

/**
 *
 * @author userd
 */
public enum EventRergisterState {

    ERROR,
    CRITICAL_ERROR,
    GROUP_NOT_REGISTRED,
    REGISTRED,
    UNREGISTRED,
    ALREADY_REGISTRED,
    ONE_PLAYER_IN_GROUP_ALREADY_REGISTRED,
    EVENT_NOT_START,
    PLAYER_HAS_VISIT_EVENT,
    HOLDER_ADD_PLAYER,
    HOLDER_ADD_GROUP,
    HOLDER_ALREADY_IN_OR_IS_FULL,
    PLAYERS_IN_GROUP_MISSMATCH,
    HAVE_MAX_PLAYERS,
    HAVE_MAX_GROUP,
    HAVE_MAX_ASMOS,
    HAVE_MAX_ELYS,
    PLAYER_IS_NOT_GROUP_LEADER,
    PLAYER_IN_GROUP_ALREADY_VISIT_EVENT;
}
