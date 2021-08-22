/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import java.util.ArrayList;
import java.util.List;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.siegelocation.SiegeLocationTemplate;
import com.ne.gs.world.knownlist.Visitor;
import com.ne.gs.world.zone.SiegeZoneInstance;
import com.ne.gs.world.zone.ZoneInstance;
import com.ne.gs.world.zone.handler.ZoneHandler;

/**
 * @author Sarynth, Source, Wakizashi
 */
public class SiegeLocation implements ZoneHandler {

    private static final Logger log = LoggerFactory.getLogger(SiegeLocation.class);

    public static final int STATE_INVULNERABLE = 0;
    public static final int STATE_VULNERABLE = 1;
    /**
     * Unique id, defined by NCSoft
     */
    protected SiegeLocationTemplate template;
    protected int locationId;
    protected SiegeType type;
    protected int worldId;
    protected SiegeRace siegeRace = SiegeRace.BALAUR;
    protected int legionId;
    protected long lastArtifactActivation;
    private boolean vulnerable;
    private int nextState;
    protected List<SiegeZoneInstance> zone;
    private List<SiegeShield> shields;
    private boolean isUnderShield;
    private boolean canTeleport;
    protected int siegeDuration;
    protected int influenceValue;
    private final FastMap<Integer, Creature> creatures = new FastMap<>();
    private final FastMap<Integer, Player> players = new FastMap<>();

    public SiegeLocation() {
    }

    public SiegeLocation(SiegeLocationTemplate template) {
        this.template = template;
        locationId = template.getId();
        worldId = template.getWorldId();
        type = template.getType();
        siegeDuration = template.getSiegeDuration();
        zone = new ArrayList<>();
        influenceValue = template.getInfluenceValue();
    }

    public SiegeLocationTemplate getTemplate() {
        return template;
    }

    /**
     * Returns unique LocationId of Siege Location
     *
     * @return Integer LocationId
     */
    public int getLocationId() {
        return locationId;
    }

    public int getWorldId() {
        return worldId;
    }

    public SiegeType getType() {
        return type;
    }

    public int getSiegeDuration() {
        return siegeDuration;
    }

    public SiegeRace getRace() {
        return siegeRace;
    }

    public void setRace(SiegeRace siegeRace) {
        this.siegeRace = siegeRace;
    }

    public int getLegionId() {
        return legionId;
    }

    public void setLegionId(int legionId) {
        this.legionId = legionId;
    }

    public void setShields(List<SiegeShield> shields) {
        this.shields = shields;
        log.debug("Attached shields for locId: " + locationId);
        for (SiegeShield shield : shields)
            log.debug(shield.toString());
    }

    /**
     * Next State: 0 invulnerable 1 vulnerable
     *
     * @return nextState
     */
    public int getNextState() {
        return nextState;
    }

    public void setNextState(int nextState) {
        this.nextState = nextState;
    }

    /**
     * @return isVulnerable
     */
    public boolean isVulnerable() {
        return vulnerable;
    }

    /**
     * @return isUnderShield
     */
    public boolean isUnderShield() {
        return isUnderShield;
    }

    /**
     * @param value
     *     new undershield value
     */
    public void setUnderShield(boolean value) {

        this.isUnderShield = value;
        if (shields != null) {
            for (SiegeShield shield : shields)
                shield.setEnabled(value);
        }
    }

    /**
     * @return the canTeleport
     */
    public boolean isCanTeleport(Player player) {
        return canTeleport;
    }

    /**
     * @param canTeleport
     *     the canTeleport to set
     */
    public void setCanTeleport(boolean canTeleport) {
        this.canTeleport = canTeleport;
    }

    /**
     * @param value
     *     new vulnerable value
     */
    public void setVulnerable(boolean value) {
        vulnerable = value;
    }

    public int getInfluenceValue() {
        return influenceValue;
    }

    /**
     * @return the zone
     */
    public List<SiegeZoneInstance> getZone() {
        return zone;
    }

    /**
     * @param zone
     *     the zone to set
     */
    public void addZone(SiegeZoneInstance zone) {
        this.zone.add(zone);
        zone.addHandler(this);
    }

    public boolean isInsideLocation(Creature creature) {
        if (zone.isEmpty()) {
            return false;
        }
        for (SiegeZoneInstance aZone : zone) {
            if (aZone.isInsideCreature(creature)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInActiveSiegeZone(Player player) {
        return isVulnerable() && isInsideLocation(player);
    }

    public void clearLocation() {
    }

    @Override
    public void onEnterZone(Creature creature, ZoneInstance zone) {
        if (!creatures.containsKey(creature.getObjectId())) {
            creatures.put(creature.getObjectId(), creature);
            if (creature instanceof Player) {
                players.put(creature.getObjectId(), (Player) creature);
            }
        }
    }

    @Override
    public void onLeaveZone(Creature creature, ZoneInstance zone) {
        if (!isInsideLocation(creature)) {
            creatures.remove(creature.getObjectId());
            players.remove(creature.getObjectId());
        }
    }

    public void doOnAllPlayers(Visitor<Player> visitor) {
        try {
            for (FastMap.Entry<Integer, Player> e = players.head(), mapEnd = players.tail(); (e = e.getNext()) != mapEnd; ) {
                Player player = e.getValue();
                if (player != null) {
                    visitor.visit(player);
                }
            }
        } catch (Exception ex) {
            log.error("Exception when running visitor on all players" + ex);
        }
    }

    /**
     * @return the creatures
     */
    public FastMap<Integer, Creature> getCreatures() {
        return creatures;
    }

    public FastMap<Integer, Player> getPlayers() {
        return players;
    }
}
