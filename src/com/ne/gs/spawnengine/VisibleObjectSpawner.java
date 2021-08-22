/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.spawnengine;

import mw.engines.geo.GeoEngine;
import mw.engines.geo.GeoHelper;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.SiegeConfig;
import com.ne.gs.controllers.GatherableController;
import com.ne.gs.controllers.NpcController;
import com.ne.gs.controllers.PetController;
import com.ne.gs.controllers.SiegeWeaponController;
import com.ne.gs.controllers.SummonController;
import com.ne.gs.controllers.effect.EffectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.NpcData;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.*;
import com.ne.gs.model.gameobjects.player.PetCommonData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.gameobjects.state.CreatureVisualState;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.VisibleObjectTemplate;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.pet.PetTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_STATE;
import com.ne.gs.services.SiegeService;
import com.ne.gs.services.SkillLearnService;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.properties.Properties;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.CreatureAwareKnownList;
import com.ne.gs.world.knownlist.NpcKnownList;
import com.ne.gs.world.knownlist.PlayerAwareKnownList;

/**
 * @author ATracer
 */
public final class VisibleObjectSpawner {

    private static final Logger log = LoggerFactory.getLogger(VisibleObjectSpawner.class);

    /**
     * @param spawn
     * @param instanceIndex
     *
     * @return
     */
    protected static VisibleObject spawnNpc(SpawnTemplate spawn, int instanceIndex) {
        int objectId = spawn.getNpcId();
        NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(objectId);
        if (npcTemplate == null) {
            log.error("No template for NPC " + String.valueOf(objectId));
            return null;
        }
        IDFactory iDFactory = IDFactory.getInstance();

        Npc npc = new Npc(iDFactory.nextId(), new NpcController(), spawn, npcTemplate);
        npc.setCreatorId(spawn.getCreatorId());
        npc.setMasterName(spawn.getMasterName());
        npc.setKnownlist(new NpcKnownList(npc));

        npc.setEffectController(new EffectController(npc));

        if (WalkerFormator.getInstance().processClusteredNpc(npc, instanceIndex)) {
            return npc;
        }

        try {
            SpawnEngine.bringIntoWorld(npc, spawn, instanceIndex);
        } catch (Exception ex) {
            log.error("Error during spawn of npc {}, world {}, x-y {}-{}",
                npcTemplate.getTemplateId(), spawn.getWorldId(), spawn.getX(), spawn.getY());
            log.error("Npc {} will be despawned", npcTemplate.getTemplateId(), ex);
            World.getInstance().despawn(npc);
        }
        return npc;
    }

    /**
     * @param spawn
     * @param instanceIndex
     *
     * @return
     */
    protected static VisibleObject spawnSiegeNpc(SiegeSpawnTemplate spawn, int instanceIndex) {
        if (!SiegeConfig.SIEGE_ENABLED) {
            return null;
        }

        int objectId = spawn.getNpcId();
        NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(objectId);
        if (npcTemplate == null) {
            log.error("No template for NPC " + String.valueOf(objectId));
            return null;
        }
        IDFactory iDFactory = IDFactory.getInstance();
        Npc npc = null;

        int spawnSiegeId = spawn.getSiegeId();
        SiegeLocation loc = SiegeService.getInstance().getSiegeLocation(spawnSiegeId);
        if ((spawn.isPeace() || loc.isVulnerable()) && spawnSiegeId == loc.getLocationId() && spawn.getSiegeRace() == loc.getRace()) {
            npc = new SiegeNpc(iDFactory.nextId(), new NpcController(), spawn, npcTemplate);
            npc.setKnownlist(new NpcKnownList(npc));
        } else if (spawn.isAssault() && loc.isVulnerable() && spawn.getSiegeRace().equals(SiegeRace.BALAUR)) {
            npc = new SiegeNpc(iDFactory.nextId(), new NpcController(), spawn, npcTemplate);
            npc.setKnownlist(new NpcKnownList(npc));
        } else {
            return null;
        }
        npc.setEffectController(new EffectController(npc));
        SpawnEngine.bringIntoWorld(npc, spawn, instanceIndex);
        return npc;
    }

    /**
     * @param spawn
     * @param instanceIndex
     *
     * @return
     */
    protected static VisibleObject spawnGatherable(SpawnTemplate spawn, int instanceIndex) {
        int objectId = spawn.getNpcId();
        VisibleObjectTemplate template = DataManager.GATHERABLE_DATA.getGatherableTemplate(objectId);
        Gatherable gatherable = new Gatherable(spawn, template, IDFactory.getInstance().nextId(), new GatherableController());
        gatherable.setKnownlist(new PlayerAwareKnownList(gatherable));
        SpawnEngine.bringIntoWorld(gatherable, spawn, instanceIndex);
        return gatherable;
    }

