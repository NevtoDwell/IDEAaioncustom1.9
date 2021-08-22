/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

/**
 * This class represents an announcement
 *
 * @author Divinity
 */
public class Announcement {

    private int id;
    private final String faction;
    private final String announce;
    private final String chatType;
    //Jenelli 02.03.2013: Теперь в базе лежит строка CronExpression, а не интервал.
    private final String delay;

    /**
     * Constructor without the ID of announcement
     *
     * @param announce
     * @param faction
     * @param chatType
     * @param delay
     */
    public Announcement(String announce, String faction, String chatType, String delay) {
        this.announce = announce;

        // Checking the right syntax
        if (!faction.equalsIgnoreCase("ELYOS") && !faction.equalsIgnoreCase("ASMODIANS")) {
            faction = "ALL";
        }

        this.faction = faction;
        this.chatType = chatType;
        this.delay = delay;
    }

    /**
     * Constructor with the ID of announcement
     *
     * @param id
     * @param announce
     * @param faction
     * @param chatType
     * @param delay
     */
    public Announcement(int id, String announce, String faction, String chatType, String delay) {
        this.id = id;
        this.announce = announce;

        // Checking the right syntax
        if (!faction.equalsIgnoreCase("ELYOS") && !faction.equalsIgnoreCase("ASMODIANS")) {
            faction = "ALL";
        }

        this.faction = faction;
        this.chatType = chatType;
        this.delay = delay;
    }

    /**
     * Return the id of the announcement In case of the id doesn't exist, return -1
     *
     * @return int - Announcement's id
     */
    public int getId() {
        if (id != 0) {
            return id;
        } else {
            return -1;
        }
    }

    /**
     * Return the announcement's text
     *
     * @return String - Announcement's text
     */
    public String getAnnounce() {
        return announce;
    }

    /**
     * Return the announcement's faction in string mode : - ELYOS - ASMODIANS - ALL
     *
     * @return String - Announcement's faction
     */
    public String getFaction() {
        return faction;
    }

    /**
     * Return the announcement's faction in Race enum mode : - Race.ELYOS - Race.ASMODIANS
     *
     * @return Race - Announcement's faction
     */
    public Race getFactionEnum() {
        if (faction.equalsIgnoreCase("ELYOS")) {
            return Race.ELYOS;
        } else if (faction.equalsIgnoreCase("ASMODIANS")) {
            return Race.ASMODIANS;
        }

        return null;
    }

    /**
     * Return the chatType in String mode (for the insert in database)
     *
     * @return String - Announcement's chatType
     */
    public String getType() {
        return chatType;
    }

    /**
     * Return the chatType with the ChatType Enum
     *
     * @return ChatType - Announcement's chatType
     */
    public ChatType getChatType() {
        if (chatType.equalsIgnoreCase("System")) {
            return ChatType.GOLDEN_YELLOW;
        } else if (chatType.equalsIgnoreCase("White")) {
            return ChatType.WHITE_CENTER;
        } else if (chatType.equalsIgnoreCase("Yellow")) {
            return ChatType.YELLOW_CENTER;
        } else if (chatType.equalsIgnoreCase("Shout")) {
            return ChatType.SHOUT;
        } else if (chatType.equalsIgnoreCase("Orange")) {
            return ChatType.GROUP_LEADER;
        } else {
            return ChatType.BRIGHT_YELLOW_CENTER;
        }
    }

    /**
     * Return the announcement's delay
     *
     * @return int - Announcement's delay
     */
    public String getDelay() {
        return delay;
    }
}
