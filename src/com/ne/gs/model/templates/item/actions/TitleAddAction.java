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
 * @author Hilgert
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TitleAddAction")
public class TitleAddAction extends AbstractItemAction {

    @XmlAttribute
    protected int titleid;
    @XmlAttribute
    protected Integer minutes;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.itemengine.actions.AbstractItemAction#canAct(com.ne.gs.model.gameobjects.player
     * .Player, com.ne.gs.model.gameobjects.Item, com.ne.gs.model.gameobjects.Item)
     */
    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        if (titleid == 0 || parentItem == null) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR);
            return false;
        }
        if (player.getTitleList().contains(titleid)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_TOOLTIP_LEARNED_TITLE);
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.itemengine.actions.AbstractItemAction#act(com.ne.gs.model.gameobjects.player
     * .Player, com.ne.gs.model.gameobjects.Item, com.ne.gs.model.gameobjects.Item)
     */
    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        ItemTemplate itemTemplate = parentItem.getItemTemplate();
        player.sendPck(SM_SYSTEM_MESSAGE.STR_USE_ITEM(DescId.of(itemTemplate.getNameId())));
        PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), itemTemplate.getTemplateId()), true);

        if (player.getTitleList().addTitle(titleid, false, minutes == null ? 0 : ((int) (System.currentTimeMillis() / 1000)) + minutes * 60)) {
            Item item = player.getInventory().getItemByObjId(parentItem.getObjectId());
            player.getInventory().delete(item);
        }
    }
}
