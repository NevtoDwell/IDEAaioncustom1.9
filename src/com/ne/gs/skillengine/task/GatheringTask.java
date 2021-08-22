/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.task;

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.GatherableController;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Gatherable;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.gather.GatherableTemplate;
import com.ne.gs.model.templates.gather.Material;
import com.ne.gs.network.aion.serverpackets.SM_GATHER_STATUS;
import com.ne.gs.network.aion.serverpackets.SM_GATHER_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.item.ItemService.ItemAddPredicate;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class GatheringTask extends AbstractCraftTask {

    private final GatherableTemplate template;
    private final Material material;

    private static final GatherItemAddPredicate ITEM_ADD_PREDICATE = new GatherItemAddPredicate();

    private static class GatherItemAddPredicate extends ItemAddPredicate {

        @Override
        public ItemUpdateType getUpdateType(Item input) {
            return ItemUpdateType.INC_GATHER;
        }
    }

    public GatheringTask(Player requestor, Gatherable gatherable, Material material, int skillLvlDiff) {
        super(requestor, gatherable, skillLvlDiff);
        this.template = gatherable.getObjectTemplate();
        this.material = material;
    }

    @Override
    protected void analyzeInteraction() {
        if (material instanceof GatherableController.FakeMaterial) {
            currentFailureValue += Rnd.get(completeValue / 15 / 2, completeValue);
            currentSuccessValue = Rnd.get(currentSuccessValue, Math.max(currentFailureValue - Rnd.get(5, 25), currentSuccessValue + 1));

            if (currentFailureValue >= completeValue) {
                currentFailureValue = completeValue;
            }
        } else {
            super.analyzeInteraction();
        }
    }

    @Override
    protected void onInteractionAbort() {
        requestor.sendPck(new SM_GATHER_UPDATE(template, material, 0, 0, 5));
        // TODO this packet is incorrect cause i need to find emotion of aborted gathering
        PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 2));
		((Gatherable) responder).getController().completeInteraction();
    }

    @Override
    protected void onInteractionFinish() {
        ((Gatherable) responder).getController().completeInteraction();
    }

    @Override
    protected void onInteractionStart() {
        requestor.sendPck(new SM_GATHER_UPDATE(template, material, 0, 0, 0));
        PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 0), true);
        PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 1), true);
    }

    @Override
    protected void sendInteractionUpdate() {
        requestor.sendPck(new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 1));
    }

    @Override
    protected void onFailureFinish() {
        requestor.sendPck(new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 1));
        requestor.sendPck(new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 7));
        PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 3), true);
    }

    @Override
    protected boolean onSuccessFinish() {
        requestor.sendPck(new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 2));
        requestor.sendPck(new SM_GATHER_UPDATE(template, material, currentSuccessValue, currentFailureValue, 6));
        PacketSendUtility.broadcastPacket(requestor, new SM_GATHER_STATUS(requestor.getObjectId(), responder.getObjectId(), 2), true);
        requestor.sendPck(SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_SUCCESS_1_BASIC(DescId.of(material.getNameid())));
        requestor.getInventory().decreaseByItemId(template.getRequiredItemId(), 1);
        ItemService.addItem(requestor, material.getItemid(), requestor.getRates()
                                                                      .getGatheringCountRate(), ITEM_ADD_PREDICATE);
        if (requestor.isInInstance()) {
            requestor.getPosition()
                     .getWorldMapInstance()
                     .getInstanceHandler()
                     .onGather(requestor, (Gatherable) responder);
        }
        ((Gatherable) responder).getController().rewardPlayer(requestor);
        return true;
    }
}
