/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import java.util.Iterator;
import com.google.common.base.Preconditions;

import com.ne.gs.ai2.AI2Engine;
import com.ne.gs.ai2.AITemplate;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.configs.main.AIConfig;
import com.ne.gs.configs.main.GeoDataConfig;
import com.ne.gs.controllers.NpcController;
import com.ne.gs.controllers.movement.NpcMoveController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.NpcType;
import com.ne.gs.model.Race;
import com.ne.gs.model.TribeClass;
import com.ne.gs.model.drop.NpcDrop;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.skill.NpcSkillList;
import com.ne.gs.model.stats.container.NpcGameStats;
import com.ne.gs.model.stats.container.NpcLifeStats;
import com.ne.gs.model.templates.npc.NpcRating;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.npc.NpcTemplateType;
import com.ne.gs.model.templates.npcshout.NpcShout;
import com.ne.gs.model.templates.npcshout.ShoutEventType;
import com.ne.gs.model.templates.npcshout.ShoutType;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.network.aion.serverpackets.SM_LOOKATOBJECT;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.spawnengine.WalkerGroup;
import com.ne.gs.spawnengine.WalkerGroupShift;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.WorldType;

/**
 * This class is a base class for all in-game NPCs, what includes: monsters and npcs that player can talk to (aka Citizens)
 *
 * @author Luno
 */
public class Npc extends Creature {

    private WalkerGroup walkerGroup;
    private boolean isQuestBusy = false;
    private final NpcSkillList skillList;
    private WalkerGroupShift walkerGroupShift;
    private long lastShoutedSeconds;
    private String masterName = "";
    private int creatorId = 0;

    public Npc(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
        this(objId, controller, spawnTemplate, objectTemplate, objectTemplate.getLevel());
    }

    public Npc(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, byte level) {
        super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition());
        Preconditions.checkNotNull(objectTemplate, "Npcs should be based on template");
        controller.setOwner(this);

        moveController = new NpcMoveController(this);

        skillList = new NpcSkillList(this);
        setupStatContainers(level);
        boolean aiOverride = false;
        if ((spawnTemplate.getModel() != null) && (spawnTemplate.getModel().getAi() != null)) {
            aiOverride = true;
            AI2Engine.getInstance().setupAI(spawnTemplate.getModel().getAi(), this);
        }

