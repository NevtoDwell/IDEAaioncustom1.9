/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2;

import java.util.Collection;
import com.google.common.base.Predicate;

import com.ne.gs.model.Race;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public interface Team<M, TM extends TeamMember<M>> {

    Integer getTeamId();

    TM getMember(Integer objectId);

    boolean hasMember(Integer objectId);

    void addMember(TM member);

    void removeMember(TM member);

    void removeMember(Integer objectId);

    Collection<M> getMembers();

    Collection<M> getOnlineMembers();

    void onEvent(TeamEvent event);

    Collection<TM> filter(Predicate<TM> predicate);

    Collection<M> filterMembers(Predicate<M> predicate);

    void sendPacket(AionServerPacket packet);

    void sendPacket(AionServerPacket packet, Predicate<M> predicate);

    int onlineMembers();

    Race getRace();

    int size();

    boolean isFull();

}
