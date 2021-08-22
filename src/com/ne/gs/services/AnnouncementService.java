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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javolution.util.FastSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.commons.services.CronService;
import com.ne.gs.database.dao.AnnouncementsDAO;
import com.ne.gs.model.Announcement;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_MESSAGE;
import com.ne.gs.world.World;

/**
 * Automatic Announcement System
 *
 * @author Divinity
 */
public class AnnouncementService {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

    private Collection<Announcement> announcements;
    //private final List<Future<?>> delays = new ArrayList<Future<?>>();
    private final List<Runnable> announceRunnableList = new ArrayList<>();

    private AnnouncementService() {
        load();
    }

    public static AnnouncementService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Reload the announcements system
     */
    public void reload() {
        // Cancel all tasks
        if (announceRunnableList != null && announceRunnableList.size() > 0) {
            for (Runnable runnable : announceRunnableList) {
                CronService.getInstance().cancel(runnable);
            }
        }
        //		if (delays != null && delays.size() > 0) {
        //			for (final Future<?> delay : delays) {
        //				delay.cancel(false);
        //			}
        //		}

        // Clear all announcements
        announcements.clear();

        // And load again all announcements
        load();
    }

    /**
     * Load the announcements system
     * Jenelli 02.03.3013 Добавлено использование CronExpression
     */
    private void load() {
        announcements = new FastSet<>(getDAO().getAnnouncements()).shared();

        for (final Announcement announce : announcements) {
            Runnable announceRunnable = new Runnable() {

                @Override
                public void run() {
                    Iterator<Player> iter = World.getInstance().getPlayersIterator();
                    while (iter.hasNext()) {
                        Player player = iter.next();

                        if (announce.getFaction().equalsIgnoreCase("ALL")) {
                            if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER) {
                                player.sendPck(new SM_MESSAGE(1, "Announcement", announce.getAnnounce(), announce.getChatType()));
                            } else {
                                player.sendPck(new SM_MESSAGE(1, "Announcement", "Announcement: " + announce.getAnnounce(), announce.getChatType()));
                            }
                        } else if (announce.getFactionEnum() == player.getRace()) {
                            if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER) {
                                AionServerPacket packet = new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian")
                                    + " Announcement", announce.getAnnounce(), announce.getChatType());
                                player.sendPck(packet);
                            } else {
                                AionServerPacket packet = new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian")
                                    + " Announcement", (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Announcement: "
                                    + announce.getAnnounce(), announce.getChatType());
                                player.sendPck(packet);
                            }
                        }
                    }
                }
            };
            CronService.getInstance().schedule(announceRunnable, announce.getDelay());
            announceRunnableList.add(announceRunnable);

            //			delays.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            //
            //				@Override
            //				public void run() {
            //					final Iterator<Player> iter = World.getInstance().getPlayersIterator();
            //					while (iter.hasNext()) {
            //						final Player player = iter.next();
            //
            //						if (announce.getFaction().equalsIgnoreCase("ALL")) {
            //							if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER) {
            //                                player.sendPck(new SM_MESSAGE(1, "Announcement", announce.getAnnounce(), announce.getChatType()));
            //                            } else {
            //                                player.sendPck(new SM_MESSAGE(1, "Announcement", "Announcement: " + announce.getAnnounce(), announce.getChatType()));
            //                            }
            //						} else if (announce.getFactionEnum() == player.getRace()) {
            //							if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER) {
            //                                final AionServerPacket packet = new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian")
            //                                        + " Announcement", announce.getAnnounce(), announce.getChatType());
            //                                player.sendPck(packet);
            //                            } else {
            //                                final AionServerPacket packet = new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian")
            //                                        + " Announcement", (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Announcement: "
            //                                        + announce.getAnnounce(), announce.getChatType());
            //                                player.sendPck(packet);
            //                            }
            //						}
            //					}
            //				}
            //			}, announce.getDelay() * 1000, announce.getDelay() * 1000));
        }

        log.info("Loaded " + announcements.size() + " announcements");
    }

    public void addAnnouncement(Announcement announce) {
        getDAO().addAnnouncement(announce);
    }

    public boolean delAnnouncement(int idAnnounce) {
        return getDAO().delAnnouncement(idAnnounce);
    }

    public Set<Announcement> getAnnouncements() {
        return getDAO().getAnnouncements();
    }

    private AnnouncementsDAO getDAO() {
        return GDB.get(AnnouncementsDAO.class);
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final AnnouncementService instance = new AnnouncementService();
    }
}
