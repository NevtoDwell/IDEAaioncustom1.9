/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.mail;

import java.sql.Timestamp;
import java.util.Calendar;

import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.database.dao.InventoryDAO;
import com.ne.gs.database.dao.MailDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Letter;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Mailbox;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_MAIL_SERVICE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemFactory;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;

public class SystemMailService {

    private static final Logger log = LoggerFactory.getLogger("SYSMAIL_LOG");

    public static SystemMailService getInstance() {
        return SingletonHolder.instance;
    }

    private SystemMailService() {
        log.info("SystemMailService: Initialized.");
    }

    public int sendMail(String sender, String recipientName, String title, String message, int attachedItemTemplId,
                         long attachedItemCount, long attachedKinahCount, LetterType letterType) {

        if (attachedItemTemplId != 0) {
            ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(attachedItemTemplId);
            if (itemTemplate == null) {
                log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] RETURN ITEM ID:" + itemTemplate
                    + " ITEM COUNT " + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " ITEM TEMPLATE IS MISSING ");

                return 0;
            }
        }

        if ((attachedItemCount == 0L) && (attachedItemTemplId != 0)) {
            return 0;
        }
        if (recipientName.length() > 16) {
            log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] ITEM RETURN" + attachedItemTemplId + " ITEM COUNT "
                + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " RECIPIENT NAME LENGTH > 16 ");

            return 0;
        }

        if ((!sender.startsWith("$$")) && (sender.length() > 16)) {
            log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] ITEM RETURN" + attachedItemTemplId + " ITEM COUNT "
                + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " SENDER NAME LENGTH > 16 ");

            return 0;
        }

        if (title.length() > 20) {
            title = title.substring(0, 20);
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000);
        }
        PlayerCommonData recipientCommonData = GDB.get(PlayerDAO.class).loadPlayerCommonDataByName(recipientName);

        if (recipientCommonData == null) {
            log.info("[SYSMAILSERVICE] > [RecipientName: " + recipientName + "] NO SUCH CHARACTER NAME.");
            return 0;
        }

        Player recipient = World.getInstance().findPlayer(recipientCommonData.getPlayerObjId());
        if (((recipient != null) && (!recipient.getMailbox().haveFreeSlots())) || (recipientCommonData.getMailboxLetters() > 99)) {
            log.info(String.format("[SYSMAILSERVICE] > [SenderName: %s] [RecipientName: %s] ITEM RETURN%d ITEM COUNT %d KINAH COUNT %d MAILBOX FULL ",
                sender, recipientCommonData.getName(), attachedItemTemplId, attachedItemCount, attachedKinahCount));

            return 0;
        }

        Item attachedItem = null;
        long finalAttachedKinahCount = 0L;
        int itemId = attachedItemTemplId;
        long count = attachedItemCount;

        if (itemId != 0) {
            Item senderItem = ItemFactory.newItem(itemId, count);
            if (senderItem != null) {
                senderItem.setEquipped(false);
                senderItem.setEquipmentSlot(0);
                senderItem.setItemLocation(StorageType.MAILBOX.getId());
                attachedItem = senderItem;
            }
        }

        if (attachedKinahCount > 0L) {
            finalAttachedKinahCount = attachedKinahCount;
        }
        String finalSender = sender;
        Timestamp time = new Timestamp(Calendar.getInstance().getTimeInMillis());
        int newLetterId = IDFactory.getInstance().nextId();
        Letter newLetter = new Letter(newLetterId, recipientCommonData.getPlayerObjId(), attachedItem, finalAttachedKinahCount,
            title, message, finalSender, time, true, letterType);

        if (!GDB.get(MailDAO.class).storeLetter(time, newLetter)) {
            return 0;
        }
        if ((attachedItem != null) && (!GDB.get(InventoryDAO.class).store(attachedItem, recipientCommonData.getPlayerObjId()))) {
            return 0;
        }

        if (recipient != null) {
            Mailbox recipientMailbox = recipient.getMailbox();
            recipientMailbox.putLetterToMailbox(newLetter);

            recipient.sendPck(new SM_MAIL_SERVICE(recipient.getMailbox()));
            recipientMailbox.isMailListUpdateRequired = true;

            if (recipientMailbox.mailBoxState != 0) {
                boolean isPostman = (recipientMailbox.mailBoxState & 0x2) == 2;
                recipient.sendPck(new SM_MAIL_SERVICE(recipient, recipientMailbox.getLetters(), isPostman));
            }

            if (letterType == LetterType.EXPRESS) {
                recipient.sendPck(SM_SYSTEM_MESSAGE.STR_POSTMAN_NOTIFY);
            }

        }

        if (!recipientCommonData.isOnline()) {
            recipientCommonData.setMailboxLetters(recipientCommonData.getMailboxLetters() + 1);
            GDB.get(MailDAO.class).updateOfflineMailCounter(recipientCommonData);
        }
        if (LoggingConfig.LOG_SYSMAIL) {
            log.info("[SYSMAILSERVICE] > [SenderName: " + sender + "] [RecipientName: " + recipientName + "] RETURN ITEM ID:" + itemId + " ITEM COUNT "
                + attachedItemCount + " KINAH COUNT " + attachedKinahCount + " MESSAGE SUCCESSFULLY SENDED ");
        }
        
        return newLetterId;
    }

    private static final class SingletonHolder {

        protected static final SystemMailService instance = new SystemMailService();
    }
}
