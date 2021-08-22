/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.services.SkillLearnService;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillLearnAction")
public class SkillLearnAction extends AbstractItemAction {

    @XmlAttribute
    protected int skillid;
    @XmlAttribute
    protected int level;
    @XmlAttribute(name = "class")
    protected PlayerClass playerClass;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        // 1. check player level
        if (player.getCommonData().getLevel() < level) {
            return false;
        }

        PlayerClass pc = player.getCommonData().getPlayerClass();
        if (!validateClass(pc)) {
            return false;
        }

        // 4. check player race and Race.PC_ALL
        Race race = parentItem.getItemTemplate().getRace();
        if (player.getRace() != race && race != Race.PC_ALL) {
            return false;
        }
        // 5. check whether this skill is already learned
        if (player.getSkillList().isSkillPresent(skillid)) {
            return false;
        }

        return true;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        // item animation and message
        ItemTemplate itemTemplate = parentItem.getItemTemplate();
        // PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.USE_ITEM(itemTemplate.getDescription()));
        player.getController().cancelUseItem();
        PacketSendUtility.broadcastPacket(player,
            new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), itemTemplate.getTemplateId()), true);

        // add skill
        SkillLearnService.learnSkillBook(player, skillid);

        // remove book from inventory (assuming its not stackable)
        Item item = player.getInventory().getItemByObjId(parentItem.getObjectId());
        player.getInventory().delete(item);
    }

    private boolean validateClass(PlayerClass pc) {
        boolean result = false;
        // 2. check if current class is second class and book is for starting class
        if (!pc.isStartingClass() && PlayerClass.getStartingClassFor(pc).ordinal() == playerClass.ordinal()) {
            result = true;
        }
        // 3. check player class and SkillClass.ALL
        if (pc == playerClass || playerClass == PlayerClass.ALL) {
            result = true;
        }

        return result;
    }
}
