/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.reward;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ne.commons.annotations.NotNull;
import com.ne.gs.database.GDB;
import com.ne.commons.utils.Rnd;
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.configs.main.PvPConfig;
import com.ne.gs.database.dao.RewardServiceDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.rewards.RewardEntryItem;
import com.ne.gs.services.PvpService;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.mail.SystemMailService;
import com.ne.gs.utils.MathUtil;

/**
 * @author KID
 */
public class RewardService {
    public class WorldDropEntry {
        public List<WorldDropItemEntry> entries = new ArrayList<>(0);
        public String cond;
    }

    public class WorldDropItemEntry {
        public int rate;
        public boolean random;
        public List<WorldDropEntryItem> items = new ArrayList<>(0);

        public void pushItem(int itemId, int count, int nameId) {
            this.items.add(new WorldDropEntryItem(itemId, count, nameId));
        }
    }

    public class WorldDropEntryItem {
        public int id, count;
        public DescId nameId;

        public WorldDropEntryItem(int itemId, int count, int nameId) {
            this.id = itemId;
            this.count = count;
            this.nameId = DescId.of(nameId);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);
    private static final RewardService controller = new RewardService();
    private final RewardServiceDAO dao;
    private final Map<Integer, WorldDropEntry> worldDrop;

    public static RewardService getInstance() {
        return controller;
    }