        if (!aiOverride) {
            AI2Engine.getInstance().setupAI(objectTemplate.getAi(), this);
        }
        lastShoutedSeconds = System.currentTimeMillis() / 1000;
    }

    @Override
    public NpcMoveController getMoveController() {
        return (NpcMoveController) super.getMoveController();
    }

    /**
     * @param level
     */
    protected void setupStatContainers(byte level) {
        setGameStats(new NpcGameStats(this));
        setLifeStats(new NpcLifeStats(this));
    }

    @Override
    public NpcTemplate getObjectTemplate() {
        return (NpcTemplate) objectTemplate;
    }

    @Override
    public String getName() {
        return getObjectTemplate().getName();
    }

    public int getNpcId() {
        return getObjectTemplate().getTemplateId();
    }

    @Override
    public byte getLevel() {
        return getObjectTemplate().getLevel();
    }

    @Override
    public NpcLifeStats getLifeStats() {
        return (NpcLifeStats) super.getLifeStats();
    }

    @Override
    public NpcGameStats getGameStats() {
        return (NpcGameStats) super.getGameStats();
    }

    @Override
    public NpcController getController() {
        return (NpcController) super.getController();
    }

    public NpcSkillList getSkillList() {
        return skillList;
    }

    public boolean hasWalkRoutes() {
        return getSpawn().getWalkerId() != null || (getSpawn().hasRandomWalk() && AIConfig.ACTIVE_NPC_MOVEMENT);
    }

    public boolean isPeace() {
        return getNpcType().equals(NpcType.PEACE);
    }

    @Override
    public boolean isAggressiveTo(Creature creature) {
        if (creature instanceof Player) {
            return creature.isAggroFrom(this);
        } else if (creature instanceof Summon) {
            return creature.isAggroFrom(this);
        }

        if (DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(getTribe(), creature.getTribe())) {
            return true;
        } else {
            return (creature instanceof Npc && guardAgainst((Npc) creature));
        }
    }
    /**
     * Represents the action of a guard defending its position
     *
     * @param npc
     *
     * @return true if this npc is a guard and the given npc is aggro to their PC race
     */
    public boolean guardAgainst(Npc npc) {
        /*
		 * Until further testing or reports, npc's will not attack npc's with same name(self). Only happens with guard type npc's. This fixes certain NPC's like
		 * ascension that should not attack each other breaking the quest. Example: http://www.aiondatabase.com/npc/205040/guardian-assassin
		 */
        if (getRace() == npc.getRace()) {
            return false;
        }

        if ((getTribe().isLightGuard() || (getRace() == Race.ELYOS && getObjectTemplate().getNpcTemplateType() == NpcTemplateType.GUARD))
            && (DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(npc.getTribe(), TribeClass.PC) || npc.getTribe() == TribeClass.GENERAL_DARK)) {
            return true;
        }
        if ((getTribe().isDarkGuard() || (getRace() == Race.ASMODIANS && getObjectTemplate().getNpcTemplateType() == NpcTemplateType.GUARD))
            && (DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(npc.getTribe(), TribeClass.PC_DARK) || npc.getTribe() == TribeClass.GENERAL)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean affectedByObstacles(){

        //TODO temporary solution
        int npcId = getNpcId();

        switch (npcId)
        {
            case 217239:	//Недособранный Флетус
            case 217233:	//Хранитель коньона Эфра
			case 216264:	//Рудра бури
            case 282189:	//Призванный Флетус
            case 282269:	//Наказание Флетуса
            case 218085:	//Всемогущий Дбарим
                return false;
        }

        return true;
    }

    @Override
    public boolean isAggroFrom(Npc npc) {
        return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(npc.getTribe(), getTribe());
    }

    @Override
    public boolean isHostileFrom(Npc npc) {
        return DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(npc.getTribe(), getTribe());
    }

    @Override
    public boolean isSupportFrom(Npc npc) {
        return DataManager.TRIBE_RELATIONS_DATA.isSupportRelation(npc.getTribe(), getTribe());
    }

    @Override
    public boolean isFriendFrom(Npc npc) {
        return DataManager.TRIBE_RELATIONS_DATA.isFriendlyRelation(npc.getTribe(), getTribe());
    }

    @Override
    public TribeClass getTribe() {
        TribeClass transformTribe = getTransformModel().getTribe();
        if (transformTribe != null) {
            return transformTribe;
        }
        return getObjectTemplate().getTribe();
    }

    public int getAggroRange() {
        return getObjectTemplate().getAggroRange();
    }

    /**
     * Check whether npc located near initial spawn location
     *
     * @return true or false
     */
    public boolean isAtSpawnLocation() {
        return getDistanceToSpawnLocation() < 3;
    }

    /**
     * @return distance to spawn location
     */
    public double getDistanceToSpawnLocation() {
        return MathUtil.getDistance(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), getX(), getY(), getZ());
    }

    @Override
    public boolean isEnemy(Creature creature) {
        if ((creature instanceof Player) && getAi2().ask(AIQuestion.CAN_ATTACK_PLAYER).isPositive()) {
            return true;
        }
        return creature.isEnemyFrom(this);
    }

    @Override
    public boolean isEnemyFrom(Npc npc) {
        if (npc.isFriendFrom(this)) {
            return false;
        }
        return isAggressiveTo(npc) || npc.getAggroList().isHating(this) || getAggroList().isHating(npc);
    }

    @Override
    public boolean isEnemyFrom(Player player) {
        return isAttackableNpc() || player.isAggroIconTo(this);
    }

    @Override
    public int getSeeState() {
        int skillSeeState = super.getSeeState();
        int congenitalSeeState = getObjectTemplate().getRating().getCongenitalSeeState().getId();
        return Math.max(skillSeeState, congenitalSeeState);
    }

    public boolean getIsQuestBusy() {
        return isQuestBusy;
    }

    public void setIsQuestBusy(boolean busy) {
        isQuestBusy = busy;
    }

    @Override
    public boolean isAttackableNpc() {
        return getNpcType() == NpcType.ATTACKABLE;
    }

    /**
     * @return Name of the Creature who summoned this Npc
     */
    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    /**
     * @return UniqueId of the Creature who summoned this Npc
     */
    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * @return
     */
    public VisibleObject getCreator() {
        return null;
    }

    @Override
    public void setTarget(VisibleObject creature) {
        if (getTarget() != creature) {
            super.setTarget(creature);
            super.clearAttackedCount();
            getGameStats().renewLastChangeTargetTime();
            if (!getLifeStats().isAlreadyDead()) {
                PacketSendUtility.broadcastPacket(this, new SM_LOOKATOBJECT(this));
            }
        }
    }

    public void setWalkerGroup(WalkerGroup wg) {
        walkerGroup = wg;
    }

    public WalkerGroup getWalkerGroup() {
        return walkerGroup;
    }

    public void setWalkerGroupShift(WalkerGroupShift shift) {
        walkerGroupShift = shift;
    }

    public WalkerGroupShift getWalkerGroupShift() {
        return walkerGroupShift;
    }

    public boolean isBoss() {
        return getObjectTemplate().getRating() == NpcRating.HERO || getObjectTemplate().getRating() == NpcRating.LEGENDARY;
    }

    public boolean hasStatic() {
        return getSpawn().getStaticId() != 0;
    }

    @Override
    public Race getRace() {
        return getObjectTemplate().getRace();
    }

    public NpcDrop getNpcDrop() {
        return getObjectTemplate().getNpcDrop();
    }

    public NpcType getNpcType() {
        return getObjectTemplate().getNpcType();
    }

    public boolean isRewardAP() {
        if ((this instanceof SiegeNpc)) {
            return false;
        }
        if (getWorldType() == WorldType.ABYSS) {
            return true;
        }
        if (getAi2().ask(AIQuestion.SHOULD_REWARD_AP).isPositive()) {
            return true;
        }
        if (getWorldType() == WorldType.BALAUREA) {
            return (getRace() == Race.DRAKAN) || (getRace() == Race.LIZARDMAN);
        }
        return false;
    }

    public boolean mayShout(int delaySeconds) {
        if (!DataManager.NPC_SHOUT_DATA.hasAnyShout(getPosition().getMapId(), getNpcId())) {
            return false;
        }
        return (System.currentTimeMillis() - lastShoutedSeconds) / 1000 >= delaySeconds;
    }

    public void shout(final NpcShout shout, final Creature target, Object param, int delaySeconds) {
        if (shout.getWhen() != ShoutEventType.DIED && shout.getWhen() != ShoutEventType.BEFORE_DESPAWN && getLifeStats().isAlreadyDead()
            || !mayShout(delaySeconds)) {
            return;
        }

        if (shout.getPattern() != null && !((AITemplate) getAi2()).onPatternShout(shout.getWhen(), shout.getPattern(), shout.getSkillNo())) {
            return;
        }
        final int shoutRange = getObjectTemplate().getMinimumShoutRange();
        if (shout.getShoutType() == ShoutType.SAY && !(target instanceof Player) || target != null && !MathUtil.isIn3dRange(target, this, shoutRange)) {
            return;
        }

        final Npc thisNpc = this;
        final SM_SYSTEM_MESSAGE message = new SM_SYSTEM_MESSAGE(true, shout.getStringId(), getObjectId(), 1, param);
        lastShoutedSeconds = System.currentTimeMillis() / 1000;

        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (thisNpc.getLifeStats().isAlreadyDead() && shout.getWhen() != ShoutEventType.DIED && shout.getWhen() != ShoutEventType.BEFORE_DESPAWN) {
                    return;
                }

                // message for the specific player (when IDLE we are already broadcasting!!!)
                if (shout.getShoutType() == ShoutType.SAY || shout.getWhen() == ShoutEventType.IDLE) {
                    // [RR] Should we have lastShoutedSeconds separated from broadcasts (??)
                    ((Player) target).sendPck(message);
                } else {
                    Iterator<Player> iter = thisNpc.getKnownList().getKnownPlayers().values().iterator();
                    while (iter.hasNext()) {
                        Player kObj = iter.next();
                        if (kObj.getLifeStats().isAlreadyDead() || !kObj.isOnline()) {
                            continue;
                        }
                        if (MathUtil.isIn3dRange(kObj, thisNpc, shoutRange)) {
                            kObj.sendPck(message);
                        }
                    }
                }
            }
        }, delaySeconds * 1000);
    }

}
