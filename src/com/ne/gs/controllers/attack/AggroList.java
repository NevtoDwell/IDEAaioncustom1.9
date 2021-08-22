/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.attack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javolution.util.FastMap;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple3;
import com.ne.commons.utils.EventNotifier;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.SummonedObject;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer, KKnD
 */
@SuppressWarnings("rawtypes")
public class AggroList {

    protected final Creature owner;
    private final FastMap<Integer, AggroInfo> aggroList = new FastMap<Integer, AggroInfo>().shared();

    public AggroList(Creature owner) {
        this.owner = owner;
    }

    /**
     * Only add damage from enemies. (Verify this includes summons, traps, pets,
     * and excludes fall damage.)
     */
    public void addDamage(Creature attacker, int damage) {
        if (!isAware(attacker)) {
            return;
        }

        AggroInfo ai = getAggroInfo(attacker);
        ai.addDamage(damage);
        /**
         * For now we add hate equal to each damage received Additionally there
         * will be broadcast of extra hate
         */
        ai.addHate(damage);

        // TODO move out to controller
        owner.getAi2().onCreatureEvent(AIEventType.ATTACK, attacker);

        EventNotifier.GLOBAL.fire(AddDamageValueCallback.class, Tuple3.of(this, attacker, damage));
    }

    /**
     * Extra hate that is received from using non-damange skill effects
     */
    public void addHate(Creature creature, int hate) {
        if (!isAware(creature)) {
            return;
        }

        addHateValue(creature, hate);
    }

    /**
     * start hating creature by adding 1 hate value
     */
    public void startHate(Creature creature) {
        addHateValue(creature, 1);
    }

    protected void addHateValue(Creature creature, int hate) {
        AggroInfo ai = getAggroInfo(creature);
        ai.addHate(hate);
        if (creature instanceof Player && owner instanceof Npc) {
            for (Player player : owner.getKnownList().getKnownPlayers().values()) {
                if (MathUtil.isIn3dRange(owner, player, 50)) {
                    QuestEngine.getInstance().onAddAggroList(new QuestEnv(owner, player, 0, 0));
                }
            }
        }
        // TODO move out to controller
        owner.getAi2().onCreatureEvent(AIEventType.ATTACK, creature);
    }

