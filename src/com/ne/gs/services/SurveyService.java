/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.gs.cache.HTMLCache;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.database.dao.SurveyControllerDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.ItemId;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.survey.SurveyItem;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.world.World;

/**
 * @author KID
 */
public class SurveyService {

    private static final Logger log = LoggerFactory.getLogger(SurveyService.class);
    private final FastMap<Integer, SurveyItem> activeItems;
    private final String htmlTemplate;

    private static final ScheduledExecutorService _surveyExecutor = Executors.newSingleThreadScheduledExecutor();

    public boolean isActive(Player player, int survId) {
        boolean avail = activeItems.containsKey(survId);
        if (avail) {
            requestSurvey(player, survId);
        }

        return avail;
    }

    public SurveyService() {
        activeItems = new FastMap<Integer, SurveyItem>().shared();
        htmlTemplate = HTMLCache.getInstance().getHTML("surveyTemplate.xhtml");
        _surveyExecutor.scheduleAtFixedRate(new TaskUpdate(), 2000, SecurityConfig.SURVEY_DELAY * 60000, TimeUnit.MILLISECONDS);
    }

    public void requestSurvey(Player player, int survId) {
        if (player.getInventory().isFull()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY);
            log.warn("[SurveyController] player " + player.getName() + " tried to receive item with full inventory.");
            return;
        }

        SurveyItem item = activeItems.get(survId);
        if (item == null) {
            // There is no survey underway.
            player.sendPck(new SM_SYSTEM_MESSAGE(1300684));
            return;
        }

        if (item.ownerId != player.getObjectId()) {
            // There is no remaining survey to take part in.
            player.sendPck(new SM_SYSTEM_MESSAGE(1300037));
            return;
        }

        if (GDB.get(SurveyControllerDAO.class).useItem(item.uniqueId)) {
            activeItems.remove(survId); // important to call here in order to remove regardles any exceptions
            ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(item.itemId);
            ItemService.addItem(player, item.itemId, item.count);
            if (item.itemId == ItemId.KINAH.value()) {
                player.sendPck(new SM_SYSTEM_MESSAGE(1300945, item.count));
            } else if (item.count == 1) {
                player.sendPck(new SM_SYSTEM_MESSAGE(1300945, DescId.of(template.getNameId())));
            } else {
                // You received %num1 %0 items as reward for the survey.
                player.sendPck(new SM_SYSTEM_MESSAGE(1300946, item.count, DescId.of(template.getNameId())));
            }
        }
    }

    public void taskUpdate() {
        List<SurveyItem> newList = GDB.get(SurveyControllerDAO.class).getAllNew();
        if (newList.size() == 0) {
            return;
        }

        Set<Integer> players = new HashSet<>();
        int cnt = 0;
        for (SurveyItem item : newList) {
            activeItems.put(item.uniqueId, item);
            cnt++;
            players.add(item.ownerId);
        }
        log.info("[SurveyController] found new " + cnt + " items for " + players.size() + " players.");
        for (int ownerId : players) {
            Player player = World.getInstance().findPlayer(ownerId);
            if (player != null) {
                showAvailable(player);
            }
        }
    }

    public void showAvailable(Player player) {
        for (SurveyItem item : activeItems.values()) {
            if (item.ownerId != player.getObjectId()) {
                continue;
            }

            String context = htmlTemplate;
            context = context.replace("%itemid%", item.itemId + "");
            context = context.replace("%itemcount%", item.count + "");
            context = context.replace("%html%", item.html);
            context = context.replace("%radio%", item.radio);

            HTMLService.sendData(player, item.uniqueId, context);
        }
    }

    public class TaskUpdate implements Runnable {

        @Override
        public void run() {
            log.info("[SurveyController] update task start.");
            taskUpdate();
        }
    }

    private static final class SingletonHolder {

        protected static final SurveyService instance = new SurveyService();
    }

    public static SurveyService getInstance() {
        return SingletonHolder.instance;
    }
}
