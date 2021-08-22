/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items;

import com.ne.gs.configs.main.PvPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.GodstoneInfo;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
public class GodStone extends ItemStone {

    private static final Logger log = LoggerFactory.getLogger(GodStone.class);

    private final GodstoneInfo godstoneInfo;
    private ActionObserver actionListener;
    private final int probability;
    private final int probabilityLeft;
    private final ItemTemplate godItem;

    public GodStone(int itemObjId, int itemId, PersistentState persistentState) {
        super(itemObjId, itemId, 0, persistentState);
        ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
        godItem = itemTemplate;
        godstoneInfo = itemTemplate.getGodstoneInfo();

        if (godstoneInfo != null) {
            probability = godstoneInfo.getProbability();
            probabilityLeft = godstoneInfo.getProbabilityleft();
        } else {
            probability = 0;
            probabilityLeft = 0;
            log.warn("CHECKPOINT: Godstone info missing for item : " + itemId);
        }

    }

    /**
     * @param player
     */
    public void onEquip(final Player player) {
        if (godstoneInfo == null || godItem == null) {
            return;
        }

        Item equippedItem = player.getEquipment().getEquippedItemByObjId(getItemObjId());
        final int godStoneChance = PvPConfig.GODSTONE_CHANCE;
        int equipmentSlot = equippedItem.getEquipmentSlot();
        final int handProbability = equipmentSlot == ItemSlot.MAIN_HAND.id() ? probability : probabilityLeft;
        actionListener = new ActionObserver(ObserverType.ATTACK) {

            @Override
            public void attack(Creature creature) {
                if (handProbability > Rnd.get(0, godStoneChance)) {
                    Skill skill = SkillEngine.getInstance().getSkill(player, godstoneInfo.getSkillid(), godstoneInfo.getSkilllvl(), player.getTarget(),
                        godItem);
                    skill.setFirstTargetRangeCheck(false);
                    if (skill.canUseSkill()) {
                        Effect effect = new Effect(player, creature, skill.getSkillTemplate(), 1, 0, godItem);
                        effect.initialize();
                        effect.applyEffect();
                        effect = null;
                    }
                }
            }
        };

        player.getObserveController().addObserver(actionListener);
    }

    /**
     * @param player
     */
    public void onUnEquip(Player player) {
        if (actionListener != null) {
            player.getObserveController().removeObserver(actionListener);
        }

    }
}
