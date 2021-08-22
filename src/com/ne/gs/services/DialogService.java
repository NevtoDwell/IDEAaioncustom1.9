/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.TeleportAnimation;
import com.ne.gs.model.autogroup.AutoGroupsType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.model.team.legion.LegionWarehouse;
import com.ne.gs.model.templates.portal.PortalPath;
import com.ne.gs.model.templates.teleport.TeleportLocation;
import com.ne.gs.model.templates.teleport.TeleporterTemplate;
import com.ne.gs.model.templates.tradelist.TradeListTemplate;
import com.ne.gs.network.aion.serverpackets.*;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.services.craft.CraftSkillUpdateService;
import com.ne.gs.services.craft.RelinquishCraftStatus;
import com.ne.gs.services.item.ItemChargeService;
import com.ne.gs.services.teleport.PortalService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.services.trade.PricesService;
import com.ne.gs.skillengine.model.SkillTargetSlot;

/**
 * @author VladimirZ
 */
public final class DialogService {

    private static final Logger log = LoggerFactory.getLogger(DialogService.class);

    public static void onCloseDialog(Npc npc, Player player) {
        switch (npc.getObjectTemplate().getTitleId()) {
            case 350409:
                player.sendPck(new SM_DIALOG_WINDOW(npc.getObjectId(), 0));
                Legion legion = player.getLegion();
                if (legion != null) {
                    LegionWarehouse lwh = player.getLegion().getLegionWarehouse();
                    if (lwh.getWhUser() == player.getObjectId()) {
                        lwh.setWhUser(0);
                    }
                }
                break;
        }
    }

