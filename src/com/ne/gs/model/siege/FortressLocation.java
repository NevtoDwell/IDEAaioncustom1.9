/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import java.util.List;

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.siegelocation.SiegeLegionReward;
import com.ne.gs.model.templates.siegelocation.SiegeLocationTemplate;
import com.ne.gs.model.templates.siegelocation.SiegeReward;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.world.zone.ZoneInstance;

/**
 * @author Source
 */
public class FortressLocation extends SiegeLocation {

    protected List<SiegeReward> siegeRewards;
    protected List<SiegeLegionReward> siegeLegionRewards;
    protected boolean isUnderShield;
    protected boolean isUnderAssault;
    protected boolean isCanTeleport;

    public FortressLocation() {
    }

    public FortressLocation(SiegeLocationTemplate template) {
        super(template);
        siegeRewards = template.getSiegeRewards() != null ? template.getSiegeRewards() : null;
        siegeLegionRewards = template.getSiegeLegionRewards() != null ? template.getSiegeLegionRewards() : null;
    }

    public List<SiegeReward> getReward() {
        return siegeRewards;
    }

    public List<SiegeLegionReward> getLegionReward() {
        return siegeLegionRewards;
    }

    /**
     * @return isEnemy
     */
    public boolean isEnemy(Creature creature) {
        return creature.getRace().getRaceId() != getRace().getRaceId();
    }

    /**
     * @return isUnderShield
     */
    @Override
    public boolean isUnderShield() {
        return isUnderShield;
    }

    /**
     * @param value
     *     new undershield value
     */
    @Override
    public void setUnderShield(boolean value) {
        isUnderShield = value;
    }

    /**
     * @return isCanTeleport
     */
    @Override
    public boolean isCanTeleport(Player player) {
        if (player == null) {
            return isCanTeleport;
        }
        return isCanTeleport && player.getRace().getRaceId() == getRace().getRaceId();
    }

    /**
     * @param status
     *     Teleportation status
     */
    @Override
    public void setCanTeleport(boolean status) {
        isCanTeleport = status;
    }

    /**
     * @return DescriptionId object with fortress name
     */
    public DescId getNameAsDescriptionId() {
        return DescId.of(template.getNameId());
    }

    @Override
    public void onEnterZone(Creature creature, ZoneInstance zone) {
        super.onEnterZone(creature, zone);
        if (isVulnerable()) {
            creature.setInsideZoneType(ZoneType.SIEGE);
        }
    }

    @Override
    public void onLeaveZone(Creature creature, ZoneInstance zone) {
        super.onLeaveZone(creature, zone);
        if (isVulnerable()) {
            creature.unsetInsideZoneType(ZoneType.SIEGE);
        }
    }

    @Override
    public void clearLocation() {
        for (Creature creature : getCreatures().values()) {
            if ((isEnemy(creature)) && ((creature instanceof Kisk))) {
                Kisk kisk = (Kisk) creature;
                kisk.getController().die();
            }
        }

        for (Player player : getPlayers().values()) {
            if (isEnemy(player)) {
                TeleportService.moveToBindLocation(player, true);
            }
        }
    }

}
