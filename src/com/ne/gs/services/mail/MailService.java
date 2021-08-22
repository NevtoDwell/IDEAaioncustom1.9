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

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.database.dao.InventoryDAO;
import com.ne.gs.database.dao.MailDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Letter;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Mailbox;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.templates.mail.MailMessage;
import com.ne.gs.network.aion.serverpackets.SM_DELETE_ITEM;
import com.ne.gs.network.aion.serverpackets.SM_MAIL_SERVICE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.AdminService;
import com.ne.gs.services.item.ItemFactory;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;

public class MailService {

    private static final Logger log = LoggerFactory.getLogger("MAIL_LOG");

    public static MailService getInstance() {
        return SingletonHolder.instance;
    }

    public void sendMail(Player sender, String recipientName, String title, String message, int attachedItemObjId,
                         int attachedItemCount, int attachedKinahCount, LetterType letterType) {

        if (letterType == LetterType.BLACKCLOUD || recipientName.length() > 16) {
            return;
        }
        if (title.length() > 20) {
            title = title.substring(0, 20);
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000);
        }
        PlayerCommonData recipientCommonData = GDB.get(PlayerDAO.class).loadPlayerCommonDataByName(recipientName);

        if (recipientCommonData == null) {
            sender.sendPck(new SM_MAIL_SERVICE(MailMessage.NO_SUCH_CHARACTER_NAME));
            return;
        }

        if (recipientCommonData.getRace() != sender.getRace() && sender.getAccessLevel() < AdminConfig.GM_LEVEL) {
            sender.sendPck(new SM_MAIL_SERVICE(MailMessage.MAIL_IS_ONE_RACE_ONLY));
            return;
        }

        Player recipient = World.getInstance().findPlayer(recipientCommonData.getPlayerObjId());
        if (recipient != null && !recipient.getMailbox().haveFreeSlots() || recipientCommonData.getMailboxLetters() > 99) {
            sender.sendPck(new SM_MAIL_SERVICE(MailMessage.RECIPIENT_MAILBOX_FULL));
            return;
        }

        if (!validateMailSendPrice(sender, attachedKinahCount, attachedItemObjId, attachedItemCount)) {
            return;
        }
        Item attachedItem = null;
        int finalAttachedKinahCount = 0;

        int kinahMailCommission = 0;
        int itemMailCommission = 0;

        Storage senderInventory = sender.getInventory();

        if (attachedItemObjId != 0 && attachedItemCount > 0) {
            Item senderItem = senderInventory.getItemByObjId(attachedItemObjId);

            if (senderItem == null) {
                return;
            }

            if (!senderItem.isTradeable(sender)) {
                return;
            }
            if (!AdminService.getInstance().canOperate(sender, null, senderItem, "mail")) {
                return;
            }
            float qualityPriceRate;
            switch (senderItem.getItemTemplate().getItemQuality()) {
                case JUNK:
                case COMMON:
                    qualityPriceRate = 0.02F;
                    break;
                case RARE:
                    qualityPriceRate = 0.03F;
                    break;
                case LEGEND:
                case UNIQUE:
                    qualityPriceRate = 0.04F;
                    break;
                case EPIC:
                case MYTHIC:
                    qualityPriceRate = 0.05F;
                    break;
                default:
                    qualityPriceRate = 0.02F;
            }

            if (senderItem.getItemCount() < attachedItemCount) {
                return;
            }

            if (senderItem.getItemCount() == attachedItemCount) {
                senderInventory.remove(senderItem);
                sender.sendPck(new SM_DELETE_ITEM(attachedItemObjId));
                attachedItem = senderItem;
            } else if (senderItem.getItemCount() > attachedItemCount) {
                attachedItem = ItemFactory.newItem(senderItem.getItemTemplate().getTemplateId(), attachedItemCount);
                senderInventory.decreaseItemCount(senderItem, attachedItemCount);
            }

            if (attachedItem == null) {
                return;
            }
            attachedItem.setEquipped(false);
            attachedItem.setEquipmentSlot(0);
            attachedItem.setItemLocation(StorageType.MAILBOX.getId());
            itemMailCommission = Math.round(attachedItem.getItemTemplate().getPrice() * attachedItem.getItemCount() * qualityPriceRate);
        }

        if (attachedKinahCount > 0 && senderInventory.getKinah() - attachedKinahCount >= 0) {
            finalAttachedKinahCount = attachedKinahCount;
            kinahMailCommission = Math.round(attachedKinahCount * 0.01F);
        }

        int finalMailKinah = 10 + kinahMailCommission + itemMailCommission + finalAttachedKinahCount;

        if (senderInventory.getKinah() > finalMailKinah) {
            senderInventory.decreaseKinah(finalMailKinah);
        } else {
            AuditLogger.info(sender, "Mail kinah exploit.");
            return;
        }

        Timestamp time = new Timestamp(Calendar.getInstance().getTimeInMillis());

        Letter newLetter = new Letter(IDFactory.getInstance().nextId(), recipientCommonData.getPlayerObjId(), attachedItem, finalAttachedKinahCount,
            title, message, sender.getName(), time, true, letterType);

        if (attachedItem != null && !GDB.get(InventoryDAO.class).store(attachedItem, recipientCommonData.getPlayerObjId())) {
            return;
        }
        if (!GDB.get(MailDAO.class).storeLetter(time, newLetter)) {
            return;
        }

