/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events;

import com.ne.gs.eventNewEngine.events.enums.EventType;
import com.ne.gs.eventNewEngine.events.xml.EventRankTemplate;
import com.ne.gs.eventNewEngine.events.xml.EventRewardItem;
import com.ne.gs.eventNewEngine.events.xml.EventRewardItemGroup;
import com.ne.gs.eventNewEngine.events.xml.EventRewardTemplate;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.item.ItemService;

/**
 *
 * @author userd
 */
public class EventRewardHelper {

    public static void GiveRewardFor(Player player, EventType etype, EventScore score, int rank) {
        EventRewardTemplate rt = etype.getEventTemplate().getRewardInfo();
        if (rt == null) {
            return;
        }
        EventRankTemplate rw = rt.getRewardByRank(rank);
        if (rw == null) {
            // no rewatd in template for this rank
            return;
        }
        if (rw.getAp() > 0) { // abyss point reward
            AbyssPointsService.addAp(player, rw.getAp());
        }
        if (rw.getGamePoint() > 0) { // toll point reward
            InGameShopEn.getInstance().addToll(player, rw.getGamePoint());
        }
        for (EventRewardItemGroup gr : rw.getRewards()) { // items reward
            for (EventRewardItem item : gr.getItems()) {
                ItemService.addItem(player, item.getItemId(), item.getCount());
            }
        }
    }
}
