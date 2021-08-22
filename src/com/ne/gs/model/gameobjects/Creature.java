/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.gs.ai2.AI2;
import com.ne.gs.ai2.AI2Engine;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.controllers.CreatureController;
import com.ne.gs.controllers.ObserveController;
import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.controllers.effect.EffectController;
import com.ne.gs.controllers.movement.MoveController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.TribeClass;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureSeeState;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.gameobjects.state.CreatureVisualState;
import com.ne.gs.model.stats.container.CreatureGameStats;
import com.ne.gs.model.stats.container.CreatureLifeStats;
import com.ne.gs.model.templates.VisibleObjectTemplate;
import com.ne.gs.model.templates.item.ItemAttackType;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.taskmanager.tasks.PacketBroadcaster;
import com.ne.gs.taskmanager.tasks.PacketBroadcaster.BroadcastMode;
import com.ne.gs.world.MapRegion;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.zone.ZoneName;
import com.ne.gs.model.templates.zone.ZoneClassName;
import com.ne.gs.world.World;
import com.ne.gs.world.zone.ZoneAttributes;
import com.ne.gs.world.zone.ZoneInstance;

/**
 * This class is representing movable objects, its base class for all in game objects that may move
 *
 * @author -Nemesiss-
 */
public abstract class Creature extends VisibleObject {

    private static final Logger log = LoggerFactory.getLogger(Creature.class);

    protected AI2 ai2;

    private CreatureLifeStats<? extends Creature> lifeStats;
    private CreatureGameStats<? extends Creature> gameStats;

    private EffectController effectController;
    protected MoveController moveController;

    private int _state = CreatureState.ACTIVE.getId();
    private int visualState = CreatureVisualState.VISIBLE.getId();
    private int seeState = CreatureSeeState.NORMAL.getId();

    private final Object[] _stateEnv = new Object[CreatureState.values().length];

    private Skill castingSkill;
    private final Map<Integer, Long> skillCoolDowns = new THashMap<>(0);
    private final Map<Integer, Long> skillCoolDownsBase = new THashMap<>(0);
    private final ObserveController observeController;
    private TransformModel transformModel;
    private final AggroList aggroList;

    private int isAdminNeutral = 0;
    private int isAdminEnmity = 0;

    private Item usingItem;

    private final transient byte[] zoneTypes = new byte[ZoneType.values().length];

    private int skillNumber;
    private int attackedCount;
    private volatile byte packetBroadcastMask;

    public Creature(Integer objectId,
                    CreatureController<? extends Creature> controller,
                    SpawnTemplate spawnTemplate,
                    VisibleObjectTemplate objectTemplate,
                    WorldPosition position) {
        super(objectId, controller, spawnTemplate, objectTemplate, position);
        observeController = new ObserveController();
        setTransformModel(new TransformModel(this));
        if ((spawnTemplate != null) && (spawnTemplate.getModel() != null) && (spawnTemplate.getModel()
                                                                                           .getTribe() != null)) {
            getTransformModel().setTribe(spawnTemplate.getModel().getTribe(), true);
        }
        aggroList = createAggroList();
    }

    public MoveController getMoveController() {
        return moveController;
    }

    protected AggroList createAggroList() {
        return new AggroList(this);
    }

    /**
     * Return CreatureController of this Creature object.
     *
     * @return CreatureController.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public CreatureController getController() {
        return (CreatureController) super.getController();
    }

    /**
     * @return the lifeStats
     */
    public CreatureLifeStats<? extends Creature> getLifeStats() {
        return lifeStats;
    }

    /**
     * @param lifeStats
     *     the lifeStats to set
     */
    public void setLifeStats(CreatureLifeStats<? extends Creature> lifeStats) {
        this.lifeStats = lifeStats;
    }

    /**
     * @return the gameStats
     */
    public CreatureGameStats<? extends Creature> getGameStats() {
        return gameStats;
    }

    /**
     * @param gameStats
     *     the gameStats to set
     */
    public void setGameStats(CreatureGameStats<? extends Creature> gameStats) {
        this.gameStats = gameStats;
    }

