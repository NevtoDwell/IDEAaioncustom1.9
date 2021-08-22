/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.main;

import com.ne.commons.configuration.Property;

public final class CustomConfig {

    /**
     * Show premium account details on login
     */
    @Property(key = "gameserver.premium.notify", defaultValue = "false")
    public static boolean PREMIUM_NOTIFY;

    /**
     * Enable announce when a player succes enchant item 15
     */
    @Property(key = "gameserver.enchant.announce.enable", defaultValue = "true")
    public static boolean ENABLE_ENCHANT_ANNOUNCE;

    /**
     * Enable speaking between factions
     */
    @Property(key = "gameserver.chat.factions.enable", defaultValue = "false")
    public static boolean SPEAKING_BETWEEN_FACTIONS;

    /**
     * Minimum level to use whisper
     */
    @Property(key = "gameserver.chat.whisper.level", defaultValue = "10")
    public static int LEVEL_TO_WHISPER;

    /**
     * Factions search mode
     */
    @Property(key = "gameserver.search.factions.mode", defaultValue = "false")
    public static boolean FACTIONS_SEARCH_MODE;

    /**
     * list gm when search players
     */
    @Property(key = "gameserver.search.gm.list", defaultValue = "false")
    public static boolean SEARCH_GM_LIST;

    /**
     * Minimum level to use search
     */
    @Property(key = "gameserver.search.player.level", defaultValue = "10")
    public static int LEVEL_TO_SEARCH;

    /**
     * Allow opposite factions to bind in enemy territories
     */
    @Property(key = "gameserver.cross.faction.binding", defaultValue = "false")
    public static boolean ENABLE_CROSS_FACTION_BINDING;

    /**
     * Enable second class change without quest
     */
    @Property(key = "gameserver.simple.secondclass.enable", defaultValue = "false")
    public static boolean ENABLE_SIMPLE_2NDCLASS;

    /**
     * Disable chain trigger rate (chain skill with 100% success)
     */
    @Property(key = "gameserver.skill.chain.triggerrate", defaultValue = "true")
    public static boolean SKILL_CHAIN_TRIGGERRATE;

    /**
     * Base Fly Time
     */
    @Property(key = "gameserver.base.flytime", defaultValue = "60")
    public static int BASE_FLYTIME;

    /**
     * Reset Dp Time
     */
    @Property(key = "gameserver.dp.reset.time", defaultValue = "300")
    public static int DP_RESET_TIME;

    /**
     * Disable prevention using old names with coupon & command
     */
    @Property(key = "gameserver.oldnames.coupon.disable", defaultValue = "false")
    public static boolean OLD_NAMES_COUPON_DISABLED;
    @Property(key = "gameserver.oldnames.command.disable", defaultValue = "true")
    public static boolean OLD_NAMES_COMMAND_DISABLED;

    /**
     * Friendlist size
     */
    @Property(key = "gameserver.friendlist.size", defaultValue = "90")
    public static int FRIENDLIST_SIZE;

    /**
     * Basic Quest limit size
     */
    @Property(key = "gameserver.basic.questsize.limit", defaultValue = "40")
    public static int BASIC_QUEST_SIZE_LIMIT;

    /**
     * Npc Cube Expands limit size
     */
    @Property(key = "gameserver.npcexpands.limit", defaultValue = "5")
    public static int NPC_CUBE_EXPANDS_SIZE_LIMIT;

    /**
     * Enable instances
     */
    @Property(key = "gameserver.instances.enable", defaultValue = "true")
    public static boolean ENABLE_INSTANCES;

    /**
     * Enable instances mob always aggro player ignore level
     */
    @Property(key = "gameserver.instances.mob.aggro", defaultValue = "300080000,300090000,300060000")
    public static String INSTANCES_MOB_AGGRO;

    /**
     * Enable instances cooldown filtring
     */
    @Property(key = "gameserver.instances.cooldown.filter", defaultValue = "0")
    public static String INSTANCES_COOL_DOWN_FILTER;

    /**
     * Instances formula
     */
    @Property(key = "gameserver.instances.cooldown.rate", defaultValue = "1")
    public static int INSTANCES_RATE;

    /**
     * Enable Kinah cap
     */
    @Property(key = "gameserver.enable.kinah.cap", defaultValue = "false")
    public static boolean ENABLE_KINAH_CAP;

    /**
     * Kinah cap value
     */
    @Property(key = "gameserver.kinah.cap.value", defaultValue = "999999999")
    public static long KINAH_CAP_VALUE;

    /**
     * Enable AP cap
     */
    @Property(key = "gameserver.enable.ap.cap", defaultValue = "false")
    public static boolean ENABLE_AP_CAP;

    /**
     * AP cap value
     */
    @Property(key = "gameserver.ap.cap.value", defaultValue = "1000000")
    public static long AP_CAP_VALUE;

    @Property(key = "gameserver.enable.exp.cap", defaultValue = "false")
    public static boolean ENABLE_EXP_CAP;

    @Property(key = "gameserver.exp.cap.value", defaultValue = "48000000")
    public static long EXP_CAP_VALUE;
    /**
     * Enable no AP in mentored group.
     */
    @Property(key = "gameserver.noap.mentor.group", defaultValue = "false")
    public static boolean MENTOR_GROUP_AP;