    public RewardService() {
        this.dao = GDB.get(RewardServiceDAO.class);
        this.worldDrop = new HashMap<>();
        try {
            this.loadEnemyKillRewardConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() throws Exception {
        log.info("reward service reload");
        this.worldDrop.clear();
        this.loadEnemyKillRewardConfig();
    }

    public void loadEnemyKillRewardConfig() throws Exception {
        int items = 0;
        File xml = new File("./config/custom_data/world_pvp_rewards.xml");
        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        if (xml.exists()) {
            try {
                doc = factory.newDocumentBuilder().parse(xml);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Node table = doc.getFirstChild();
            for (Node nodea = table.getFirstChild(); nodea != null; nodea = nodea.getNextSibling()) {
                if (nodea.getNodeName().equals("world")) {
                    NamedNodeMap attrs = nodea.getAttributes();
                    int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                    WorldDropEntry world = this.worldDrop.get(id);
                    if (world == null) { world = new WorldDropEntry(); }

                    Node ncond = attrs.getNamedItem("cond");
                    if (ncond != null) {
                        world.cond = ncond.getNodeValue();
                    }

                    for (Node nodeb = nodea.getFirstChild(); nodeb != null; nodeb = nodeb.getNextSibling()) {
                        if (nodeb.getNodeName().equals("entry")) {
                            WorldDropItemEntry entry = new WorldDropItemEntry();
                            attrs = nodeb.getAttributes();
                            entry.rate = Integer.parseInt(attrs.getNamedItem("rate").getNodeValue());
                            if (attrs.getNamedItem("random") != null) {
                                entry.random = Boolean.parseBoolean(attrs.getNamedItem("random").getNodeValue());
                            }

                            for (Node nodec = nodeb.getFirstChild(); nodec != null; nodec = nodec.getNextSibling()) {
                                if (nodec.getNodeName().equals("item")) {
                                    attrs = nodec.getAttributes();
                                    int itemId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                                    ItemTemplate tpl = DataManager.ITEM_DATA.getItemTemplate(itemId);
                                    if (tpl == null) {
                                        throw new IllegalArgumentException("reward service was unable to recognize item id " + itemId + " as item");
                                    }

                                    int nameId = tpl.getNameId();
                                    int count = 1;
                                    if (attrs.getNamedItem("count") != null) {
                                        count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                                    }

                                    entry.pushItem(itemId, count, nameId);
                                    items++;
                                }
                            }

                            world.entries.add(entry);
                        }
                    }

                    this.worldDrop.put(id, world);
                }
            }
        }
        log.info("enemy kill reward items: " + this.worldDrop.size() + " worlds with " + items + " items.");
    }

    public void verify(Player player) {
        List<RewardEntryItem> list = dao.getAvailable(player.getObjectId());
        if (list.size() == 0) {
            return;
        }
        if (player.getCommonData().getMailboxLetters() >= 100) {
            log.warn("[RewardController] player " + player.getName() + " tried to receive item with mail box full.");
            return;
        }

        List<Integer> rewarded = new ArrayList<>(list.size());
        for (RewardEntryItem item : list) {
            if (DataManager.ITEM_DATA.getItemTemplate(item.id) == null) {
                log.warn("[RewardController][" + item.unique + "] null template for item " + item.id + " on player " + player
                    .getObjectId() + ".");
                continue;
            } else {
                try {
                    SystemMailService.getInstance()
                                     .sendMail("$$CASH_ITEM_MAIL", player.getName(), item.id + ", " + item.count, "0, " + System
                                         .currentTimeMillis() / 1000 + ",", item.id, (int) item.count, 0L, LetterType.BLACKCLOUD);
                    log.info("[RewardController][" + item.unique + "] player " + player.getName() + " has received (" + item.count + ")" + item.id + ".");
                    rewarded.add(item.unique);
                } catch (Exception e) {
                    log.error("[RewardController][" + item.unique + "] failed to add item (" + item.count + ")" + item.id + " to " + player
                        .getObjectId(), e);
                }
            }
        }

        if (rewarded.size() > 0) {
            dao.uncheckAvailable(rewarded);
        }
    }

    public void enemyKillReward(Player winner, Player victim) {
        if (this.worldDrop.containsKey(victim.getWorldId())) {
            WorldDropEntry world = this.worldDrop.get(victim.getWorldId());

            if (world.cond != null) {
                if (!victim.getConditioner().check(GiveRewardDecision.class, world.cond)) {
                    return;
                }
            }

            for (WorldDropItemEntry entry : world.entries) {
                if (winner.isInGroup2()) {
                    for (Player member : winner.getPlayerGroup2().getMembers()) {
                        this.enemyKillRewardFinish(entry, member, victim);
                    }
                } else {
                    this.enemyKillRewardFinish(entry, winner, victim);
                }
            }
        }
    }

    private void enemyKillRewardFinish(WorldDropItemEntry entry, Player member, Player victim) {
        if (!MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE)) {
            return;
        }

        int dailyKills = PvpService.getDailyKillsFor(member.getObjectId(), victim.getObjectId());
        if (dailyKills >= PvPConfig.CHAIN_KILL_NUMBER_RESTRICTION) {
            member.sendMsg(String.format("Сегодня вы убили %s %d раз, и больше не получите награду!", victim.getName(), dailyKills));
        } else {
            if (Rnd.get(100) <= entry.rate) {
                if (entry.random) {
                    WorldDropEntryItem item;
                    switch (entry.items.size()) {
                        case 1:
                            item = entry.items.get(0);
                            break;
                        default:
                            item = entry.items.get(Rnd.get(0, entry.items.size() - 1));
                            break;
                    }

                    if (item != null) { this.rewardItem(member, item); } else {
                        log.warn("enemyKillReward@ null item");
                    }

                } else {
                    for (WorldDropEntryItem item : entry.items) {
                        this.rewardItem(member, item);
                    }
                }
            }
        }
    }

    private void rewardItem(Player winner, WorldDropEntryItem item) {
        ItemService.addItem(winner, item.id, item.count);
//        long result = ItemService.addItem(winner, item.id, item.count);
//        if (result == 0) {
//            if (item.count == 1) {
//                winner.sendPck(new SM_SYSTEM_MESSAGE(1390000, item.nameId)); // You have aquired %s
//            } else {
//                winner.sendPck(new SM_SYSTEM_MESSAGE(1390005, item.count, item.nameId)); // You have aquired %s %d(s)
//            }
//        }
    }

    public static class GiveRewardDecision extends SimpleCond<String> {
        final String cond;

        public GiveRewardDecision(String cond) {
            this.cond = cond;
        }

        @NotNull
        @Override
        public String getType() {
            return GiveRewardDecision.class.getName();
        }

        @Override
        public Boolean onEvent(@NotNull String e) {
            return e.equals(cond);
        }
    }
}
