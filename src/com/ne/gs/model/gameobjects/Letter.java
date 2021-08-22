/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import java.sql.Timestamp;

/**
 * @author kosyachok
 */
public class Letter extends AionObject {

    private final int recipientId;
    private Item attachedItem;
    private long attachedKinahCount;
    private final String senderName;
    private final String title;
    private final String message;
    private boolean unread;
    private boolean express;
    private final Timestamp timeStamp;
    private PersistentState persistentState;
    private LetterType letterType;

    /**
     * @param objId
     * @param attachedItem
     * @param title
     * @param message
     * @param senderName
     * @param timeStamp
     *     new letter constructor
     */
    public Letter(int objId, int recipientId, Item attachedItem, long attachedKinahCount, String title, String message,
                  String senderName, Timestamp timeStamp, boolean unread, LetterType letterType) {
        super(objId);

        if (letterType == LetterType.EXPRESS || letterType == LetterType.BLACKCLOUD) {
            express = true;
        } else {
            express = false;
        }
        this.recipientId = recipientId;
        this.attachedItem = attachedItem;
        this.attachedKinahCount = attachedKinahCount;
        this.title = title;
        this.message = message;
        this.senderName = senderName;
        this.timeStamp = timeStamp;
        this.unread = unread;
        persistentState = PersistentState.NEW;
        this.letterType = letterType;
    }

    @Override
    public String getName() {
        return String.valueOf(attachedItem.getItemTemplate().getNameId());
    }

    public int getRecipientId() {
        return recipientId;
    }

    public Item getAttachedItem() {
        return attachedItem;
    }

    public long getAttachedKinah() {
        return attachedKinahCount;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public LetterType getLetterType() {
        return letterType;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setReadLetter() {
        unread = false;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    public boolean isExpress() {
        return express;
    }

    public void setExpress(boolean express) {
        this.express = express;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    public void setLetterType(LetterType letterType) {
        this.letterType = letterType;
        if (letterType == LetterType.EXPRESS || letterType == LetterType.BLACKCLOUD) {
            express = true;
        } else {
            express = false;
        }
    }

    public PersistentState getLetterPersistentState() {
        return persistentState;
    }

    public void removeAttachedItem() {
        attachedItem = null;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    public void removeAttachedKinah() {
        attachedKinahCount = 0;
        persistentState = PersistentState.UPDATE_REQUIRED;
    }

    public void delete() {
        persistentState = PersistentState.DELETED;
    }

    public void setPersistState(PersistentState state) {
        persistentState = state;
    }

    /**
     * @return the timeStamp
     */
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
