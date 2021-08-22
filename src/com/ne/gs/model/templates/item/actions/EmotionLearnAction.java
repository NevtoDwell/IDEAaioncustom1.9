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

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EmotionLearnAction")
public class EmotionLearnAction extends AbstractItemAction {

    @XmlAttribute
    protected int emotionid;
    @XmlAttribute
    protected Integer minutes;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        if (emotionid == 0 || parentItem == null) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR);
            return false;
        }
        if (player.getEmotions() != null && player.getEmotions().contains(emotionid)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_TOOLTIP_LEARNED_EMOTION);
            return false;
        }
        return true;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        ItemTemplate itemTemplate = parentItem.getItemTemplate();
        player.sendPck(SM_SYSTEM_MESSAGE.STR_USE_ITEM(DescId.of(itemTemplate.getNameId())));
        PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), itemTemplate.getTemplateId()), true);

        player.getEmotions().add(emotionid, minutes == null ? 0 : (int) (System.currentTimeMillis() / 1000) + minutes * 60, true);
        player.getInventory().delete(parentItem);

    }
}