    /**
     * /** Show dialog id and quest id
     */
    @Property(key = "gameserver.dialog.showid", defaultValue = "true")
    public static boolean ENABLE_SHOW_DIALOGID;

    /**
     * Custom RiftLevels for Heiron and Beluslan
     */
    @Property(key = "gameserver.rift.heiron_fm", defaultValue = "50")
    public static int HEIRON_FM;
    @Property(key = "gameserver.rift.heiron_gm", defaultValue = "50")
    public static int HEIRON_GM;
    @Property(key = "gameserver.rift.beluslan_fm", defaultValue = "50")
    public static int BELUSLAN_FM;
    @Property(key = "gameserver.rift.beluslan_gm", defaultValue = "50")
    public static int BELUSLAN_GM;

    @Property(key = "gameserver.reward.service.enable", defaultValue = "false")
    public static boolean ENABLE_REWARD_SERVICE;

    /**
     * Limits Config
     */
    @Property(key = "gameserver.limits.enable", defaultValue = "true")
    public static boolean LIMITS_ENABLED;

    @Property(key = "gameserver.limits.update", defaultValue = "0 0 0 * * ?")
    public static String LIMITS_UPDATE;

    @Property(key = "gameserver.limits.rate", defaultValue = "1")
    public static int LIMITS_RATE;

    @Property(key = "gameserver.abyssxform.afterlogout", defaultValue = "false")
    public static boolean ABYSSXFORM_LOGOUT;

    @Property(key = "gameserver.ride.restriction.enable", defaultValue = "true")
    public static boolean ENABLE_RIDE_RESTRICTION;

    /**
     * Items Config
     * Kisk spawn time
     */
    @Property(key = "gameserver.item.toypetspawn.action.time", defaultValue = "10000")
    public static int TOYPETSPAWN_ACTION_TIME;
    /**
     * Can player install new kisk before prevision despawned?
     */
    @Property(key = "gameserver.item.toypetspawn.enable.new.kisk.spawn", defaultValue = "false")
    public static boolean TOYPETSPAWN_NEW_KISK_SPAWN_ENABLE;

    @Property(key = "gameserver.arena.ticketcheck", defaultValue = "false")
    public static boolean ARENA_TICKET_CHECK;
    
    @Property(key = "gameserver.arena-of-chaos.ticketcheck", defaultValue = "false")
    public static boolean ARENA_OF_CHAOS_TICKET_CHECK;
    
    @Property(key = "gameserver.arena-of-chaos.players.size", defaultValue = "10")
    public static int ARENA_OF_CHAOS_PLAYERS_SIZE;
    
    @Property(key = "gameserver.pvp-solo-arena.ticketcheck", defaultValue = "false")
    public static boolean PVP_SOLO_ARENA_TICKET_CHECK;

    /**
     * Decompose time
     */
    @Property(key = "gameserver.item.decompose.action.time", defaultValue = "1")
    public static int DECOMPOSE_ACTION_TIME;

    @Property(key = "gameserver.arena.time", defaultValue =
        "0 0 10 ? * SAT <> 0 0 23 ? * SAT;" +
            "0 0 0 ? * SAT <> 0 0 1 ? * SAT;" +
            "0 0 10 ? * SUN <> 0 0 23 ? * SUN;" +
            "0 0 0 ? * SUN <> 0 0 1 ? * SUN;" +
            "0 0 12 ? * MON-FRI <> 0 0 13 ? * MON-FRI;" +
            "0 0 18 ? * MON-FRI <> 0 0 23 ? * MON-FRI;" +
            "0 0 0 ? * MON-FRI <> 0 0 1 ? * MON-FRI;")
    public static String ARENA_TIME;
    
    @Property(key = "gameserver.online.bonuses.enabled", defaultValue = "false")
    public static boolean ONLINE_BONUSES_ENABLED;
    
    @Property(key = "gameserver.online.bonuses.lifetime", defaultValue = "5")
    public static int ONLINE_BONUSES_LIFETIME;
    
    @Property(key = "gameserver.items.time.override", defaultValue = "")
    public static String ITEMS_TIME_OVERRIDE;
    
    @Property(key = "gameserver.keys.with.original.chance", defaultValue = "")
    public static String KEYS_WITH_ORIGINAL_CHANCE;


    @Property(key = "gameserver.secondclasslevel", defaultValue = "0")
    public static int SECONDCLASS_LEVEL;

    @Property(key = "gameserver.secondclass.equests", defaultValue = "")
    public static int[] SECONDCLASS_EQUESTS;

    @Property(key = "gameserver.secondclass.aquests", defaultValue = "")
    public static int[] SECONDCLASS_AQUESTS;

    @Property(key = "gameserver.secondclass.eskills", defaultValue = "")
    public static String SECONDCLASS_ESKILLS;

    @Property(key = "gameserver.secondclass.askills", defaultValue = "")
    public static String SECONDCLASS_ASKILLS;

    @Property(key = "gameserver.events.enable", defaultValue = "false")
    public static boolean EVENT_MODE;
}
