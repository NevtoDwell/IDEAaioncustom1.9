/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.sql.Timestamp;

import com.ne.commons.Sys;
import com.ne.gs.model.IExpirable;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.toypet.PetAdoptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.PlayerPetsDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.templates.VisibleObjectTemplate;
import com.ne.gs.model.templates.pet.PetDopingBag;
import com.ne.gs.model.templates.pet.PetFunctionType;
import com.ne.gs.model.templates.pet.PetTemplate;
import com.ne.gs.services.toypet.PetFeedProgress;
import com.ne.gs.services.toypet.PetHungryLevel;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PetCommonData extends VisibleObjectTemplate implements IExpirable {

    private static final Logger _log = LoggerFactory.getLogger(PetCommonData.class);

    private int decoration;
    private String name;
    private final int petId;
    private Timestamp birthday;
    PetFeedProgress feedProgress = null;
    PetDopingBag dopingBag = null;
    private volatile boolean cancelFeed = false;
    private boolean feedingTime = true;
    private long curentTime;

    private final int petObjectId;
    private final int masterObjectId;

    private long startMoodTime;
    private int shuggleCounter;

    private int lastSentPoints;
    private long moodCdStarted;
    private long giftCdStarted;
    private Timestamp despawnTime;
    private boolean isLooting = false;
    private boolean isBuffing = false;

    private int lifeTime;

    public PetCommonData(int petId, int masterObjectId) {
        petObjectId = IDFactory.getInstance().nextId();
        this.petId = petId;
        this.masterObjectId = masterObjectId;
        PetTemplate template = DataManager.PET_DATA.getPetTemplate(petId);
        if (template == null) {
            _log.error("FIXME: PetTemplate for id " + petId + " does not exist, owner " + masterObjectId);
        } else {
            if (template.ContainsFunction(PetFunctionType.FOOD)) {
                int flavourId = template.getPetFunction(PetFunctionType.FOOD).getId();
                int lovedLimit = DataManager.PET_FEED_DATA.getFlavourById(flavourId).getLovedFoodLimit();
                feedProgress = new PetFeedProgress((byte) (lovedLimit & 0xFF));
            }
            if (template.ContainsFunction(PetFunctionType.DOPING)) {
                dopingBag = new PetDopingBag();
            }
        }
    }

    public final int getDecoration() {
        return decoration;
    }

    public final void setDecoration(int decoration) {
        this.decoration = decoration;
    }

    @Override
    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final int getPetId() {
        return petId;
    }

    public int getBirthday() {
        if (birthday == null) {
            return 0;
        }

        return (int) (birthday.getTime() / 1000);
    }

    public Timestamp getBirthdayTimestamp() {
        return birthday;
    }

    public void setBirthday(Timestamp birthday) {
        this.birthday = birthday;
    }

    public long getCurentTime() {
        return curentTime;
    }

    public void setCurentTime(long curentTime) {
        this.curentTime = curentTime;

    }

    public void setIsFeedingTime(boolean food) {
        feedingTime = food;
    }

    public boolean isFeedingTime() {
        return feedingTime;
    }

    public boolean getCancelFeed() {
        return cancelFeed;
    }

    public void setCancelFeed(boolean cancelFeed) {
        this.cancelFeed = cancelFeed;
    }

    /**
     * @param feedingTime
     */
    public void setFeedingTime(boolean feedingTime) {
        this.feedingTime = feedingTime;
    }

    public void setReFoodTime(long reFoodTime) {
        setFeedingTime(false);
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                feedingTime = true;
                curentTime = 0;
                feedProgress.setHungryLevel(PetHungryLevel.HUNGRY);
            }
        }, reFoodTime);
    }

    public long getTime() {
        long time = System.currentTimeMillis() - curentTime;
        if (time < 0 || time > 600000) {
            curentTime = 0;
            time = 0;
        }

        return 600000 - time == 600000 ? 0 : 600000 - time;
    }

    public int getObjectId() {
        return petObjectId;
    }

    public int getMasterObjectId() {
        return masterObjectId;
    }

    @Override
    public int getTemplateId() {
        return petId;
    }

    @Override
    public int getNameId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public final long getMoodStartTime() {
        return startMoodTime;
    }

    public final int getShuggleCounter() {
        return shuggleCounter;
    }

    public final void setShuggleCounter(int shuggleCounter) {
        this.shuggleCounter = shuggleCounter;
    }

    public final int getMoodPoints(boolean forPacket) {
        if (startMoodTime == 0) {
            startMoodTime = System.currentTimeMillis();
        }
        int points = Math.round((System.currentTimeMillis() - startMoodTime) / 1000f) + shuggleCounter * 1000;
        if (forPacket && points > 9000) {
            return 9000;
        }
        return points;
    }

    public final int getLastSentPoints() {
        return lastSentPoints;
    }

    public final void setLastSentPoints(int points) {
        lastSentPoints = points;
    }

    public final boolean increaseShuggleCounter() {
        if (getMoodRemainingTime() > 0) {
            return false;
        }
        moodCdStarted = System.currentTimeMillis();
        shuggleCounter++;
        return true;
    }

    public final void clearMoodStatistics() {
        startMoodTime = 0;
        shuggleCounter = 0;
    }

    public final void setStartMoodTime(long startMoodTime) {
        this.startMoodTime = startMoodTime;
    }

    /**
     * @return moodCdStarted
     */
    public long getMoodCdStarted() {
        return moodCdStarted;
    }

    /**
     * @param moodCdStarted
     *     the moodCdStarted to set
     */
    public void setMoodCdStarted(long moodCdStarted) {
        this.moodCdStarted = moodCdStarted;
    }

    public int getMoodRemainingTime() {
        long stop = moodCdStarted + 600000;
        long remains = stop - System.currentTimeMillis();
        if (remains <= 0) {
            setMoodCdStarted(0);
            return 0;
        }
        return (int) (remains / 1000);
    }

    /**
     * @return the giftCdStarted
     */
    public long getGiftCdStarted() {
        return giftCdStarted;
    }

    /**
     * @param giftCdStarted
     *     the giftCdStarted to set
     */
    public void setGiftCdStarted(long giftCdStarted) {
        this.giftCdStarted = giftCdStarted;
    }

    public int getGiftRemainingTime() {
        long stop = giftCdStarted + 3600 * 1000;
        long remains = stop - System.currentTimeMillis();
        if (remains <= 0) {
            setGiftCdStarted(0);
            return 0;
        }
        return (int) (remains / 1000);
    }

    /**
     * @return the despawnTime
     */
    public Timestamp getDespawnTime() {
        return despawnTime;
    }

    /**
     * @param despawnTime
     *     the despawnTime to set
     */
    public void setDespawnTime(Timestamp despawnTime) {
        this.despawnTime = despawnTime;
    }

    /**
     * Saves mood data to GDB
     */
    public void savePetMoodData() {
        GDB.get(PlayerPetsDAO.class).savePetMoodData(this);
    }

    public PetFeedProgress getFeedProgress() {
        return feedProgress;
    }

    public void setIsLooting(boolean isLooting) {
        this.isLooting = isLooting;
    }

    public boolean isLooting() {
        return isLooting;
    }

    public PetDopingBag getDopingBag() {
        return dopingBag;
    }

    public void setIsBuffing(boolean isBuffing) {
        this.isBuffing = isBuffing;
    }

    public boolean isBuffing() {
        return isBuffing;
    }

    @Override
    public int getExpireTime() {
        return getBirthday() + (getLifeTime() * 60);
    }

    @Override
    public void expireEnd(Player player) {
        if (player == null)
            return;

        player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_PET_ABANDON_EXPIRE_TIME_COMPLETE(name));
        PetAdoptionService.surrenderPet(player, petId);
    }

    @Override
    public boolean canExpireNow() {
        return true;
    }

    @Override
    public void expireMessage(Player player, int time) {
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public int getRemainingLifeTime() {
        if (getLifeTime() == 0) return 0;

        int deadline = getBirthday() + (getLifeTime() * 60);
        int secondsLeft = (int) (deadline - (Sys.millis() / 1000));
        secondsLeft = Math.max(0, secondsLeft);
        return secondsLeft;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }
}