        if (recipient != null) {
            Mailbox recipientMailbox = recipient.getMailbox();
            recipientMailbox.putLetterToMailbox(newLetter);

            sender.sendPck(new SM_MAIL_SERVICE(MailMessage.MAIL_SEND_SECCESS));

            recipient.sendPck(new SM_MAIL_SERVICE(recipientMailbox));
            recipientMailbox.isMailListUpdateRequired = true;

            if (recipientMailbox.mailBoxState != 0) {
                boolean isPostman = (recipientMailbox.mailBoxState & 0x2) == 2;
                recipient.sendPck(new SM_MAIL_SERVICE(recipient, recipientMailbox.getLetters(), isPostman));
            }

            if (letterType == LetterType.EXPRESS) {
                recipient.sendPck(SM_SYSTEM_MESSAGE.STR_POSTMAN_NOTIFY);
            }
        }
        if (attachedItem != null && LoggingConfig.LOG_MAIL) {
            log.info("[MAILSERVICE] [Player: " + sender.getName() + "] send [Item: " + attachedItem.getItemId()
                + (LoggingConfig.ENABLE_ADVANCED_LOGGING ? "] [Item Name: " + attachedItem.getItemName() + "]" : "]") + " [Count: "
                + attachedItem.getItemCount() + "] to [Reciever: " + recipientName + "]");
        }

        if (!recipientCommonData.isOnline()) {
            sender.sendPck(new SM_MAIL_SERVICE(MailMessage.MAIL_SEND_SECCESS));
            recipientCommonData.setMailboxLetters(recipientCommonData.getMailboxLetters() + 1);
            GDB.get(MailDAO.class).updateOfflineMailCounter(recipientCommonData);
        }
    }

    public void readMail(Player player, int letterId) {
        Letter letter = player.getMailbox().getLetterFromMailbox(letterId);
        if (letter == null) {
            log.warn("Cannot read mail " + player.getObjectId() + " " + letterId);
            return;
        }

        player.sendPck(new SM_MAIL_SERVICE(player, letter, letter.getTimeStamp().getTime()));
        letter.setReadLetter();
    }

    public void getAttachments(Player player, int letterId, int attachmentType) {
        Letter letter = player.getMailbox().getLetterFromMailbox(letterId);

        if (letter == null) {
            return;
        }
        switch (attachmentType) {
            case 0:
                Item attachedItem = letter.getAttachedItem();
                if (attachedItem == null) {
                    return;
                }
                if (player.getInventory().isFull()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY);
                    return;
                }
                player.getInventory().add(attachedItem);
                if (!GDB.get(InventoryDAO.class).store(attachedItem, player.getObjectId())) {
                    return;
                }
                player.sendPck(new SM_MAIL_SERVICE(letterId, attachmentType));
                letter.removeAttachedItem();
                break;
            case 1:
                player.getInventory().increaseKinah(letter.getAttachedKinah());
                player.sendPck(new SM_MAIL_SERVICE(letterId, attachmentType));
                letter.removeAttachedKinah();
        }
    }

    public void deleteMail(Player player, int[] mailObjId) {
        Mailbox mailbox = player.getMailbox();

        for (int letterId : mailObjId) {
            mailbox.removeLetter(letterId);
            GDB.get(MailDAO.class).deleteLetter(letterId);
        }
        player.sendPck(new SM_MAIL_SERVICE(mailObjId));
    }

    private boolean validateMailSendPrice(Player sender, int attachedKinahCount, int attachedItemObjId, int attachedItemCount) {
        int itemMailCommission = 0;
        int kinahMailCommission = Math.round(attachedKinahCount * 0.01F);
        if (attachedItemObjId != 0) {
            Item senderItem = sender.getInventory().getItemByObjId(attachedItemObjId);
            if (senderItem == null || senderItem.getItemTemplate() == null) {
                return false;
            }
            float qualityPriceRate;
            switch (senderItem.getItemTemplate().getItemQuality()) {
                case JUNK:
                case COMMON:
                    qualityPriceRate = 0.02F;
                    break;
                case RARE:
                    qualityPriceRate = 0.03F;
                    break;
                case LEGEND:
                case UNIQUE:
                    qualityPriceRate = 0.04F;
                    break;
                case EPIC:
                case MYTHIC:
                    qualityPriceRate = 0.05F;
                    break;
                default:
                    qualityPriceRate = 0.02F;
            }

            itemMailCommission = Math.round(senderItem.getItemTemplate().getPrice() * attachedItemCount * qualityPriceRate);
        }

        int finalMailPrice = 10 + itemMailCommission + kinahMailCommission;

        return sender.getInventory().getKinah() >= finalMailPrice;
    }

    public void onPlayerLogin(Player player) {
        ThreadPoolManager.getInstance().schedule(new MailLoadTask(player), 5000);
    }

    public void refreshMail(Player player) {
        player.sendPck(new SM_MAIL_SERVICE(player.getMailbox()));
        player.sendPck(new SM_MAIL_SERVICE(player, player.getMailbox().getLetters(), false));
    }

    private class MailLoadTask implements Runnable {

        private final Player player;

        private MailLoadTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            player.setMailbox(GDB.get(MailDAO.class).loadPlayerMailbox(player));
            player.sendPck(new SM_MAIL_SERVICE(player.getMailbox()));
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final MailService instance = new MailService();
    }
}
