/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import gnu.trove.map.hash.THashMap;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.geometry.Area;
import com.ne.gs.model.templates.zone.ZoneClassName;
import com.ne.gs.model.templates.zone.ZoneInfo;
import com.ne.gs.model.templates.zone.ZoneTemplate;
import com.ne.gs.world.World;
import com.ne.gs.world.zone.handler.AdvencedZoneHandler;
import com.ne.gs.world.zone.handler.ZoneHandler;

/**
 * @author ATracer
 */
public class ZoneInstance implements Comparable<ZoneInstance> {

    private final ZoneInfo template;
    private final int mapId;
    private final Map<Integer, Creature> creatures = new THashMap<>();
    protected List<ZoneHandler> handlers = new ArrayList<>();

    public ZoneInstance(int mapId, ZoneInfo template) {
        this.template = template;
        this.mapId = mapId;
    }

    /**
     * @return the template
     */
    public Area getAreaTemplate() {
        return template.getArea();
    }

    /**
     * @return the template
     */
    public ZoneTemplate getZoneTemplate() {
        return template.getZoneTemplate();
    }

    public boolean revalidate(Creature creature) {
        return (mapId == creature.getWorldId() && template.getArea().isInside3D(creature.getX(), creature.getY(), creature.getZ()));
    }

    public synchronized boolean onEnter(Creature creature) {
        if (creatures.containsKey(creature.getObjectId())) {
            return false;
        }
        creatures.put(creature.getObjectId(), creature);
        if (creature instanceof Player) {
            creature.getController().onEnterZone(this);
        }
        for (ZoneHandler handler : handlers) {
            handler.onEnterZone(creature, this);
        }
        return true;
    }

    public synchronized boolean onLeave(Creature creature) {
        if (!creatures.containsKey(creature.getObjectId())) {
            return false;
        }
        creatures.remove(creature.getObjectId());
        creature.getController().onLeaveZone(this);
        for (ZoneHandler handler : handlers) {
            handler.onLeaveZone(creature, this);
        }
        return true;
    }

    public boolean onDie(Creature attacker, Creature target) {
        if (!creatures.containsKey(target.getObjectId())) {
            return false;
        }
        for (ZoneHandler handler : handlers) {
            if (handler instanceof AdvencedZoneHandler) {
                if (((AdvencedZoneHandler) handler).onDie(attacker, target, this)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInsideCreature(Creature creature) {
        return creatures.containsKey(creature.getObjectId());
    }

    public boolean isInsideCordinate(float x, float y, float z) {
        return template.getArea().isInside3D(x, y, z);
    }

    @Override
    public int compareTo(ZoneInstance o) {
        int result = getZoneTemplate().getPriority() - o.getZoneTemplate().getPriority();
        if (result == 0) {
            return template.getZoneTemplate().getName().id() - o.template.getZoneTemplate().getName().id();
        }
        return result;
    }

    public void addHandler(ZoneHandler handler) {
        handlers.add(handler);
    }

    public boolean canFly() {
        if (template.getZoneTemplate().getFlags() == -1) {
            return World.getInstance().getWorldMap(mapId).getTemplate().isFly();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.FLY.getId()) != 0;
    }

    public boolean canGlide() {
        if (template.getZoneTemplate().getFlags() == -1) {
            return World.getInstance().getWorldMap(mapId).getTemplate().canGlide();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.GLIDE.getId()) != 0;
    }

    public boolean canPutKisk() {
        if (template.getZoneTemplate().getFlags() == -1) {
            return World.getInstance().getWorldMap(mapId).getTemplate().canPutKisk();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.BIND.getId()) != 0;
    }

    public boolean canRecall() {
        if (template.getZoneTemplate().getFlags() == -1) {
            return World.getInstance().getWorldMap(mapId).getTemplate().canRecall();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.RECALL.getId()) != 0;
    }

    public boolean canRide() {
        if (template.getZoneTemplate().getFlags() == -1) {
            return World.getInstance().getWorldMap(mapId).getTemplate().canRide();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.RIDE.getId()) != 0;
    }

    public boolean canFlyRide() {
        if (template.getZoneTemplate().getFlags() == -1) {
            return World.getInstance().getWorldMap(mapId).getTemplate().canFlyRide();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.FLY_RIDE.getId()) != 0;
    }

    public boolean isPvpAllowed() {
        if (template.getZoneTemplate().getZoneType() != ZoneClassName.PVP) {
            return World.getInstance().getWorldMap(mapId).getTemplate().isPvpAllowed();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.PVP_ENABLED.getId()) != 0;
    }

    public boolean isSameRaceDuelsAllowed() {
        if (template.getZoneTemplate().getZoneType() != ZoneClassName.DUEL || template.getZoneTemplate().getFlags() == 0
	    || World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.DUEL_SAME_RACE_ENABLED)) {
            return World.getInstance().getWorldMap(mapId).isSameRaceDuelsAllowed();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.DUEL_SAME_RACE_ENABLED.getId()) != 0;
    }

    public boolean isOtherRaceDuelsAllowed() {
        if (template.getZoneTemplate().getZoneType() != ZoneClassName.DUEL || template.getZoneTemplate().getFlags() == 0
	    || World.getInstance().getWorldMap(mapId).hasOverridenOption(ZoneAttributes.DUEL_OTHER_RACE_ENABLED)) {
            return World.getInstance().getWorldMap(mapId).isOtherRaceDuelsAllowed();
        }
        return (template.getZoneTemplate().getFlags() & ZoneAttributes.DUEL_OTHER_RACE_ENABLED.getId()) != 0;
    }

    public Map<Integer, Creature> getCreatures() {
        return creatures;
    }
}
