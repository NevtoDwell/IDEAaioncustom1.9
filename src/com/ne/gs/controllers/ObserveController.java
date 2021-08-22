/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import javolution.util.FastList;

import com.ne.gs.controllers.attack.AttackResult;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.controllers.observer.AttackerCriticalStatus;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * Notes:<br>
 * 1) There should be locking against onceUsedObservers<br>
 * 2) Check observers size before iteration to minimize memory allocations
 *
 * @author ATracer
 * @author Cura
 */
public class ObserveController {

    private final ReentrantLock lock = new ReentrantLock();
    protected Collection<ActionObserver> observers = new FastList<ActionObserver>(0).shared();
    protected FastList<ActionObserver> onceUsedObservers = new FastList<>(0);
    protected Collection<AttackCalcObserver> attackCalcObservers = new CopyOnWriteArrayList<>();

    /**
     * Once used observer add to observerController. If observer notify will be removed.
     *
     * @param observer
     */
    public void attach(ActionObserver observer) {
        observer.makeOneTimeUse();
        lock.lock();
        try {
            onceUsedObservers.add(observer);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param observer
     */
    public void addObserver(ActionObserver observer) {
        observers.add(observer);
    }

    /**
     * @param observer
     */
    public void addAttackCalcObserver(AttackCalcObserver observer) {
        attackCalcObservers.add(observer);
    }

    /**
     * @param observer
     */
    public void removeObserver(ActionObserver observer) {
        observers.remove(observer);
        lock.lock();
        try {
            onceUsedObservers.remove(observer);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param observer
     */
    public void removeAttackCalcObserver(AttackCalcObserver observer) {
        attackCalcObservers.remove(observer);
    }

    /**
     * notify all observers
     */
    public void notifyObservers(ObserverType type, Object... object) {
        List<ActionObserver> tempOnceused = Collections.emptyList();
        lock.lock();
        try {
            if (onceUsedObservers.size() > 0) {
                tempOnceused = new ArrayList<>();
                Iterator<ActionObserver> iterator = onceUsedObservers.iterator();
                while (iterator.hasNext()) {
                    ActionObserver observer = iterator.next();
                    if (observer.getObserverType().matchesObserver(type)) {
                        if (observer.tryUse()) {
                            tempOnceused.add(observer);
                            iterator.remove();
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        // notify outside of lock
        for (ActionObserver observer : tempOnceused) {
            notifyAction(type, observer, object);
        }

        if (observers.size() > 0) {
            for (ActionObserver observer : observers) {
                if (observer.getObserverType().matchesObserver(type)) {
                    notifyAction(type, observer, object);
                }
            }
        }
    }

    private void notifyAction(ObserverType type, ActionObserver observer, Object... object) {
        switch (type) {
            case ATTACK:
                observer.attack((Creature) object[0]);
                break;
            case ATTACKED:
                observer.attacked((Creature) object[0]);
                break;
            case DEATH:
                observer.died((Creature) object[0]);
                break;
            case EQUIP:
                observer.equip((Item) object[0], (Player) object[1]);
                break;
            case UNEQUIP:
                observer.unequip((Item) object[0], (Player) object[1]);
                break;
            case MOVE:
                observer.moved();
                break;
            case SKILLUSE:
                observer.skilluse((Skill) object[0]);
                break;
            case DOT_ATTACKED:
                observer.dotattacked((Creature) object[0], (Effect) object[1]);
                break;
            case ITEMUSE:
                observer.itemused((Item) object[0]);
                break;
            case NPCDIALOGREQUEST:
                observer.npcdialogrequested((Npc) object[0]);
                break;
            case ABNORMALSETTED:
                observer.abnormalsetted((AbnormalState) object[0]);
                break;
            case SUMMONRELEASE:
                observer.summonrelease();
                break;
            case LOOT:
                observer.loot();
                break;
            case GATHER:
                observer.gather();
                break;
        }
    }

    /**
     * notify	             that creature died
     */
    public void notifyDeathObservers(Creature creature) {
        notifyObservers(ObserverType.DEATH, creature);
    }

    /**
     * notify that creature moved
     */
    public void notifyMoveObservers() {
        notifyObservers(ObserverType.MOVE);
    }

    /**
     * notify that creature attacking
     */
    public void notifyAttackObservers(Creature creature) {
        notifyObservers(ObserverType.ATTACK, creature);
    }

    /**
     * notify that creature attacked
     */
    public void notifyAttackedObservers(Creature creature) {
        notifyObservers(ObserverType.ATTACKED, creature);
    }

    /**
     * notify that creature attacked by dot's hit
     */
    public void notifyDotAttackedObservers(Creature creature, Effect effect) {
        notifyObservers(ObserverType.DOT_ATTACKED, creature, effect);
    }

    /**
     * notify that creature used a skill
     */
    public void notifySkilluseObservers(Skill skill) {
        notifyObservers(ObserverType.SKILLUSE, skill);
    }

    /**
     * @param item
     * @param owner
     */
    public void notifyItemEquip(Item item, Player owner) {
        notifyObservers(ObserverType.EQUIP, item, owner);
    }

    /**
     * @param item
     * @param owner
     */
    public void notifyItemUnEquip(Item item, Player owner) {
        notifyObservers(ObserverType.UNEQUIP, item, owner);
    }

    /**
     * notify that player used an item
     */
    public void notifyItemuseObservers(Item item) {
        notifyObservers(ObserverType.ITEMUSE, item);
    }

    /**
     * notify that player requested dialog with npc
     */
    public void notifyRequestDialogObservers(Npc npc) {
        notifyObservers(ObserverType.NPCDIALOGREQUEST, npc);
    }

    public void notifyAbnormalSettedObservers(AbnormalState state) {
        notifyObservers(ObserverType.ABNORMALSETTED, state);
    }

    public void notifySummonReleaseObservers() {
        notifyObservers(ObserverType.SUMMONRELEASE);
    }

    /**
     * @param status
     *
     * @return true or false
     */
    public boolean checkAttackStatus(AttackStatus status) {
        if (attackCalcObservers.size() > 0) {
            for (AttackCalcObserver observer : attackCalcObservers) {
                if (observer.checkStatus(status)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param status
     *
     * @return
     */
    public boolean checkAttackerStatus(AttackStatus status) {
        if (attackCalcObservers.size() > 0) {
            for (AttackCalcObserver observer : attackCalcObservers) {
                if (observer.checkAttackerStatus(status)) {
                    return true;
                }
            }
        }
        return false;
    }

    public AttackerCriticalStatus checkAttackerCriticalStatus(AttackStatus status, boolean isSkill) {
        if (attackCalcObservers.size() > 0) {
            for (AttackCalcObserver observer : attackCalcObservers) {
                AttackerCriticalStatus acStatus = observer.checkAttackerCriticalStatus(status, isSkill);
                if (acStatus.isResult()) {
                    return acStatus;
                }
            }
        }
        return new AttackerCriticalStatus(false);
    }

    /**
     * @param attackList
     */
    public void checkShieldStatus(List<AttackResult> attackList, Creature attacker) {
        if (attackCalcObservers.size() > 0) {
            for (AttackCalcObserver observer : attackCalcObservers) {
                observer.checkShield(attackList, attacker);
            }
        }
    }

    public float getBasePhysicalDamageMultiplier(boolean isSkill) {
        float multiplier = 1;
        if (attackCalcObservers.size() > 0) {
            for (AttackCalcObserver observer : attackCalcObservers) {
                multiplier *= observer.getBasePhysicalDamageMultiplier(isSkill);
            }
        }
        return multiplier;
    }

    public float getBaseMagicalDamageMultiplier() {
        float multiplier = 1;
        if (attackCalcObservers.size() > 0) {
            for (AttackCalcObserver observer : attackCalcObservers) {
                multiplier *= observer.getBaseMagicalDamageMultiplier();
            }
        }
        return multiplier;
    }

    /**
     * Clear all observers
     */
    public void clear() {
        lock.lock();
        try {
            onceUsedObservers.clear();
        } finally {
            lock.unlock();
        }
        observers.clear();
        attackCalcObservers.clear();
    }
}
