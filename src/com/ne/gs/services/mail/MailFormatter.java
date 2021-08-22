/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.mail;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.templates.mail.MailPart;
import com.ne.gs.model.templates.mail.MailTemplate;

public final class MailFormatter {

    public static void sendBlackCloudMail(String recipientName, final int itemObjectId, final int itemCount) {
        MailTemplate template = DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate("$$CASH_ITEM_MAIL", "", Race.PC_ALL);

        MailPart formatter = new MailPart() {
            @Override
            public String getParamValue(String name) {
                if ("itemid".equals(name)) {
                    return Integer.toString(itemObjectId);
                }
                if ("count".equals(name)) {
                    return Integer.toString(itemCount);
                }
                if ("unk1".equals(name)) {
                    return "0";
                }
                if ("purchasedate".equals(name)) {
                    return Long.toString(System.currentTimeMillis() / 1000L);
                }
                return "";
            }
        };
        String title = template.getFormattedTitle(formatter);
        String body = template.getFormattedMessage(formatter);

        SystemMailService.getInstance().sendMail("$$CASH_ITEM_MAIL", recipientName, title, body, itemObjectId, itemCount, 0L, LetterType.BLACKCLOUD);
    }

    public static void sendAbyssRewardMail(final SiegeLocation siegeLocation, final PlayerCommonData playerData, final AbyssSiegeLevel level,
                                           final SiegeResult result, final long time, int attachedItemObjId, long attachedItemCount, long attachedKinahCount) {
        MailTemplate template = DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate("$$ABYSS_REWARD_MAIL", "", playerData.getRace());

        MailPart formatter = new MailPart() {
            @Override
            public String getParamValue(String name) {
                if ("siegelocid".equals(name)) {
                    return Integer.toString(siegeLocation.getTemplate().getId());
                }
                if ("datetime".equals(name)) {
                    return Long.toString(time / 1000);
                }
                if ("rankid".equals(name)) {
                    return Integer.toString(level.getId());
                }
                if ("raceid".equals(name)) {
                    return Integer.toString(playerData.getRace().getRaceId());
                }
                if ("resultid".equals(name)) {
                    return Integer.toString(result.getId());
                }
                return "";
            }
        };
        String title = template.getFormattedTitle(formatter);
        String message = template.getFormattedMessage(formatter);

        SystemMailService.getInstance().sendMail("$$ABYSS_REWARD_MAIL", playerData.getName(), title, message, attachedItemObjId, attachedItemCount,
            attachedKinahCount, LetterType.NORMAL);
    }
}
