/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.container;

import javolution.util.FastMap;

import com.ne.gs.model.team.legion.LegionMember;
import com.ne.gs.model.team.legion.LegionMemberEx;
import com.ne.gs.world.exceptions.DuplicateAionObjectException;

/**
 * Container for storing Legion members by Id and name.
 *
 * @author Simple
 */
public class LegionMemberContainer {

    private final FastMap<Integer, LegionMember> legionMemberById = new FastMap<Integer, LegionMember>().shared();

    private final FastMap<Integer, LegionMemberEx> legionMemberExById = new FastMap<Integer, LegionMemberEx>().shared();
    private final FastMap<String, LegionMemberEx> legionMemberExByName = new FastMap<String, LegionMemberEx>().shared();

    /**
     * Add LegionMember to this Container.
     *
     * @param legionMember
     */
    public void addMember(LegionMember legionMember) {
        if (!legionMemberById.containsKey(legionMember.getObjectId())) {
            legionMemberById.put(legionMember.getObjectId(), legionMember);
        }
    }

    /**
     * This method will return a member from cache
     *
     * @param memberObjId
     */
    public LegionMember getMember(int memberObjId) {
        return legionMemberById.get(memberObjId);
    }

    /**
     * Add LegionMemberEx to this Container.
     *
     * @param legionMember
     */
    public void addMemberEx(LegionMemberEx legionMember) {
        if (legionMemberExById.containsKey(legionMember.getObjectId()) || legionMemberExByName.containsKey(legionMember.getName())) {
            throw new DuplicateAionObjectException();
        }
        legionMemberExById.put(legionMember.getObjectId(), legionMember);
        legionMemberExByName.put(legionMember.getName(), legionMember);
    }

    /**
     * This method will return a memberEx from cache
     *
     * @param memberObjId
     */
    public LegionMemberEx getMemberEx(int memberObjId) {
        return legionMemberExById.get(memberObjId);
    }

    /**
     * This method will return a memberEx from cache
     *
     * @param memberName
     */
    public LegionMemberEx getMemberEx(String memberName) {
        return legionMemberExByName.get(memberName);
    }

    /**
     * Remove LegionMember from this Container.
     *
     * @param legionMember
     */
    public void remove(LegionMemberEx legionMember) {
        legionMemberById.remove(legionMember.getObjectId());
        legionMemberExById.remove(legionMember.getObjectId());
        legionMemberExByName.remove(legionMember.getName());
    }

    /**
     * Returns true if legion is in cached by id
     *
     * @param memberObjId
     *
     * @return true or false
     */
    public boolean contains(int memberObjId) {
        return legionMemberById.containsKey(memberObjId);
    }

    /**
     * Returns true if legion is in cached by id
     *
     * @param memberObjId
     *
     * @return true or false
     */
    public boolean containsEx(int memberObjId) {
        return legionMemberExById.containsKey(memberObjId);
    }

    /**
     * Returns true if legion is in cached by id
     *
     * @param memberName
     *
     * @return true or false
     */
    public boolean containsEx(String memberName) {
        return legionMemberExByName.containsKey(memberName);
    }

    public void clear() {
        legionMemberById.clear();
        legionMemberExById.clear();
        legionMemberExByName.clear();
    }
}