    public boolean hasNpcInDamageList() {
        for (AggroInfo ai : aggroList.values()) {
            AionObject aobj = ai.getAttacker();

            if (aobj == null || owner.equals(aobj)) {
                continue;
            }

            if (aobj instanceof Npc && !(aobj instanceof SummonedObject)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return player/group/alliance with most damage.
     */
    public AionObject getMostDamage() {
        AionObject mostDamage = null;
        int maxDamage = 0;

        for (AggroInfo ai : getFinalDamageList(true)) {
            if (ai.getAttacker() == null || owner.equals(ai.getAttacker())) {
                continue;
            }

            if (ai.getDamage() > maxDamage) {
                mostDamage = ai.getAttacker();
                maxDamage = ai.getDamage();
            }
        }

        return mostDamage;
    }

    public Race getPlayerWinnerRace() {
        AionObject winner = getMostDamage();
        if (winner instanceof PlayerGroup) {
            return ((PlayerGroup) winner).getRace();
        } else if (winner instanceof Player) {
            return ((Player) winner).getRace();
        }
        return null;
    }

    /**
     * @return player with most damage
     */
    public Player getMostPlayerDamage() {
        if (aggroList.isEmpty()) {
            return null;
        }

        Player mostDamage = null;
        int maxDamage = 0;

        // Use final damage list to get pet damage as well.
        for (AggroInfo ai : getFinalDamageList(false)) {
            if (ai.getDamage() > maxDamage) {
                mostDamage = (Player) ai.getAttacker();
                maxDamage = ai.getDamage();
            }
        }

        return mostDamage;
    }

    /**
     * @return player with most damage
     */
    public Player getMostPlayerDamageOfMembers(Collection<Player> team, int highestLevel) {
        if (aggroList.isEmpty()) {
            return null;
        }

        Player mostDamage = null;
        int maxDamage = 0;

        // Use final damage list to get pet damage as well.
        for (AggroInfo ai : getFinalDamageList(false)) {
            if (!team.contains(ai.getAttacker())) {
                continue;
            }
            if (ai.getDamage() > maxDamage) {

                mostDamage = (Player) ai.getAttacker();
                maxDamage = ai.getDamage();
            }
        }

        if (mostDamage != null && mostDamage.isMentor()) {
            for (Player member : team) {
                if (member.getLevel() == highestLevel) {
                    mostDamage = member;
                }
            }
        }
        return mostDamage;
    }

    /**
     * @return most hated creature
     */
    public Creature getMostHated() {
        if (aggroList.isEmpty()) {
            return null;
        }

        Creature mostHated = null;
        int maxHate = 0;

        for (FastMap.Entry<Integer, AggroInfo> e = aggroList.head(), mapEnd = aggroList.tail(); (e = e.getNext()) != mapEnd;) {
            AggroInfo ai = e.getValue();
            if (ai == null) {
                continue;
            }

            // aggroList will never contain anything but creatures
            Creature attacker = (Creature) ai.getAttacker();

            if (attacker.getLifeStats().isAlreadyDead()) {
                ai.setHate(0);
            }

            if (ai.getHate() > maxHate) {
                mostHated = attacker;
                maxHate = ai.getHate();
            }
        }

        return mostHated;
    }

    /**
     * @param creature
     *
     * @return
     */
    public boolean isMostHated(Creature creature) {
        if (creature == null || creature.getLifeStats().isAlreadyDead()) {
            return false;
        }

        Creature mostHated = getMostHated();
        return mostHated != null && mostHated.equals(creature);

    }

    /**
     * @param creature
     * @param value
     */
    public void notifyHate(Creature creature, int value) {
        if (isHating(creature)) {
            addHate(creature, value);
        }
    }

    /**
     * @param creature
     */
    public void stopHating(VisibleObject creature) {
        AggroInfo aggroInfo = aggroList.get(creature.getObjectId());
        if (aggroInfo != null) {
            aggroInfo.setHate(0);
        }
    }

    /**
     * Remove completely creature from aggro list
     */
    public void remove(Creature creature) {
        aggroList.remove(creature.getObjectId());
    }

    /**
     * Clear aggroList
     */
    public void clear() {
        aggroList.clear();
    }

    /**
     * @return aggroInfo
     */
    public AggroInfo getAggroInfo(Creature creature) {
        AggroInfo ai = aggroList.get(creature.getObjectId());
        if (ai == null) {
            ai = new AggroInfo(creature);
            aggroList.put(creature.getObjectId(), ai);
        }
        return ai;
    }

    /**
     * @return boolean
     */
    public boolean isHating(Creature creature) {
        return aggroList.containsKey(creature.getObjectId());
    }

    /**
     * @return aggro list
     */
    public Collection<AggroInfo> getList() {
        return aggroList.values();
    }

    /**
     * @return total damage
     */
    public int getTotalDamage() {
        int totalDamage = 0;
        for (AggroInfo ai : aggroList.values()) {
            totalDamage += ai.getDamage();
        }
        return totalDamage;
    }

    /**
     * Used to get a list of AggroInfo with player/group/alliance damages
     * combined. - Includes only AggroInfo with PlayerAlliance, PlayerGroup, and
     * Player objects.
     *
     * @return finalDamageList including players/groups/alliances
     */
    public Collection<AggroInfo> getFinalDamageList(boolean mergeGroupDamage) {
        Map<Integer, AggroInfo> list = new HashMap<>();

        for (AggroInfo ai : aggroList.values()) {
            Player player = null;
            if (!(ai.getAttacker() instanceof Player)) {

                // Check to see if this is a summon, if so add the damage to the group.
                Creature master = ((Creature) ai.getAttacker()).getMaster();

                if (master instanceof Player) {
                    player = (Player) master;
                }
            } else {
                player = (Player) ai.getAttacker();
            }

            // Don't include damage from players outside the known list.
            if (player == null || !owner.getKnownList().knowns(player)) {
                continue;
            }

            if (mergeGroupDamage) {
                AionObject source;

                if (player.isInTeam()) {
                    source = player.getCurrentTeam();
                } else {
                    source = player;
                }

                if (list.containsKey(source.getObjectId())) {
                    list.get(source.getObjectId()).addDamage(ai.getDamage());
                } else {
                    AggroInfo aggro = new AggroInfo(source);
                    aggro.setDamage(ai.getDamage());
                    list.put(source.getObjectId(), aggro);
                }
            } else if (list.containsKey(player.getObjectId())) {
                // Summon or other assistance
                list.get(player.getObjectId()).addDamage(ai.getDamage());
            } else {
                // Create a separate object so we don't taint current list.
                AggroInfo aggro = new AggroInfo(player);
                aggro.addDamage(ai.getDamage());
                list.put(player.getObjectId(), aggro);
            }
        }

        return list.values();
    }

    protected boolean isAware(Creature creature) {
        return creature != null
                && !creature.getObjectId().equals(owner.getObjectId())
                && (creature.isEnemy(owner)
                || DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(owner.getTribe(), creature.getTribe()));
    }

    public static abstract class AddDamageValueCallback implements TypedCallback<Tuple3<AggroList, Creature, Integer>, Object> {

        // redirector to object notifier
        static {
            EventNotifier.GLOBAL.attach(new AddDamageValueCallback() {
                @Override
                public void onDamageAdded(AggroList aggroList, Creature creature, int hate) {
                    aggroList.owner.getNotifier().fire(AddDamageValueCallback.class, Tuple3.of(aggroList, creature, hate));
                }
            });
        }

        @Override
        public Object onEvent(@NotNull Tuple3<AggroList, Creature, Integer> e) {
            if (e._1.isAware(e._2)) {
                onDamageAdded(e._1, e._2, e._3);
            }
            return null;
        }

        @NotNull
        @Override
        public String getType() {
            return AddDamageValueCallback.class.getName();
        }

        public abstract void onDamageAdded(AggroList aggroList, Creature creature, int hate);
    }
}
