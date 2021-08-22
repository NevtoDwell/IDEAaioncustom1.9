/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.drop;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.drop.DropItem;
import com.ne.gs.model.gameobjects.DropNpc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_LOOT;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author xTz
 */
@Deprecated
public class DropDistributionService {

    private static final Logger log = LoggerFactory.getLogger(DropDistributionService.class);

    public static DropDistributionService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Called from CM_GROUP_LOOT to handle rolls
     */
    public void handleRoll(Player player, int roll, int itemId, int npcId, int index) {
        DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
        if (player == null || dropNpc == null) {
            return;
        }
        int luck = 0;
        if (player.isInGroup2() || player.isInAlliance2()) {
            if (roll == 0) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_ME);
            } else {
                luck = Rnd.get(1, 100);
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_ME(luck, 100));
            }
            for (Player member : dropNpc.getInRangePlayers()) {
                if (member == null) {
                    log.warn("member null Owner is in group? " + player
                        .isInGroup2() + " Owner is in Alliance? " + player.isInAlliance2());
                    continue;
                }

                int teamId = member.getCurrentTeamId();
                member.sendPck(new SM_GROUP_LOOT(teamId, member.getObjectId(), itemId, npcId, dropNpc
                    .getDistributionId(), luck, index));
                if (!player.equals(member) && member.isOnline()) {
                    if (roll == 0) {
                        member.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_OTHER(player.getName()));
                    } else {
                        member.sendPck(SM_SYSTEM_MESSAGE
                            .STR_MSG_DICE_RESULT_OTHER(player.getName(), luck, 100));
                    }
                }
            }
            distributeLoot(player, luck, itemId, npcId);
        }
    }

    /**
     * Called from CM_GROUP_LOOT to handle bids
     */
    public void handleBid(Player player, long bid, int itemId, int npcId, int index) {
        DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
        if (player == null || dropNpc == null) {
            return;
        }

        if (player.isInGroup2() || player.isInAlliance2()) {
            if ((bid > 0 && player.getInventory().getKinah() < bid) || bid < 0) {
                bid = 0; // Set BID to 0 if player has bid more KINAH then they have in inventory or send negative value
            }

            if (bid > 0) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_PAY_RESULT_ME);
            } else {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_PAY_GIVEUP_ME);
            }

            for (Player member : dropNpc.getInRangePlayers()) {
                if (member == null) {
                    log.warn("member null Owner is in group? " + player
                        .isInGroup2() + " Owner is in Alliance? " + player.isInAlliance2());
                    continue;
                }

                int teamId = member.getCurrentTeamId();
                member.sendPck(new SM_GROUP_LOOT(teamId, member.getObjectId(), itemId, npcId, dropNpc
                    .getDistributionId(), bid, index));
                if (!player.equals(member) && member.isOnline()) {
                    if (bid > 0) {
                        member.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_PAY_RESULT_OTHER(player.getName()));
                    } else {
                        member.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_PAY_GIVEUP_OTHER(player.getName()));
                    }
                }
            }
            distributeLoot(player, bid, itemId, npcId);
        }
    }

    /**
     * Checks all players have Rolled or Bid then Distributes items accordingly
     */
    private void distributeLoot(Player player, long luckyPlayer, int itemId, int npcId) {
        DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
        Set<DropItem> dropItems = DropRegistrationService.getInstance().geCurrentDropMap().get(npcId);
        DropItem requestedItem = null;

        if (dropItems == null) {
            return;
        }

        synchronized (dropItems) {
            for (DropItem dropItem : dropItems) {
                if (dropItem.getIndex() == dropNpc.getCurrentIndex()) {
                    requestedItem = dropItem;
                    break;
                }
            }
        }

        if (requestedItem == null) {
            return;
        }
        player.unsetPlayerMode(PlayerMode.IN_ROLL);

        // Removes player from ARRAY once they have rolled or bid
        if (dropNpc.containsPlayerStatus(player)) {
            dropNpc.delPlayerStatus(player);
        }

        if (luckyPlayer > requestedItem.getHighestValue()) {
            requestedItem.setHighestValue(luckyPlayer);
            requestedItem.setWinningPlayer(player);
        }

        if (!dropNpc.getPlayerStatus().isEmpty()) {
            return;
        }

        if (player.isInGroup2() || player.isInAlliance2()) {
            for (Player member : dropNpc.getInRangePlayers()) {
                if (member == null) {
                    continue;
                }
                if (requestedItem.getWinningPlayer() == null) {
                    member.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_PAY_ALL_GIVEUP);
                }
                int teamId = member.getCurrentTeamId();
                AionServerPacket packet = new SM_GROUP_LOOT(teamId, requestedItem
                    .getWinningPlayer() != null ? requestedItem.getWinningPlayer()
                    .getObjectId() : 1, itemId, npcId, dropNpc.getDistributionId(), 0xFFFFFFFF, requestedItem
                    .getIndex());
                member.sendPck(packet);
            }
        }

        LootGroupRules lgr = player.getLootGroupRules();
        if (lgr != null) {
            lgr.removeItemToBeDistributed(requestedItem);
        }

        // Check if there is a Winning Player registered if not all members must have passed...
        if (requestedItem.getWinningPlayer() == null) {
            requestedItem.isFreeForAll(true);
            if (lgr != null && !lgr.getItemsToBeDistributed().isEmpty()) {
                DropService.getInstance().canDistribute(player, lgr.getItemsToBeDistributed().getFirst());
            }
            return;
        }

        requestedItem.isDistributeItem(true);
        DropService.getInstance().requestDropItem(player, npcId, dropNpc.getCurrentIndex());
        if (lgr != null && !lgr.getItemsToBeDistributed().isEmpty()) {
            DropService.getInstance().canDistribute(player, lgr.getItemsToBeDistributed().getFirst());
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final DropDistributionService instance = new DropDistributionService();
    }

}