    /**
     * @param spawn
     * @param instanceIndex
     * @param creator
     *
     * @return
     */
    public static Trap spawnTrap(SpawnTemplate spawn, int instanceIndex, Creature creator, int skillId) {
        int objectId = spawn.getNpcId();
        NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(objectId);
        Trap trap = new Trap(IDFactory.getInstance().nextId(), new NpcController(), spawn, npcTemplate);
        trap.setKnownlist(new NpcKnownList(trap));
        trap.setEffectController(new EffectController(trap));
        trap.setCreator(creator);
        trap.getSkillList().addSkill(trap, skillId, 1);
        trap.setVisualState(CreatureVisualState.HIDE1);
        // set proper trap range
        SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
        if (skillTemplate == null) {
            trap.getAi2().onCustomEvent(1, 15); // TODO validate
        } else {
            Properties properties = skillTemplate.getProperties();
            int targetDistance = properties.getTargetDistance();
            trap.getAi2().onCustomEvent(1, targetDistance);
        }


        SpawnEngine.bringIntoWorld(trap, spawn, instanceIndex);
        PacketSendUtility.broadcastPacket(trap, new SM_PLAYER_STATE(trap));
        return trap;
    }

    /**
     * @param spawn
     * @param instanceIndex
     * @param creator
     *
     * @return
     */
    public static GroupGate spawnGroupGate(SpawnTemplate spawn, int instanceIndex, Creature creator) {
        int objectId = spawn.getNpcId();
        NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(objectId);
        GroupGate groupgate = new GroupGate(IDFactory.getInstance().nextId(), new NpcController(), spawn, npcTemplate);
        groupgate.setKnownlist(new PlayerAwareKnownList(groupgate));
        groupgate.setEffectController(new EffectController(groupgate));
        groupgate.setCreator(creator);
        SpawnEngine.bringIntoWorld(groupgate, spawn, instanceIndex);
        return groupgate;
    }

    /**
     * @param spawn
     * @param instanceIndex
     * @param creator
     *
     * @return
     */
    public static Kisk spawnKisk(SpawnTemplate spawn, int instanceIndex, Player creator) {
        int npcId = spawn.getNpcId();
        NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(npcId);
        Kisk kisk = new Kisk(IDFactory.getInstance().nextId(), new NpcController(), spawn, template, creator);
        kisk.setKnownlist(new PlayerAwareKnownList(kisk));
        kisk.setCreator(creator);
        kisk.setEffectController(new EffectController(kisk));
        SpawnEngine.bringIntoWorld(kisk, spawn, instanceIndex);
        return kisk;
    }

