/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemCategory;
import com.ne.gs.model.templates.item.actions.EnchantItemAction;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemSocketService;
import com.ne.gs.services.trade.PricesService;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer, Wakizashi
 */
public class CM_MANASTONE extends AionClientPacket {

    private int npcObjId;
    private int slotNum;
    private int actionType;
    private int targetFusedSlot;
    private int stoneUniqueId;
    private int targetItemUniqueId;
    private int supplementUniqueId;
    @SuppressWarnings("unused")
    private ItemCategory actionCategory;

    @Override
    protected void readImpl() {
        actionType = readC();
        targetFusedSlot = readC();
        targetItemUniqueId = readD();
        switch (actionType) {
            case 1:
            case 2:
                stoneUniqueId = readD();
                supplementUniqueId = readD();
                break;
            case 3:
                slotNum = readC();
                readC();
                readH();
                npcObjId = readD();
                break;
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        VisibleObject obj = player.getKnownList().getObject(npcObjId);

        if(player.isInState(com.ne.gs.model.gameobjects.state.CreatureState.RESTING)){
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_USE_ITEM_WHILE_IN_CURRENT_STANCE);
            return;
        }
        
        switch (actionType) {
            case 1: // enchant stone
            case 2: // add manastone
                EnchantItemAction action = new EnchantItemAction();
                Item manastone = player.getInventory().getItemByObjId(stoneUniqueId);
                Item targetItem = player.getEquipment().getEquippedItemByObjId(targetItemUniqueId);
                if (targetItem == null) {
                    targetItem = player.getInventory().getItemByObjId(targetItemUniqueId);
                }
                if (action.canAct(player, manastone, targetItem)) {
                    Item supplement = player.getInventory().getItemByObjId(supplementUniqueId);
                    if (supplement != null) {
                        if (supplement.getItemId() / 100000 != 1661) {
                            return;
                        }
                    }
                    action.act(player, manastone, targetItem, supplement, targetFusedSlot);
                }
                break;
            case 3: // remove manastone
                long price = PricesService.getPriceForService(500, player.getRace());
                if (player.getInventory().getKinah() < price) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
                    return;
                }
                Item item = player.getInventory().getItemByObjId(targetItemUniqueId);
                if (item == null) {
                    player.sendMsg("Невозможно извлечь с используемого предмета.");
                    return;
                }
                if (obj != null && obj instanceof Npc && MathUtil.isInRange(player, obj, 7)) {
                    player.getInventory().decreaseKinah(price);
                    if (targetFusedSlot == 1) {
                        ItemSocketService.removeManastone(player, targetItemUniqueId, slotNum);
                    } else {
                        ItemSocketService.removeFusionstone(player, targetItemUniqueId, slotNum);
                    }
                }
        }
    }

}
