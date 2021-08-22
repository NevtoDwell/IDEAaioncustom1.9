/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javolution.util.FastMap;

import com.ne.gs.model.gameobjects.Letter;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.network.aion.serverpackets.SM_MAIL_SERVICE;
import com.ne.gs.services.mail.MailService;

/**
 * @author kosyachok
 * @modified Atracer
 */
public class Mailbox {

    private final Map<Integer, Letter> mails = new FastMap<Integer, Letter>().shared();
    private final Map<Integer, Letter> reserveMail = new FastMap<Integer, Letter>().shared();

    private final Player owner;
    public boolean isMailListUpdateRequired;
    public byte mailBoxState = 0;
    private boolean skipPostmanCooldown;

    public Mailbox(Player player) {
        owner = player;
    }

    /**
     * @param letter
     */
    public void putLetterToMailbox(Letter letter) {
        if (haveFreeSlots()) {
            mails.put(letter.getObjectId(), letter);
        } else {
            reserveMail.put(letter.getObjectId(), letter);
        }
    }

    /**
     * Get all letters in mailbox (sorted according to time received)
     *
     * @return
     */
    public Collection<Letter> getLetters() {
        SortedSet<Letter> letters = new TreeSet<>(new Comparator<Letter>() {
            @Override
            public int compare(Letter o1, Letter o2) {
                if (o1.getTimeStamp().getTime() > o2.getTimeStamp().getTime()) {
                    return 1;
                }
                if (o1.getTimeStamp().getTime() < o2.getTimeStamp().getTime()) {
                    return -1;
                }

                return o1.getObjectId() > o2.getObjectId() ? 1 : -1;
            }
        });

        for (Letter letter : mails.values()) {
            letters.add(letter);
        }

        return letters;
    }

    public List<Letter> getNewSystemLetters(String substring) {
        List<Letter> letters = new ArrayList<>();
        for (Letter letter : mails.values()) {
            if (letter.getSenderName() != null && letter.isUnread() && owner.getCommonData().getLastOnline().getTime() <= letter.getTimeStamp().getTime()) {
                if ((letter.getSenderName().startsWith("%") || letter.getSenderName().startsWith("$$")) && letter.getSenderName().startsWith(substring)) {
                    letters.add(letter);
                }
            }
        }
        return letters;
    }

    /**
     * Get letter with specified letter id
     *
     * @param letterObjId
     *
     * @return
     */
    public Letter getLetterFromMailbox(int letterObjId) {
        return mails.get(letterObjId);
    }

    /**
     * Check whether mailbox contains empty letters
     *
     * @return
     */
    public boolean haveUnread() {
        for (Letter letter : mails.values()) {
            if (letter.isUnread()) {
                return true;
            }
        }
        return false;
    }

    public final int getUnreadCount() {
        int unreadCount = 0;
        for (Letter letter : mails.values()) {
            if (letter.isUnread()) {
                unreadCount++;
            }
        }
        return unreadCount;
    }

    public boolean haveUnreadByType(LetterType letterType) {
        for (Letter letter : mails.values()) {
            if (letter.isUnread() && letter.getLetterType() == letterType) {
                return true;
            }
        }
        return false;
    }

    public final int getUnreadCountByType(LetterType letterType) {
        int count = 0;
        for (Letter letter : mails.values()) {
            if (letter.isUnread() && letter.getLetterType() == letterType) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return
     */
    public boolean haveFreeSlots() {
        return mails.size() < 100;
    }

    /**
     * @param letterId
     */
    public void removeLetter(int letterId) {
        mails.remove(letterId);
        uploadReserveLetters();
    }

    /**
     * Current size of mailbox
     *
     * @return
     */
    public int size() {
        return mails.size();
    }

    public void uploadReserveLetters() {
        if (reserveMail.size() > 0 && haveFreeSlots()) {
            for (Letter letter : reserveMail.values()) {
                if (haveFreeSlots()) {
                    mails.put(letter.getObjectId(), letter);
                    reserveMail.remove(letter.getObjectId());
                } else {
                    break;
                }
            }
            MailService.getInstance().refreshMail(getOwner());
        }
    }

    public void sendMailList(boolean expressOnly) {
        owner.sendPck(new SM_MAIL_SERVICE(owner, getLetters(), expressOnly));
    }

    public Player getOwner() {
        return owner;
    }

	public boolean canSkipPostmanCooldown() {
		return skipPostmanCooldown;
	}

	public void setSkipPostmanCooldown(boolean skipPostmanCooldown) {
		this.skipPostmanCooldown = skipPostmanCooldown;
	}
}