    public static void onDialogSelect(int dialogId, final Player player, final Npc npc, int questId, int extendedRewardIndex) {

        QuestEnv env = new QuestEnv(npc, player, questId, dialogId);
        env.setExtendedRewardIndex(extendedRewardIndex);
        if (QuestEngine.getInstance().onDialog(env)) {
            return;
        }

        if (player.getAccessLevel() >= 3 && CustomConfig.ENABLE_SHOW_DIALOGID) {
            player.sendMsg("dialogId: " + dialogId);
            player.sendMsg("questId: " + questId);
        }

        int targetObjectId = npc.getObjectId();

        switch (dialogId) {
            case 2: {
                TradeListTemplate tradeListTemplate = DataManager.TRADE_LIST_DATA.getTradeListTemplate(npc.getNpcId());
                if (tradeListTemplate == null) {
                    player.sendMsg("Buy list is missing!!");
                    break;
                }
                int tradeModifier = tradeListTemplate.getSellPriceRate();
                player.sendPck(new SM_TRADELIST(player, npc, tradeListTemplate, PricesService.getVendorBuyModifier() * tradeModifier
                    / 100));
                break;
            }
            case 3: {
                player.sendPck(new SM_SELL_ITEM(targetObjectId, PricesService.getVendorSellModifier(player.getRace())));
                break;
            }
            case 4: { // stigma
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1));
                break;
            }
            case 5: { // create legion
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 2));
                break;
            }
            case 6: { // disband legion
                LegionService.getInstance().requestDisbandLegion(npc, player);
                break;
            }
            case 7: { // recreate legion
                LegionService.getInstance().recreateLegion(npc, player);
                break;
            }
            case 21: { // warehouse (2.5)
                if (!RestrictionsManager.canUseWarehouse(player)) {
                    return;
                }
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 26));
                WarehouseService.sendWarehouseInfo(player, true);
                break;
            }
            case 26: {
                if (questId != 0) {
                    QuestState qs = player.getQuestStateList().getQuestState(questId);
                    if (qs != null) {
                        if (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD) {
                            if (AdminConfig.QUEST_DIALOG_LOG) {
                                log.info("Error in the quest " + questId + ". No response from " + npc.getNpcId() + " on the step " + qs.getQuestVarById(0));
                            }
                            if (!"useitem".equals(npc.getAi2().getName())) {
                                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 10));
                            } else {
                                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 0));
                            }
                        }
                    } else {
                        if (AdminConfig.QUEST_DIALOG_LOG) {
                            log.info("Quest " + questId + " is not implemented.");
                        }
                        player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 10));
                    }
                } else {
                    player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 0));
                }
                break;
            }
            case 28: { // Consign trade?? npc karinerk, koorunerk (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 13));
                break;
            }
            case 30: { // soul healing (2.5)
                final long expLost = player.getCommonData().getExpRecoverable();
                if (expLost == 0) {
                    player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.SPEC2);
                    player.getCommonData().setDeathCount(0);
                }
                double factor = (expLost < 1000000 ? 0.25 - (0.00000015 * expLost) : 0.1);
                final int price = (int) (expLost * factor);

                RequestResponseHandler responseHandler = new RequestResponseHandler(npc) {

                    @Override
                    public void acceptRequest(Creature requester, Player responder) {
                        if (player.getInventory().getKinah() >= price) {
                            player.sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP2(expLost));
                            player.sendPck(SM_SYSTEM_MESSAGE.STR_SUCCESS_RECOVER_EXPERIENCE);
                            player.getCommonData().resetRecoverableExp();
                            player.getInventory().decreaseKinah(price);
                            player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.SPEC2);
                            player.getCommonData().setDeathCount(0);
                        } else {
                            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_KINA(price));
                        }
                    }

                    @Override
                    public void denyRequest(Creature requester, Player responder) {
                        // no message
                    }
                };
                if (player.getCommonData().getExpRecoverable() > 0) {
                    boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_RECOVER_EXPERIENCE, responseHandler);
                    if (result) {
                        player.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_RECOVER_EXPERIENCE, 0, 0, String.valueOf(price)));
                    }
                } else {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_DONOT_HAVE_RECOVER_EXPERIENCE);
                }
                break;
            }
            case 31: { // (2.5)
                switch (npc.getNpcId()) {
                    case 204089: // pvp arena in pandaemonium.
                        TeleportService.teleportTo(player, 120010000, 1, 983.925f, 1550.646f, 221.99f, 45);
                        break;
                    case 203764: // pvp arena in sanctum.
                        TeleportService.teleportTo(player, 110010000, 1, 1462.4106f, 1326.277f, 564.14f, 82);
                        break;
                    case 203981: {
                        if (player.getRace() == Race.ELYOS) {
                            QuestState qs = player.getQuestStateList().getQuestState(1346);
                            if (qs == null || qs.getStatus() != QuestStatus.COMPLETE) {
                                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 27));
                            } else {
                                TeleportService.teleportTo(player, 210020000, 1, 436.97f, 421.161f, 274.37f, 74, TeleportAnimation.BEAM_ANIMATION);
                            }
                        }
                        break;
                    }
                }
                break;
            }
            case 32: { // (2.5)
                switch (npc.getNpcId()) {
                    case 204087:
                        TeleportService.teleportTo(player, 120010000, 1, 1006.135f, 1528.68f, 222.19f, 105);
                        break;
                    case 203875:
                        TeleportService.teleportTo(player, 110010000, 1, 1472.103f, 1346.85f, 563.987f, 22);
                        break;
                    case 203982:
                        TeleportService.teleportTo(player, 210020000, 1, 446.2f, 431.1f, 274.5f, 14, TeleportAnimation.BEAM_ANIMATION);
                        break;
                }
                break;
            }
            case 36: { // Godstone socketing (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 21));
                break;
            }
            case 37: { // remove mana stone (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 20));
                break;
            }
            case 38: { // modify appearance (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 19));
                break;
            }
            case 39: { // flight and teleport (2.5)
                if (CustomConfig.ENABLE_SIMPLE_2NDCLASS) {
                    int level = player.getLevel();
                    if (level < 9) {
                        player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 27));
                    } else {
                        TeleportService.showMap(player, targetObjectId, npc.getNpcId());
                    }
                } else {
                    switch (npc.getNpcId()) {
                        case 203194: {
                            if (player.getRace() == Race.ELYOS) {
                                QuestState qs = player.getQuestStateList().getQuestState(1006);
                                if (qs == null || qs.getStatus() != QuestStatus.COMPLETE) {
                                    player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 27));
                                } else {
                                    TeleportService.showMap(player, targetObjectId, npc.getNpcId());
                                }
                            }
                            break;
                        }
                        case 203679: {
                            if (player.getRace() == Race.ASMODIANS) {
                                QuestState qs = player.getQuestStateList().getQuestState(2008);
                                if (qs == null || qs.getStatus() != QuestStatus.COMPLETE) {
                                    player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 27));
                                } else {
                                    TeleportService.showMap(player, targetObjectId, npc.getNpcId());
                                }
                            }
                            break;
                        }
                        default: {
                            TeleportService.showMap(player, targetObjectId, npc.getNpcId());
                        }
                    }
                }
                break;
            }
            case 40: // improve extraction (2.5)
            case 41: { // learn tailoring armor smithing etc. (2.5)
                CraftSkillUpdateService.getInstance().learnSkill(player, npc);
                break;
            }
            case 42: { // expand cube (2.5)
                CubeExpandService.expandCube(player, npc);
                break;
            }
            case 43: { // (2.5)
                WarehouseService.expandWarehouse(player, npc);
                break;
            }
            case 48: { // legion warehouse (2.5)
                if (npc.getObjectTemplate().getTitleId() == 350409) {
                    LegionService.getInstance().openLegionWarehouse(player, npc);
                }
                break;
            }
            case 51: { // WTF??? Quest dialog packet (2.5)
                break;
            }
            case 53: { // (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 28));
                break;
            }
            case 54: { // coin reward (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 3, 0));
                // PacketSendUtility.sendPacket(player, new SM_MESSAGE(0, null, "This feature is not available yet",
                // ChatType.GOLDEN_YELLOW));
                break;
            }
            case 56:
            case 57: { // (2.5)
                byte changesex = 0; // 0 plastic surgery, 1 gender switch
                byte check_ticket = 2; // 2 no ticket, 1 have ticket
                if (dialogId == 57) {
                    // Gender Switch
                    changesex = 1;
                    if (player.getInventory().getItemCountByItemId(169660000) > 0 || player.getInventory().getItemCountByItemId(169660001) > 0) {
                        check_ticket = 1;
                    }
                } else // Plastic Surgery
                    if (player.getInventory().getItemCountByItemId(169650000) > 0 || player.getInventory().getItemCountByItemId(169650001) > 0) {
                        check_ticket = 1;
                    }
                player.sendPck(new SM_PLASTIC_SURGERY(player, check_ticket, changesex));
                player.setEditMode(true);
                break;
            }
            case 58: // dredgion
                // FIXME
