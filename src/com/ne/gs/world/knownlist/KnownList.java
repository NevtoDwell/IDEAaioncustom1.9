/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.knownlist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.MapRegion;

/**
 * KnownList.
 *
 * @author -Nemesiss-
 * @modified kosyachok
 */

public class KnownList {

    private static final Logger log = LoggerFactory.getLogger(KnownList.class);

    /**
     * Owner of this KnownList.
     */
    protected final VisibleObject owner;
    /**
     * List of objects that this KnownList owner known
     */
    protected final ConcurrentHashMap<Integer, VisibleObject> knownObjects = new ConcurrentHashMap<>(5, 0.75f, 4);

    /**
     * @param owner
     */
    public KnownList(VisibleObject owner) {
        this.owner = owner;
    }

    /**
     * Do KnownList update.
     */
    public void doUpdate() {
        synchronized (this) {
            forgetObjects();
            findVisibleObjects();
        }
    }

    /**
     * Clear known list. Used when object is despawned.
     */
    public void clear() {
        for (VisibleObject object : knownObjects.values()) {
            object.getKnownList().del(owner, false);
        }
        knownObjects.clear();
    }

    /**
     * Check if object is known
     *
     * @param object
     *
     * @return true if object is known
     */
    public boolean knowns(AionObject object) {
        return knownObjects.containsKey(object.getObjectId());
    }

    /**
     * Add VisibleObject to this KnownList.
     *
     * @param object
     */
    protected boolean add(VisibleObject object) {
        if (!isAwareOf(object)) {
            return false;
        }
        if (knownObjects.put(object.getObjectId(), object) == null) {
            addVisualObject(object);
            return true;
        }
        return false;
    }

    public void addVisualObject(VisibleObject object) {
        if (object instanceof Creature) {
            if (SecurityConfig.INVIS && !owner.canSee((Creature) object)) {
                return;
            }
        }

        owner.getController().see(object);
    }

    /**
     * Delete VisibleObject from this KnownList.
     *
     * @param object
     */
    private void del(VisibleObject object, boolean isOutOfRange) {
        /**
         * object was known.
         */
        if (knownObjects.remove(object.getObjectId()) != null) {
            delVisualObject(object, isOutOfRange);
        }
    }

    public void delVisualObject(VisibleObject object, boolean isOutOfRange) {
        owner.getController().notSee(object, isOutOfRange);
    }

    /**
     * forget out of distance objects.
     */
    private void forgetObjects() {
        for (VisibleObject object : knownObjects.values()) {
            if (!checkObjectInRange(object) && !object.getKnownList().checkReversedObjectInRange(owner)) {
                del(object, true);
                object.getKnownList().del(owner, true);
            }
        }
    }

    /**
     * Find objects that are in visibility range.
     */
    protected void findVisibleObjects() {
        if (owner == null || !owner.isSpawned()) {
            return;
        }

        MapRegion[] regions = owner.getActiveRegion().getNeighbours();
        for (MapRegion r : regions) {
            FastMap<Integer, VisibleObject> objects = r.getObjects();
            for (FastMap.Entry<Integer, VisibleObject> e = objects.head(), mapEnd = objects.tail(); (e = e
                .getNext()) != mapEnd; ) {
                VisibleObject newObject = e.getValue();
                if (newObject == owner || newObject == null) {
                    continue;
                }

                if (!isAwareOf(newObject)) {
                    continue;
                }
                if (knownObjects.containsKey(newObject.getObjectId())) {
                    continue;
                }

                if (!checkObjectInRange(newObject) && !newObject.getKnownList().checkReversedObjectInRange(owner)) {
                    continue;
                }

                /**
                 * New object is not known.
                 */
                if (add(newObject)) {
                    newObject.getKnownList().add(owner);
                }
            }
        }
    }

    /**
     * Whether knownlist owner aware of found object (should be kept in knownlist)
     *
     * @param newObject
     *
     * @return
     */
    protected boolean isAwareOf(VisibleObject newObject) {
        return true;
    }

    protected boolean checkObjectInRange(VisibleObject newObject) {
        // check if Z distance is greater than maxZvisibleDistance
        if (Math.abs(owner.getZ() - newObject.getZ()) > owner.getMaxZVisibleDistance()) {
            return false;
        }

        return MathUtil.isInRange(owner, newObject, owner.getVisibilityDistance());
    }

    /**
     * Check can be overriden if new object has different known range and that value should be used
     *
     * @param newObject
     *
     * @return
     */
    protected boolean checkReversedObjectInRange(VisibleObject newObject) {
        return false;
    }

    public void doOnAllNpcs(Visitor<Npc> visitor) {
        doOnAllNpcs(visitor, 2147483647);
    }

    public int doOnAllNpcs(Visitor<Npc> visitor, int iterationLimit) {
        int counter = 0;
        try {
            for (VisibleObject newObject : knownObjects.values()) {
                if (newObject instanceof Npc) {
                    counter++;
                    if (counter == iterationLimit) {
                        break;
                    }
                    visitor.visit((Npc) newObject);
                }
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
        return counter;
    }

    public int doOnAllNpcsWithOwner(VisitorWithOwner<Npc, VisibleObject> visitor, int iterationLimit) {
        int counter = 0;
        try {
            for (VisibleObject newObject : knownObjects.values()) {
                if (newObject instanceof Npc) {
                    counter++;
                    if (counter == iterationLimit) {
                        break;
                    }
                    visitor.visit((Npc) newObject, owner);
                }
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
        return counter;
    }

    public void doOnAllPlayers(Visitor<Player> visitor) {
        try {
            for (Player player : getKnownPlayers().values()) {
                if (player != null) {
                    visitor.visit(player);
                }
            }
        } catch (Exception ex) {
            log.error("Exception when running visitor on all players", ex);
        }
    }

    public void doOnAllObjects(Visitor<VisibleObject> visitor) {
        try {
            for (VisibleObject newObject : knownObjects.values()) {
                if (newObject != null) {
                    visitor.visit(newObject);
                }
            }
        } catch (Exception ex) {
            log.error("Exception when running visitor on all objects", ex);
        }
    }

    public Map<Integer, VisibleObject> getKnownObjects() {
        return knownObjects;
    }

    public Map<Integer, Player> getKnownPlayers() {
        return Maps.transformValues(Maps.filterEntries(knownObjects, PLAYER_FILTER), PLAYER_TRANSFORMER);
    }

    public VisibleObject getObject(int targetObjectId) {
        return knownObjects.get(targetObjectId);
    }

    private static class PlayerFilter implements Predicate<Map.Entry<Integer, VisibleObject>> {
        @Override
        public boolean apply(Map.Entry<Integer, VisibleObject> input) {
            return input.getValue() instanceof Player;
        }
    }

    private static class PlayerTransformer implements Function<VisibleObject, Player> {
        @Override
        public Player apply(VisibleObject input) {
            return (Player) input;
        }
    }

    private static final Predicate<Map.Entry<Integer, VisibleObject>> PLAYER_FILTER = new PlayerFilter();
    private static final Function<VisibleObject, Player> PLAYER_TRANSFORMER = new PlayerTransformer();
}