    public abstract byte getLevel();

    /**
     * @return the effectController
     */
    public EffectController getEffectController() {
        return effectController;
    }

    /**
     * @param effectController
     *     the effectController to set
     */
    public void setEffectController(EffectController effectController) {
        this.effectController = effectController;
    }

    public AI2 getAi2() {
        return ai2 != null ? ai2 : AI2Engine.getInstance().setupAI("dummy", this);
    }

    public void setAi2(AI2 ai2) {
        this.ai2 = ai2;
    }

    /**
     * Is creature casting some skill
     */
    public boolean isCasting() {
        return castingSkill != null;
    }

    /**
     * Set current casting skill or null when skill ends
     */
    public void setCasting(Skill castingSkill) {
        if (castingSkill != null) {
            skillNumber++;
        }
        this.castingSkill = castingSkill;
    }

    /**
     * Current casting skill id
     */
    public int getCastingSkillId() {
        return castingSkill != null ? castingSkill.getSkillTemplate().getSkillId() : 0;
    }

    /**
     * Current casting skill
     */
    public Skill getCastingSkill() {
        return castingSkill;
    }

    public int getSkillNumber() {
        return skillNumber;
    }

    public void setSkillNumber(int skillNumber) {
        this.skillNumber = skillNumber;
    }

    public int getAttackedCount() {
        return attackedCount;
    }

    public void incrementAttackedCount() {
        attackedCount++;
    }

    public void clearAttackedCount() {
        attackedCount = 0;
    }

    /**
     * Is using item
     */
    public boolean isUsingItem() {
        return usingItem != null;
    }

    /**
     * Set using item
     */
    public void setUsingItem(Item usingItem) {
        this.usingItem = usingItem;
    }

    /**
     * get Using ItemId
     */
    public int getUsingItemId() {
        return usingItem != null ? usingItem.getItemTemplate().getTemplateId() : 0;
    }

    /**
     * Using Item
     */
    public Item getUsingItem() {
        return usingItem;
    }

    /**
     * All abnormal effects are checked that disable movements
     */
    public boolean canPerformMove() {
        return !(getEffectController().isAbnormalState(AbnormalState.CANT_MOVE_STATE) || !isSpawned());
    }

    /**
     * All abnormal effects are checked that disable attack
     */
    public boolean canAttack() {
        return !(getEffectController().isAbnormalState(AbnormalState.CANT_ATTACK_STATE) || isCasting() || isInState(CreatureState.RESTING) || isInState(CreatureState.PRIVATE_SHOP));
    }

