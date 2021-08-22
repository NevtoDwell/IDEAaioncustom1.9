/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.database.SQL;
import com.ne.commons.database.SqlTable;
import com.ne.commons.database.TableColumn;
import com.ne.commons.database.TableRow;
import com.ne.gs.cache.HTMLCache;
import com.ne.gs.configs.main.HTMLConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.Guides.GuideTemplate;
import com.ne.gs.model.templates.Guides.SurveyTemplate;
import com.ne.gs.network.aion.serverpackets.SM_QUESTIONNAIRE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * Use this service to send raw html to the client.
 *
 * @author lhw, xTz
 */
public final class HTMLService {

    private static final Logger log = LoggerFactory.getLogger("ITEM_HTML_LOG");

    public static String getHTMLTemplate(GuideTemplate template) {
        String context = HTMLCache.getInstance().getHTML("guideTemplate.xhtml");

        StringBuilder sb = new StringBuilder();
        sb.append("<reward_items multi_count='").append(template.getRewardCount()).append("'>\n");
        for (SurveyTemplate survey : template.getSurveys()) {
            sb.append("<item_id count='").append(survey.getCount()).append("'>").append(survey.getItemId())
                .append("</item_id>\n");
        }
        sb.append("</reward_items>\n");
        context = context.replace("%reward%", sb);
        context = context.replace("%radio%", template.getSelect().isEmpty() ? " " : template.getSelect());
        context = context.replace("%html%", template.getMessage().isEmpty() ? " " : template.getMessage());
        context = context.replace("%rewardInfo%", template.getRewardInfo().isEmpty() ? " " : template.getRewardInfo());
        return context;
    }

    public static void showHTML(Player player, String html) {
        sendData(player, IDFactory.getInstance().nextId(), html);
    }

    public static void sendData(Player player, int messageId, String html) {
        byte packet_count = (byte) Math.ceil(html.length() / (Short.MAX_VALUE - 8) + 1);
        if (packet_count < 256) {
            for (byte i = 0; i < packet_count; i++) {
                try {
                    int from = i * (Short.MAX_VALUE - 8), to = (i + 1) * (Short.MAX_VALUE - 8);
                    if (from < 0) {
                        from = 0;
                    }
                    if (to > html.length()) {
                        to = html.length();
                    }
                    String sub = html.substring(from, to);
                    player.getClientConnection().sendPacket(new SM_QUESTIONNAIRE(messageId, i, packet_count, sub));
                } catch (Exception e) {
                    log.error("htmlservice.sendData", e);
                }
            }
        }
    }

    public static void sendGuideHtml(Player player) {
        if (player.getLevel() > 1) {
            List<GuideTemplate> surveyTemplate = DataManager.GUIDE_HTML_DATA
                .getTemplatesFor(player.getPlayerClass(), player.getRace(), player.getLevel());

            List<TableRow<GuidesTable>> rows;
            if (HTMLConfig.ACCOUNT_GUIDES) {
                rows = SQL.select(GuidesTable.class)
                    .where(GuidesTable.account_id, player.getPlayerAccount().getId())
                        //.and(GuidesTable.aquired, 1)
                    .submit();
            } else {
                rows = SQL.select(GuidesTable.class)
                    .where(GuidesTable.player_id, player.getObjectId())
                        //.and(GuidesTable.aquired, 1)
                    .submit();
            }

            Set<String> aquiredTitles = new THashSet<>(rows.size());
            Map<String, TableRow<GuidesTable>> exists = new THashMap<>(rows.size());
            for (TableRow<GuidesTable> row : rows) {
                if (row.get(GuidesTable.aquired).equals(true)) {
                    aquiredTitles.add(row.<String>get(GuidesTable.title));
                } else {
                    exists.put(row.<String>get(GuidesTable.title), row);
                }
            }

            Iterator<GuideTemplate> it = surveyTemplate.iterator();
            while (it.hasNext()) {
                GuideTemplate tpl = it.next();
                if (aquiredTitles.contains(tpl.getTitle())) {
                    it.remove();
                }
            }

            for (GuideTemplate template : surveyTemplate) {
                if (template != null) {
                    if (template.isActivated()) {
                        int id;
                        if (exists.containsKey(template.getTitle())) {
                            id = exists.get(template.getTitle()).<Integer>get(GuidesTable.guide_id);
                            sendData(player, id, getHTMLTemplate(template));
                        } else {
                            id = IDFactory.getInstance().nextId();
                            sendData(player, id, getHTMLTemplate(template));
                            SQL.insertOrUpdate(GuidesTable.class,
                                TableRow.of(GuidesTable.class)
                                    .set(GuidesTable.guide_id, id)
                                    .set(GuidesTable.player_id, player.getObjectId())
                                    .set(GuidesTable.account_id, player.getPlayerAccount().getId())
                                    .set(GuidesTable.title, template.getTitle())
                                    .set(GuidesTable.aquired, false))
                                .submit();
                        }
                    }
                } else {
                    log.warn("Null guide templates for player: {}", player.getName());
                }
            }
        }
    }

