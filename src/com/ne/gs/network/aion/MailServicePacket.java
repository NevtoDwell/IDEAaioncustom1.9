/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.Iterables;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Letter;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;

/**
 * @author kosyachok
 */
public abstract class MailServicePacket extends AionServerPacket {

    protected Player player;

    /**
     * @param player
     */
    public MailServicePacket(Player player) {
        this.player = player;
    }

    protected void writeLettersList(Collection<Letter> letters, Player player, boolean isPostman, int showCount) {
        Iterator<List<Letter>> it = Iterables.partition(letters, 90).iterator();
        if (it.hasNext()) {
            letters = it.next();
        } else {
            letters = Collections.emptyList();
        }

        writeD(player.getObjectId());
        if ((player.getMailbox().isMailListUpdateRequired) || (isPostman)) {
            writeC(0);
            writeH(isPostman ? -showCount : -letters.size());
            for (Letter letter : letters) {
                if (!isPostman || (letter.isExpress() && letter.isUnread())) {
                    writeD(letter.getObjectId());
                    writeS(letter.getSenderName());
                    writeS(letter.getTitle());
                    writeC(letter.isUnread() ? 0 : 1);
                    if (letter.getAttachedItem() != null) {
                        writeD(letter.getAttachedItem().getObjectId());
                        writeD(letter.getAttachedItem().getItemTemplate().getTemplateId());
                    } else {
                        writeD(0);
                        writeD(0);
                    }
                    writeQ(letter.getAttachedKinah());
                    writeC(letter.getLetterType().getId());
                }
            }
            player.getMailbox().isMailListUpdateRequired = isPostman;
        } else {
            writeC(1);
            writeH(0);
        }
    }

    protected void writeMailMessage(int messageId) {
        writeC(messageId);
    }

    protected void writeMailboxState(int totalCount, int unreadCount, int expressCount, int blackCloudCount) {
        writeH(totalCount);
        writeH(unreadCount);
        writeH(expressCount);
        writeH(blackCloudCount);
    }

    protected void writeLetterRead(Letter letter, long time, int totalCount, int unreadCount, int expressCount,
                                   int blackCloudCount) {
        writeD(letter.getRecipientId());
        writeD(totalCount + unreadCount * 65536);
        writeD(expressCount + blackCloudCount);
        writeD(letter.getObjectId());
        writeD(letter.getRecipientId());
        writeS(letter.getSenderName());
        writeS(letter.getTitle());
        writeS(letter.getMessage());

        Item item = letter.getAttachedItem();
        if (item != null) {
            ItemTemplate itemTemplate = item.getItemTemplate();

            writeD(item.getObjectId());
            writeD(itemTemplate.getTemplateId());
            writeD(1);// unk
            writeD(0);// unk
            writeNameId(itemTemplate.getNameId());

            ItemInfoBlob itemInfoBlob = ItemInfoBlob.getFullBlob(player, item);
            itemInfoBlob.writeMe(getBuf());
        } else {
            writeQ(0);
            writeQ(0);
            writeD(0);
        }

        writeD((int) letter.getAttachedKinah());
        writeD(0); // AP reward for castle assault/defense (in future)
        writeC(0);
        writeD((int) (time / 1000));
        writeC(letter.getLetterType().getId());
    }

    protected void writeLetterState(int letterId, int attachmentType) {
        writeD(letterId);
        writeC(attachmentType);
        writeC(1);
    }

    protected void writeLetterDelete(int totalCount, int unreadCount, int expressCount, int blackCloudCount, int... letterIds) {
        writeD(totalCount + unreadCount * 65536);
        writeD(expressCount + blackCloudCount);
        writeH(letterIds.length);
        for (int letterId : letterIds) {
            writeD(letterId);
        }
    }
}
