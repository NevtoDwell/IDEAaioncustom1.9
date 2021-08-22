/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.dao.PetitionDAO;
import com.ne.gs.model.Petition;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_PETITION;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;

/**
 * @author zdead
 */
public class PetitionService {

    private static final Logger log = LoggerFactory.getLogger(PetitionService.class);

    private static final SortedMap<Integer, Petition> registeredPetitions = new TreeMap<>();

    public static PetitionService getInstance() {
        return SingletonHolder.instance;
    }

    public PetitionService() {
        log.info("Loading PetitionService ...");
        Set<Petition> petitions = GDB.get(PetitionDAO.class).getPetitions();
        for (Petition p : petitions) {
            registeredPetitions.put(p.getPetitionId(), p);
        }
        log.info("Successfully loaded " + registeredPetitions.size() + " database petitions");
    }

    public Collection<Petition> getRegisteredPetitions() {
        return registeredPetitions.values();
    }

    public void deletePetition(int playerObjId) {
        Set<Petition> petitions = new HashSet<>();
        for (Petition p : registeredPetitions.values()) {
            if (p.getPlayerObjId() == playerObjId) {
                petitions.add(p);
            }
        }
        for (Petition p : petitions) {
            if (registeredPetitions.containsKey(p.getPetitionId())) {
                registeredPetitions.remove(p.getPetitionId());
            }
        }

        GDB.get(PetitionDAO.class).deletePetition(playerObjId);
        if (playerObjId > 0 && World.getInstance().findPlayer(playerObjId) != null) {
            Player p = World.getInstance().findPlayer(playerObjId);
            p.sendPck(new SM_PETITION());
        }
        rebroadcastPlayerData();
    }

    public void setPetitionReplied(int petitionId) {
        int playerObjId = registeredPetitions.get(petitionId).getPlayerObjId();
        GDB.get(PetitionDAO.class).setReplied(petitionId);
        registeredPetitions.remove(petitionId);
        rebroadcastPlayerData();
        if (playerObjId > 0 && World.getInstance().findPlayer(playerObjId) != null) {
            Player p = World.getInstance().findPlayer(playerObjId);
            p.sendPck(new SM_PETITION());
        }
    }

    public synchronized Petition registerPetition(Player sender, int typeId, String title, String contentText,
                                                  String additionalData) {
        int id = GDB.get(PetitionDAO.class).getNextAvailableId();
        Petition ptt = new Petition(id, sender.getObjectId(), typeId, title, contentText, additionalData, 0);
        GDB.get(PetitionDAO.class).insertPetition(ptt);
        registeredPetitions.put(ptt.getPetitionId(), ptt);
        broadcastMessageToGM(sender, ptt.getPetitionId());
        return ptt;
    }

    private void rebroadcastPlayerData() {
        for (Petition p : registeredPetitions.values()) {
            Player player = World.getInstance().findPlayer(p.getPlayerObjId());
            if (player != null) {
                player.sendPck(new SM_PETITION(p));
            }
        }
    }

    private void broadcastMessageToGM(Player sender, int petitionId) {
        Iterator<Player> players = World.getInstance().getPlayersIterator();
        while (players.hasNext()) {
            Player p = players.next();
            if (p.getAccessLevel() > 0) {
                PacketSendUtility.sendBrightYellowMessageOnCenter(p, "New Support Petition from: " + sender.getName() + " (#" + petitionId + ")");
            }
        }
    }

    public boolean hasRegisteredPetition(Player player) {
        return hasRegisteredPetition(player.getObjectId());
    }

    public boolean hasRegisteredPetition(int playerObjId) {
        boolean result = false;
        for (Petition p : registeredPetitions.values()) {
            if (p.getPlayerObjId() == playerObjId) {
                result = true;
            }
        }
        return result;
    }

    public Petition getPetition(int playerObjId) {
        for (Petition p : registeredPetitions.values()) {
            if (p.getPlayerObjId() == playerObjId) {
                return p;
            }
        }
        return null;
    }

    public synchronized int getNextAvailablePetitionId() {
        return 0;
    }

    public int getWaitingPlayers(int playerObjId) {
        int counter = 0;
        for (Petition p : registeredPetitions.values()) {
            if (p.getPlayerObjId() == playerObjId) {
                break;
            }
            counter++;
        }
        return counter;
    }

    public int calculateWaitTime(int playerObjId) {
        int timePerPetition = 15;
        int timeBetweenPetition = 30;
        int result = timeBetweenPetition;
        for (Petition p : registeredPetitions.values()) {
            if (p.getPlayerObjId() == playerObjId) {
                break;
            }
            result += timePerPetition;
            result += timeBetweenPetition;
        }
        return result;
    }

    public void onPlayerLogin(Player player) {
        if (hasRegisteredPetition(player)) {
            player.sendPck(new SM_PETITION(getPetition(player.getObjectId())));
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final PetitionService instance = new PetitionService();
    }

}