    public static void onPlayerLogin(Player player) {
        if (player == null) {
            return;
        }

        List<TableRow<GuidesTable>> rows;
        if (HTMLConfig.ACCOUNT_GUIDES) {
            rows = SQL.select(GuidesTable.class)
                .where(GuidesTable.account_id, player.getPlayerAccount().getId())
                .and(GuidesTable.aquired, 0)
                .submit();
        } else {
            rows = SQL.select(GuidesTable.class)
                .where(GuidesTable.player_id, player.getObjectId())
                .and(GuidesTable.aquired, 0)
                .submit();
        }

        for (TableRow<GuidesTable> row : rows) {
            GuideTemplate template = DataManager.GUIDE_HTML_DATA
                .getTemplateByTitle(row.<String>get(GuidesTable.title));
            if (template != null) {
                if (template.isActivated()) {
                    sendData(player, row.<Integer>get(GuidesTable.guide_id), getHTMLTemplate(template));
                }
            } else {
                log.warn("Null guide template for title: {}", row.<String>get(GuidesTable.title));
            }
        }
    }

    public static void getReward(Player player, int messageId, List<Integer> items) {
        if (player == null || messageId < 1) {
            return;
        }
        if (SurveyService.getInstance().isActive(player, messageId)) {
            return;
        }

        List<TableRow<GuidesTable>> rows;
        if (HTMLConfig.ACCOUNT_GUIDES) {
            rows = SQL.select(GuidesTable.class)
                .where(GuidesTable.guide_id, messageId)
                .and(GuidesTable.account_id, player.getPlayerAccount().getId())
                .and(GuidesTable.aquired, 0)
                .submit();
        } else {
            rows = SQL.select(GuidesTable.class)
                .where(GuidesTable.guide_id, messageId)
                .and(GuidesTable.player_id, player.getObjectId())
                .and(GuidesTable.aquired, 0)
                .submit();
        }

        if (rows.iterator().hasNext()) {
            TableRow<GuidesTable> row = rows.iterator().next();
            GuideTemplate template = DataManager.GUIDE_HTML_DATA
                .getTemplateByTitle(row.<String>get(GuidesTable.title));

            if (template == null) {
                return;
            }
            if (items.size() > template.getRewardCount()) {
                return;
            }

            if (items.size() > player.getInventory().getFreeSlots()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
                return;
            }

            List<SurveyTemplate> templates;
            if (template.getSurveys().size() != template.getRewardCount()) {
                templates = getSurveyTemplates(template.getSurveys(), items);
            } else {
                templates = template.getSurveys();
            }

            if (templates.isEmpty()) {
                return;
            }

            for (SurveyTemplate item : templates) {
                ItemService.addItem(player, item.getItemId(), item.getCount());
                if (LoggingConfig.LOG_ITEM) {
                    log.info(String.format("[ITEM] Item Guide ID/Count - %d/%d to player %s.", item.getItemId(), item
                        .getCount(), player.getName()));
                }
            }

            SQL.insertOrUpdate(GuidesTable.class,
                TableRow.of(GuidesTable.class)
                    .set(GuidesTable.guide_id, messageId)
                    .set(GuidesTable.player_id, player.getObjectId())
                    .set(GuidesTable.account_id, player.getPlayerAccount().getId())
                    .set(GuidesTable.title, template.getTitle())
                    .set(GuidesTable.aquired, 1))
                .submit();
        }
    }

    private static List<SurveyTemplate> getSurveyTemplates(List<SurveyTemplate> surveys,
                                                           List<Integer> items) {
        List<SurveyTemplate> templates = new ArrayList<>();
        for (SurveyTemplate survey : surveys) {
            if (items.contains(survey.getItemId())) {
                templates.add(survey);
            }
        }
        return templates;
    }

    public static Iterable<Integer> getUsedIDs() {
        return Iterables
            .transform(SQL.select(GuidesTable.class).submit(),
                new Function<TableRow<GuidesTable>, Integer>() {
                    @Override
                    public Integer apply(TableRow<GuidesTable> row) {
                        return row.get(GuidesTable.guide_id);
                    }
                });
    }

    @SqlTable(name = "guides")
    static enum GuidesTable implements TableColumn {
        guide_id("guide_id", Integer.class),
        player_id("player_id", Integer.class),
        account_id("account_id", Integer.class),
        title("title", String.class),
        aquired("aquired", Boolean.class);

        private final String _name;
        private final Class<?> _type;

        private GuidesTable(String name, Class<?> type) {
            _name = name;
            _type = type;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public Class<?> getType() {
            return _type;
        }
    }
}