    public int getState() {
        return _state;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getStateEnv(CreatureState state) {
        return (T) _stateEnv[state.ordinal()];
    }

    public void setState(CreatureState state) {
        _state |= state.getId();
    }

    public void setState(CreatureState state, Object env) {
        _state |= state.getId();
        _stateEnv[state.ordinal()] = env;
    }

    public void setState(int state) {
        _state = state;
        for (int i = 0; i < _stateEnv.length; i++) {
            _stateEnv[i] = null;
        }
    }

    public void unsetState(CreatureState state) {
        _state &= ~state.getId();
        _stateEnv[state.ordinal()] = null;
    }

    public boolean isInState(CreatureState state) {
        int isState = this._state & state.getId();

        return isState == state.getId();
    }
    
    /**
     * @return visualState
     */
    public int getVisualState() {
        return visualState;
    }

    /**
     * @param visualState
     *     the visualState to set
     */
    public void setVisualState(CreatureVisualState visualState) {
        this.visualState |= visualState.getId();
    }

    public void unsetVisualState(CreatureVisualState visualState) {
        this.visualState &= ~visualState.getId();
    }

    public boolean isInVisualState(CreatureVisualState visualState) {
        int isVisualState = this.visualState & visualState.getId();

        return isVisualState == visualState.getId();

    }

    /**
     * @return seeState
     */
    public int getSeeState() {
        return seeState;
    }

    /**
     * @param seeState
     *     the seeState to set
     */
    public void setSeeState(CreatureSeeState seeState) {
        this.seeState |= seeState.getId();
    }

    public void unsetSeeState(CreatureSeeState seeState) {
        this.seeState &= ~seeState.getId();
    }

    public boolean isInSeeState(CreatureSeeState seeState) {
        int isSeeState = this.seeState & seeState.getId();

        return isSeeState == seeState.getId();

    }

    /**
     * @return the transformedModelId
     */
    public TransformModel getTransformModel() {
        return transformModel;
    }

    public final void setTransformModel(TransformModel model) {
        transformModel = model;
    }

    /**
     * @return the aggroList
     */
    public final AggroList getAggroList() {
        return aggroList;
    }

    /**
     * This is adding broadcast to player.
     */
    public final void addPacketBroadcastMask(BroadcastMode mode) {
        packetBroadcastMask |= mode.mask();

        PacketBroadcaster.getInstance().add(this);

        // Debug
        if (log.isDebugEnabled()) {
            log.debug("PacketBroadcaster: Packet " + mode.name() + " added to player " + getName());
        }
    }

    /**
     * This is removing broadcast from player.
     */
    public final void removePacketBroadcastMask(BroadcastMode mode) {
        packetBroadcastMask &= ~mode.mask();

        // Debug
        if (log.isDebugEnabled()) {
            log.debug("PacketBroadcaster: Packet " + mode.name() + " removed from player " + getName()); // fix
            // ClassCastException
        }
    }

    /**
     * Broadcast getter.
     */
    public final byte getPacketBroadcastMask() {
        return packetBroadcastMask;
    }

    /**
     * @return the observeController
     */
    public ObserveController getObserveController() {
        return observeController;
    }

    /**
     * Double dispatch like method
     */
    public boolean isEnemy(Creature creature) {
        return creature.isEnemyFrom(this);
    }

    /**
     * @param creature
     */
    public boolean isEnemyFrom(Creature creature) {
        return false;
    }

    /**
     * @param player
     *
     * @return
     */
    public boolean isEnemyFrom(Player player) {
        return false;
    }

    /**
     * @param npc
     *
     * @return
     */
    public boolean isEnemyFrom(Npc npc) {
        return false;
    }

    public TribeClass getTribe() {
        return TribeClass.GENERAL;
    }

    /**
     * Double dispatch like method
     */
    public boolean isAggressiveTo(Creature creature) {
        return creature.isAggroFrom(this);
    }

    /**
     * @param creature
     *
     * @return
     */
    public boolean isAggroFrom(Creature creature) {
        return false;
    }

    /**
     * @param npc
     *
     * @return
     */
    public boolean isAggroFrom(Npc npc) {
        return false;
    }

    /**
     * @param npc
     *
     * @return
     */
    public boolean isHostileFrom(Npc npc) {
        return false;
    }

    /**
     * @param npc
     */
    public boolean isSupportFrom(Npc npc) {
        return false;
    }

    /**
     * @param npc
     */
    public boolean isFriendFrom(Npc npc) {
        return false;
    }

    @Override
    public boolean canSee(Creature creature) {
        return creature != null && creature.getVisualState() <= getSeeState();
    }

    public boolean isSeeObject(VisibleObject object) {
        return getKnownList().knowns(object);
    }

    public boolean isSeePlayer(Player player) {
        if (SecurityConfig.INVIS && !canSee(player)) {
            return false;
        }
        return getKnownList().knowns(player);
    }

    /**
     * @return NpcObjectType.NORMAL
     */
    public NpcObjectType getNpcObjectType() {
        return NpcObjectType.NORMAL;
    }

    /**
     * For summons and different kind of servants<br>
     * it will return currently acting player.<br>
     * This method is used for duel and enemy relations,<br>
     * rewards<br>
     *
     * @return Master of this creature or self
     */
    public Creature getMaster() {
        return this;
    }

    /**
     * For summons it will return summon object and for <br>
     * servants - player object.<br>
     * Used to find attackable target for npcs.<br>
     *
     * @return acting master - player in case of servants
     */
    public Creature getActingCreature() {
        return this;
    }

    public boolean isSkillDisabled(SkillTemplate template) {
        Integer cooldownId = template.getCooldownId();
        Long cd, baseCd;
        long now = Sys.millis();
        synchronized (skillCoolDowns) {
            cd = skillCoolDowns.get(cooldownId);

            if (cd == null) {
                return false;
            }

            if (cd < now) {
                removeSkillCoolDown(cooldownId);
                return false;
            }

            // Some shared cooldown skills have indipendent and different cooldown they must not be blocked
            baseCd = skillCoolDownsBase.get(cooldownId);
            if (baseCd != null) {
                int tplCd = template.getDuration() + template.getCooldown() * 100;
                if ((tplCd + baseCd) < now) {
                    return false;
                }
            }
        }

        return true;
    }

    public long getSkillCoolDown(Integer cooldownId) {
        Long v;
        synchronized (skillCoolDowns) {
            v = skillCoolDowns.get(cooldownId);
        }

        return v == null ? 0 : v;
    }

    public void setSkillCoolDown(Integer cooldownId, long time) {
        if (cooldownId == 0) {
            return;
        }

        synchronized (skillCoolDowns) {
            skillCoolDowns.put(cooldownId, time);
        }
    }

    @NotNull
    public Map<Integer, Long> getSkillCoolDowns() {
        synchronized (skillCoolDowns) {
            return ImmutableMap.copyOf(skillCoolDowns);
        }
    }

    public void removeSkillCoolDown(Integer cooldownId) {
        synchronized (skillCoolDowns) {
            skillCoolDowns.remove(cooldownId);
            skillCoolDownsBase.remove(cooldownId);
        }
    }

    public void flushSkillCd(int... exceptions) {
        List<Integer> ens = new ArrayList<>(exceptions.length);
        for (int exception : exceptions) {
            SkillTemplate tpl = DataManager.SKILL_DATA.getSkillTemplate(exception);
            if (tpl != null) {
                ens.add(tpl.getCooldownId());
            }
        }

        synchronized (skillCoolDowns) {
            for (Integer delayId : skillCoolDowns.keySet()) {
                if (!ens.contains(delayId)) {
                    //skillCoolDowns.remove(delayId);
                    //skillCoolDownsBase.remove(delayId);
                    skillCoolDowns.put(delayId, (long) 0);
                    skillCoolDownsBase.put(delayId, (long) 0);
                }
            }
        }
    }

    public void flushSkillCd() {
        synchronized (skillCoolDowns) {
            for (Integer delayId : skillCoolDowns.keySet()) {
                skillCoolDowns.put(delayId, (long) 0);
                skillCoolDownsBase.put(delayId, (long) 0);
            }
        }
    }

    public void setSkillCoolDownBase(Integer cooldownId, long baseTime) {
        if (cooldownId == 0) {
            return;
        }

        synchronized (skillCoolDowns) {
            skillCoolDownsBase.put(cooldownId, baseTime);
        }
    }

    /**
     * @return isAdminNeutral value
     */
    public int getAdminNeutral() {
        return isAdminNeutral;
    }

    /**
     * @param newValue
     */
    public void setAdminNeutral(int newValue) {
        isAdminNeutral = newValue;
    }

    /**
     * @return isAdminEnmity value
     */
    public int getAdminEnmity() {
        return isAdminEnmity;
    }

    /**
     * @param newValue
     */
    public void setAdminEnmity(int newValue) {
        isAdminEnmity = newValue;
    }

    public float getCollision() {
        return getObjectTemplate().getBoundRadius().getCollision();
    }

    /**
     * @return
     */
    public boolean isAttackableNpc() {
        return false;
    }

    public ItemAttackType getAttackType() {
        return ItemAttackType.PHYSICAL;
    }

    /**
     * Creature is flying (FLY or GLIDE states)
     */
    public boolean isFlying() {
        return (isInState(CreatureState.FLYING) && !isInState(CreatureState.RESTING)) || isInState(CreatureState.GLIDING);
    }
	
	public boolean isGliding() {
        return (!isInState(CreatureState.RESTING)) && isInState(CreatureState.GLIDING);
    }

    public boolean isInFlyingState() {
        return isInState(CreatureState.FLYING) && !isInState(CreatureState.RESTING);
    }

    public byte isPlayer() {
        return 0;
    }

    public boolean isPvpTarget(Creature creature) {
        return getActingCreature() instanceof Player && creature.getActingCreature() instanceof Player;
    }

    public void revalidateZones() {
        MapRegion mapRegion = getPosition().getMapRegion();
        if (mapRegion != null) {
            mapRegion.revalidateZones(this);
        }
    }

    public boolean isInsideZone(ZoneName zoneName) {
        return isSpawned() && getPosition().getMapRegion().isInsideZone(zoneName, this);
    }

    public void setInsideZoneType(ZoneType zoneType) {
        byte current = zoneTypes[zoneType.getValue()];
        zoneTypes[zoneType.getValue()] = (byte) (current + 1);
    }

    public void unsetInsideZoneType(ZoneType zoneType) {
        byte current = zoneTypes[zoneType.getValue()];
        zoneTypes[zoneType.getValue()] = (byte) (current - 1);
    }

    public boolean isInsideZoneType(ZoneType zoneType) {
        return zoneTypes[zoneType.getValue()] > 0;
    }
    
    public boolean isInsidePvPZone() {
    boolean isDisputedWorld = getWorldId() == 600020000 || getWorldId() == 600030000;
    if (zoneTypes[ZoneType.SIEGE.getValue()] > 0) {
      return true;
    }
    int pvpValue = zoneTypes[ZoneType.PVP.getValue()];
    return isDisputedWorld ? isCurrentZonePvp(pvpValue) : pvpValue == 0 || pvpValue == 2;
    }
    
    private boolean isCurrentZonePvp(int pvpValue) {
    boolean isPvpAllowed = World.getInstance().getWorldMap(getWorldId()).isPvpAllowed();
    if (isPvpAllowed && pvpValue == 0) {
      return true;
    } else if (pvpValue != 1) {
      return false;
    } else {
      List<ZoneInstance> zones = getPosition().getMapRegion().getZones(this);
      for (ZoneInstance zone : zones) {
	if (zone.getZoneTemplate().getZoneType().equals(ZoneClassName.PVP) && !((zone.getZoneTemplate().getFlags() & ZoneAttributes.PVP_ENABLED.getId()) != 0)) {
	  return false;
	}
      }
      return true;
    }
  }

    public Race getRace() {
        return Race.NONE;
    }

    public int getSkillCooldown(SkillTemplate template) {
        return template.getCooldown();
    }

    public int getItemCooldown(ItemTemplate template) {
        return template.getUseLimits().getDelayTime();
    }
	
    public int getCriticalEffectMulti() {
        return CriticalEffectMulti;
    }

    public void setCriticalEffectMulti(int criticalEffectMulti) {
        CriticalEffectMulti = criticalEffectMulti;
    }

    private int CriticalEffectMulti = 1;
    
      public boolean isInsideZoneClassName(ZoneClassName name) {
    if (getActiveRegion() == null) {
      return false;
    }
    List<ZoneInstance> zones = getActiveRegion().getZones(this);
    for (ZoneInstance zone : zones) {
      if (zone.getZoneTemplate().getZoneType().equals(name)) {
	return true;
      }
    }
    return false;
  }

  public boolean isInsideZoneClassName(String name) {
    if (getActiveRegion() == null) {
      return false;
    }
    List<ZoneInstance> zones = getActiveRegion().getZones(this);
    for (ZoneInstance zone : zones) {
      if (zone.getZoneTemplate().getName() == ZoneName.get(name)) {
	return true;
      }
    }
    return false;
  }
}