    /**
     * @param owner
     *
     * @author ViAl Spawns postman for express mail
     */
    public static Npc spawnPostman(Player owner) {
        int npcId = owner.getRace() == Race.ELYOS ? 798100 : 798101;
        NpcData npcData = DataManager.NPC_DATA;
        NpcTemplate template = npcData.getNpcTemplate(npcId);
        IDFactory iDFactory = IDFactory.getInstance();
        int worldId = owner.getWorldId();
        int instanceId = owner.getInstanceId();
        double radian = Math.toRadians(MathUtil.convertHeadingToDegree(owner.getHeading()));

        Vector3f pos = GeoEngine.getAvailablePoint(
                owner,
                (float) (Math.cos(radian) * 5),
                (float) (Math.sin(radian) * 5),
                CollidableType.PHYSICAL.getId());


        SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, pos.getX(), pos.getY(), pos.getZ(), (byte) 0);
        Npc postman = new Npc(iDFactory.nextId(), new NpcController(), spawn, template);
        postman.setKnownlist(new PlayerAwareKnownList(postman));
        postman.setEffectController(new EffectController(postman));
        postman.getAi2().onCustomEvent(1, owner);
        SpawnEngine.bringIntoWorld(postman, spawn, instanceId);
        owner.setPostman(postman);
        return postman;
    }

    /**
     * @param spawn
     * @param instanceIndex
     * @param creator
     * @param skillId
     * @param level
     *
     * @return
     */
    public static Servant spawnServant(SpawnTemplate spawn, int instanceIndex, Creature creator, int skillId, int level,
                                       NpcObjectType objectType) {
        int objectId = spawn.getNpcId();
        NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(objectId);

        int creatureLevel = creator.getLevel();
        level = SkillLearnService.getSkillLearnLevel(skillId, creatureLevel, level);
        byte servantLevel = (byte) SkillLearnService.getSkillMinLevel(skillId, creatureLevel, level);

        Servant servant = new Servant(IDFactory.getInstance().nextId(), new NpcController(), spawn, npcTemplate, servantLevel);
        servant.setKnownlist(new NpcKnownList(servant));
        servant.setEffectController(new EffectController(servant));
        servant.setCreator(creator);
        servant.setNpcObjectType(objectType);
        servant.getSkillList().addSkill(servant, skillId, 1);
        servant.setTarget(creator.getTarget());
        SpawnEngine.bringIntoWorld(servant, spawn, instanceIndex);
        return servant;
    }

    /**
     * @param spawn
     * @param instanceIndex
     * @param creator
     * @param attackCount
     *
     * @return
     */
    public static Homing spawnHoming(SpawnTemplate spawn, int instanceIndex, Creature creator, int attackCount, int skillId,
                                     int level) {
        int objectId = spawn.getNpcId();
        NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(objectId);

        int creatureLevel = creator.getLevel();
        level = SkillLearnService.getSkillLearnLevel(skillId, creatureLevel, level);
        byte homingLevel = (byte) SkillLearnService.getSkillMinLevel(skillId, creatureLevel, level);

        Homing homing = new Homing(IDFactory.getInstance().nextId(), new NpcController(), spawn, npcTemplate, homingLevel);
        homing.setState(CreatureState.WEAPON_EQUIPPED);
        homing.setKnownlist(new NpcKnownList(homing));
        homing.setEffectController(new EffectController(homing));
        homing.setCreator(creator);
        homing.setAttackCount(attackCount);
        SpawnEngine.bringIntoWorld(homing, spawn, instanceIndex);
        return homing;
    }

    /**
     * @param creator
     * @param npcId
     * @param skillLevel
     *
     * @return
     */
    public static Summon spawnSummon(Player creator, int npcId, int skillId, int skillLevel, int time) {
        float x = creator.getX();
        float y = creator.getY();
        float z = creator.getZ();
        int heading = creator.getHeading();
        int worldId = creator.getWorldId();
        int instanceId = creator.getInstanceId();

        SpawnTemplate spawn = SpawnEngine.createSpawnTemplate(worldId, npcId, x, y, z, heading);
        NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(npcId);

        skillLevel = SkillLearnService.getSkillLearnLevel(skillId, creator.getCommonData().getLevel(), skillLevel);
        byte level = (byte) SkillLearnService.getSkillMinLevel(skillId, creator.getCommonData().getLevel(), skillLevel);
        boolean isSiegeWeapon = npcTemplate.getAi().equals("siege_weapon");
        Summon summon = new Summon(IDFactory.getInstance().nextId(), isSiegeWeapon ? new SiegeWeaponController(npcId) : new SummonController(), spawn,
            npcTemplate, isSiegeWeapon ? npcTemplate.getLevel() : level, time);
        summon.setKnownlist(new CreatureAwareKnownList(summon));
        summon.setEffectController(new EffectController(summon));
        summon.setMaster(creator);
        summon.getLifeStats().synchronizeWithMaxStats();

        SpawnEngine.bringIntoWorld(summon, spawn, instanceId);
        return summon;
    }

    /**
     * @param player
     * @param petId
     *
     * @return
     */
    public static Pet spawnPet(Player player, int petId) {

        PetCommonData petCommonData = player.getPetList().getPet(petId);
        if (petCommonData == null) {
            return null;
        }
        PetTemplate petTemplate = DataManager.PET_DATA.getPetTemplate(petId);
        if (petTemplate == null) {
            return null;
        }

        PetController controller = new PetController();
        Pet pet = new Pet(petTemplate, controller, petCommonData, player);
        pet.setKnownlist(new PlayerAwareKnownList(pet));
        player.setToyPet(pet);

        float x = player.getX();
        float y = player.getY();
        float z = player.getZ();
        int heading = player.getHeading();
        int worldId = player.getWorldId();
        int instanceId = player.getInstanceId();
        SpawnTemplate spawn = SpawnEngine.createSpawnTemplate(worldId, petId, x, y, z, heading);

        SpawnEngine.bringIntoWorld(pet, spawn, instanceId);
        return pet;
    }
}
