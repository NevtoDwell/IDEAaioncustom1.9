/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.services.item.ItemPacketService.ItemDeleteType;

/**
 * @author Avol
 */
public class CM_DELETE_ITEM extends AionClientPacket {

    public int itemObjectId;

    @Override
    protected void readImpl() {
        itemObjectId = readD();
    }

    @Override
    protected void runImpl() {

        Player player = getConnection().getActivePlayer();
        Storage inventory = player.getInventory();
        Item item = inventory.getItemByObjId(itemObjectId);

        if (item != null) {
            if (!item.getItemTemplate().isBreakable()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_UNBREAKABLE_ITEM(DescId.of(item.getNameID())));
            } else {
                inventory.delete(item, ItemDeleteType.DISCARD);
                checkQuestItems(player, item);
            }
        }
    }
    
    private void checkQuestItems(Player player, Item item) {
        if(item.getItemId() == 170420021) {
        	QuestState qs = player.getQuestStateList().getQuestState(18830);
        	if(qs != null) {
        		qs.setQuestVar(0);
                qs.setCompleteCount(0);
                qs.setStatus(null);
                if (qs.getPersistentState() != PersistentState.NEW) {
                    qs.setPersistentState(PersistentState.DELETED);
                }
                player.sendMsg("Квест \"Совет помощника\" был удален т.к. у вас отсутствуют необходимые предметы. Необходимо перезайти в игру и взять квест заново.");	
        	}
        }
        else if(item.getItemId() == 170425021) {
        	QuestState qs = player.getQuestStateList().getQuestState(28830);
        	if(qs != null) {
        		qs.setQuestVar(0);
                qs.setCompleteCount(0);
                qs.setStatus(null);
                if (qs.getPersistentState() != PersistentState.NEW) {
                    qs.setPersistentState(PersistentState.DELETED);
                }
                player.sendMsg("Квест \"Совет помощника\" был удален т.к. у вас отсутствуют необходимые предметы. Необходимо перезайти в игру и взять квест заново.");	
        	}
        }
    }
}