//                if (DredgionService2.getInstance().isDredgionAvialable()) {
//                    AutoGroupsType agt = AutoGroupsType.getAutoGroup(npc.getNpcId());
//                    if (agt != null) {
//                        player.sendPck(new SM_AUTO_GROUP(agt.getInstanceMaskId()));
//                    } else {
//                        player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 0));
//                    }
//                } else {
//                    player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1011));
//                }
                break;
            case 60: { // (2.5)
                break;
            }
            case 61: { // armsfusion (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 29));
                break;
            }
            case 62: { // armsbreaking (2.5)
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 30));
                break;
            }
            case 63: { // join npcFaction (2.5)
                player.getNpcFactions().enterGuild(npc);
                break;
            }
            case 64: { // leave npcFaction (2.5)
                player.getNpcFactions().leaveNpcFaction(npc);
                break;
            }
            case 65: { // repurchase (2.5)
                player.sendPck(new SM_REPURCHASE(player, npc.getObjectId()));
                break;
            }
            case 66: { // adopt pet (2.5)
                player.sendPck(new SM_PET(6));
                break;
            }
            case 67: { // surrender pet (2.5)
                player.sendPck(new SM_PET(7));
                break;
            }
            case 68: { // housing build
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 32));
                break;
            }
            case 69: { // housing destruct
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 33));
                break;
            }
            case 70: { // condition an individual item
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 35));
                break;
            }
            case 71: { // condition all equiped items
                ItemChargeService.startChargingEquippedItems(player, targetObjectId, 1);
                break;
            }
            case 73: {
                TradeListTemplate tradeListTemplate = DataManager.TRADE_LIST_DATA.getTradeInListTemplate(npc.getNpcId());
                if (tradeListTemplate == null) {
                    player.sendMsg("Buy list is missing!!");
                    break;
                }
                player.sendPck(new SM_TRADE_IN_LIST(npc, tradeListTemplate, 100));
                break;
            }
            case 74: {
                RelinquishCraftStatus.getInstance();
                RelinquishCraftStatus.relinquishExpertStatus(player, npc);
                break;
            }
            case 75: {
                RelinquishCraftStatus.getInstance();
                RelinquishCraftStatus.relinquishMasterStatus(player, npc);
                break;
            }
            case 79: {
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 38));
                break;
            }
            case 87: { // adopt pet (2.5)
                player.sendPck(new SM_PET(16));
                break;
            }
            case 88: { // surrender pet (2.5)
                player.sendPck(new SM_PET(17));
                break;
            }
            case 89: {
                player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 42));
                break;
            }
            case 90: {
                ItemChargeService.startChargingEquippedItems(player, targetObjectId, 2);
                break;
            }
            case 91: {
                // TODO
                //HousingService.getInstance().recreatePlayerStudio(player);
                break;
            }
            case 10000:
            case 10001:
            case 10002: {
                if (questId == 0) { // generic npc reply (most are teleporters)
                    TeleporterTemplate template = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npc.getNpcId());
                    PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalDialog(npc.getNpcId(), dialogId, player.getRace());
                    if (portalPath != null) {
                        PortalService.port(portalPath, player, targetObjectId);
                    } else if (template != null) {
                        TeleportLocation loc = template.getTeleLocIdData().getTelelocations().get(0);
                        if (loc != null) {
                            TeleportService.teleport(template, loc.getLocId(), player, npc,
                                npc.getAi2().getName().equals("general") ? TeleportAnimation.JUMP_AIMATION : TeleportAnimation.BEAM_ANIMATION);
                        }
                    }
                } else {
                    player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, dialogId, questId));
                }
                break;
            }
            default: {
                if (questId > 0) {
                    if (dialogId == 18 && player.getInventory().isFull()) {
                        player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 0));
                    } else {
                        player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, dialogId, questId));
                    }
                } else {
                    player.sendPck(new SM_DIALOG_WINDOW(targetObjectId, dialogId));
                }
                break;
            }
        }
    }
}
