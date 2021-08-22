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
import java.util.Calendar;

import com.ne.gs.database.GDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.GameServer;
import com.ne.gs.configs.main.AdvCustomConfig;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.configs.main.ShivaConfig;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.Gender;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.templates.BoundRadius;
import com.ne.gs.model.templates.VisibleObjectTemplate;
import com.ne.gs.network.aion.serverpackets.SM_STATUPDATE_EXP;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.stats.XPLossEnum;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;

/**
 * This class is holding base information about player, that may be used even when player itself is not online.
 *
 * @author Luno
 * @modified cura
 */
public class PlayerCommonData extends VisibleObjectTemplate {

    static Logger log = LoggerFactory.getLogger(PlayerCommonData.class);

    private final int playerObjId;
    private Race race;
    private String name;
    private PlayerClass playerClass;
    /**
     * Should be changed right after character creation *
     */
    private int level = 0;
    private long exp = 0;
    private long expRecoverable = 0;
    private Gender gender;
    private Timestamp lastOnline = new Timestamp(Calendar.getInstance().getTime().getTime() - 20000);
    private boolean online;
    private String note;
    private WorldPosition position;
    private int questExpands = 0;
    private int npcExpands = AdvCustomConfig.CUBE_SIZE;
    private int warehouseSize = 0;
    private int titleId = -1;
    private int mailboxLetters;
    private int soulSickness = 0;
    private boolean noExp = false;
    private long reposteCurrent;
    private long reposteMax;
    private long salvationPoint;
    private int mentorFlagTime;
    private int worldOwnerId;
    private BoundRadius boundRadius;
    private FriendList friendList;

    private long lastTransferTime;

    public static PlayerCommonData get(Integer objectId) {
        Player player = World.getInstance().findPlayer(objectId);
        if (player == null) {
            return GDB.get(PlayerDAO.class).loadPlayerCommonData(objectId);
        }
        return player.getCommonData();
    }

    // TODO: Move all function to playerService or Player class.
    public PlayerCommonData(int objId) {
        playerObjId = objId;
    }

    public int getPlayerObjId() {
        return playerObjId;
    }

    public long getExp() {
        return exp;
    }

    public int getQuestExpands() {
        return questExpands;
    }

    public void setQuestExpands(int questExpands) {
        this.questExpands = questExpands;
    }

    public void setNpcExpands(int npcExpands) {
        this.npcExpands = npcExpands;
    }

    public int getNpcExpands() {
        return npcExpands;
    }

    public long getExpShown() {
        return exp - DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(level);
    }

    public long getExpNeed() {
        if (level == DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel()) {
            return 0;
        }
        return DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(level + 1) - DataManager.PLAYER_EXPERIENCE_TABLE
                                                                                               .getStartExpForLevel(level);
    }

    /**
     * calculate the lost experience must be called before setexp
     *
     * @author Jangan
     */
    public void calculateExpLoss() {
        long expLost = XPLossEnum.getExpLoss(level, getExpNeed());

        int unrecoverable = (int) (expLost * 0.33333333);
        int recoverable = (int) expLost - unrecoverable;
        long allExpLost = recoverable + expRecoverable;

        if (getExpShown() > unrecoverable) {
            exp = exp - unrecoverable;
        } else {
            exp = exp - getExpShown();
        }
        if (getExpShown() > recoverable) {
            expRecoverable = allExpLost;
            exp = exp - recoverable;
        } else {
            expRecoverable = expRecoverable + getExpShown();
            exp = exp - getExpShown();
        }

        if (expRecoverable > getExpNeed() * 0.25D) {
            expRecoverable = Math.round(getExpNeed() * 0.25D);
        }

        if (getPlayer() != null) {
            getPlayer().sendPck(new SM_STATUPDATE_EXP(getExpShown(), getExpRecoverable(), getExpNeed(), getCurrentReposteEnergy(),
                getMaxReposteEnergy()));
        }
    }

    public void setRecoverableExp(long expRecoverable) {
        this.expRecoverable = expRecoverable;
    }

    public void resetRecoverableExp() {
        long el = expRecoverable;
        expRecoverable = 0;
        setExp(exp + el);
    }

    public long getExpRecoverable() {
        return expRecoverable;
    }

    /**
     * @param value
     */
    public void addExp(long value, int npcNameId) {
        this.addExp(value, null, npcNameId, "");
    }

    public void addExp(long value, RewardType rewardType) {
        this.addExp(value, rewardType, 0, "");
    }

    public void addExp(long value, RewardType rewardType, int npcNameId) {
        this.addExp(value, rewardType, npcNameId, "");
    }

    public void addExp(long value, RewardType rewardType, String name) {
        this.addExp(value, rewardType, 0, name);
    }

    public void addExp(long value, RewardType rewardType, int npcNameId, String name) {
        if (noExp) {
            return;
        }

        if (CustomConfig.ENABLE_EXP_CAP) {
            value = value > CustomConfig.EXP_CAP_VALUE ? CustomConfig.EXP_CAP_VALUE : value;
        }
        long reward = value;
        if (getPlayer() != null && rewardType != null) {
            reward = rewardType.calcReward(getPlayer(), value);
        }

        long repose = 0;
        if (isReadyForReposteEnergy() && getCurrentReposteEnergy() > 0) {
            repose = (long) (reward / 100f * 40); // 40% bonus
            addReposteEnergy(-repose);
        }

        long salvation = 0;
        if (isReadyForSalvationPoints() && getCurrentSalvationPercent() > 0) {
            salvation = (long) (reward / 100f * getCurrentSalvationPercent());
            // TODO! remove salvation points?
        }

        reward += repose + salvation;
        setExp(exp + reward);
        if (getPlayer() != null) {
            if (rewardType != null) {
                switch (rewardType) {
                    case GROUP_HUNTING:
                    case HUNTING:
                    case QUEST:
                        if (npcNameId == 0) {
                            // You have gained %num1 XP.
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP2(reward));
                        } else if (repose > 0 && salvation > 0) {
                            // You have gained %num1 XP from %0 (Energy of Repose %num2, Energy of Salvation %num3).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP_VITAL_MAKEUP_BONUS_DESC(DescId.of(npcNameId * 2 + 1), reward, repose, salvation));
                        } else if (repose > 0 && salvation == 0) {
                            // You have gained %num1 XP from %0 (Energy of Repose %num2).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP_VITAL_BONUS_DESC(DescId.of(npcNameId * 2 + 1), reward, repose));
                        } else if (repose == 0 && salvation > 0) {
                            // You have gained %num1 XP from %0 (Energy of Salvation %num2).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP_MAKEUP_BONUS_DESC(DescId.of(npcNameId * 2 + 1), reward, salvation));
                        } else {
                            // You have gained %num1 XP from %0.
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP_DESC(DescId.of(npcNameId * 2 + 1), reward));
                        }
                        break;
                    case PVP_KILL:
                        if (repose > 0 && salvation > 0) {
                            // You have gained %num1 XP from %0 (Energy of Repose %num2, Energy of Salvation %num3).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP_VITAL_MAKEUP_BONUS(name, reward, repose, salvation));
                        } else if (repose > 0 && salvation == 0) {
                            // You have gained %num1 XP from %0 (Energy of Repose %num2).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP_VITAL_BONUS(name, reward, repose));
                        } else if (repose == 0 && salvation > 0) {
                            // You have gained %num1 XP from %0 (Energy of Salvation %num2).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP_MAKEUP_BONUS(name, reward, salvation));
                        } else {
                            // You have gained %num1 XP from %0.
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP(name, reward));
                        }
                        break;
                    case CRAFTING:
                    case GATHERING:
                        if (repose > 0 && salvation > 0) {
                            // You have gained %num1 XP(Energy of Repose %num2, Energy of Salvation %num3).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP2_VITAL_MAKEUP_BONUS(reward, repose, salvation));
                        } else if (repose > 0 && salvation == 0) {
                            // You have gained %num1 XP(Energy of Repose %num2).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP2_VITAL_BONUS(reward, repose));
                        } else if (repose == 0 && salvation > 0) {
                            // You have gained %num1 XP(Energy of Salvation %num2).
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP2_MAKEUP_BONUS(reward, salvation));
                        } else {
                            // You have gained %num1 XP.
                            getPlayer().sendPck(SM_SYSTEM_MESSAGE.STR_GET_EXP2(reward));
                        }
                        break;
                }
            }
        }
    }

    public boolean isReadyForSalvationPoints() {
        return level >= 15 && level < GSConfig.PLAYER_MAX_LEVEL + 1;
    }

    public boolean isReadyForReposteEnergy() {
        return level >= 10;
    }

    public void addReposteEnergy(long add) {
        if (!isReadyForReposteEnergy()) {
            return;
        }

        reposteCurrent += add;
        if (reposteCurrent < 0) {
            reposteCurrent = 0;
        } else if (reposteCurrent > getMaxReposteEnergy()) {
            reposteCurrent = getMaxReposteEnergy();
        }
    }

    public void updateMaxReposte() {
        if (!isReadyForReposteEnergy()) {
            reposteCurrent = 0;
            reposteMax = 0;
        } else {
            reposteMax = (long) (getExpNeed() * 0.25f); // Retail 99%
        }
    }

    public void setCurrentReposteEnergy(long value) {
        reposteCurrent = value;
    }

    public long getCurrentReposteEnergy() {
        return isReadyForReposteEnergy() ? reposteCurrent : 0;
    }

    public long getMaxReposteEnergy() {
        return isReadyForReposteEnergy() ? reposteMax : 0;
    }

    /**
     * sets the exp value
     */
    public void setExp(long exp) {
        // maxLevel is 56 but in game 55 should be shown with full XP bar
        int maxLevel = DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel();

        if (getPlayerClass() != null && getPlayerClass().isStartingClass()) {
            maxLevel = ShivaConfig.STARTING_LEVEL > ShivaConfig.STARTCLASS_MAXLEVEL ? ShivaConfig.STARTING_LEVEL : ShivaConfig.STARTCLASS_MAXLEVEL;
        }

        long maxExp = DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(maxLevel);

        if (exp > maxExp) {
            exp = maxExp;
        }

        int oldLvl = level;
        this.exp = exp;
        // make sure level is never larger than maxLevel-1
        boolean up = false;
        while (level + 1 < maxLevel && (up = exp >= DataManager.PLAYER_EXPERIENCE_TABLE
                                                               .getStartExpForLevel(level + 1)) || level - 1 >= 0
            && exp < DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(level)) {
            if (up) {
                level++;
            } else {
                level--;
            }

            upgradePlayerData();
        }

        if (getPlayer() != null) {
            if (up && GSConfig.ENABLE_RATIO_LIMITATION) {
                if (level >= GSConfig.RATIO_MIN_REQUIRED_LEVEL && getPlayer().getPlayerAccount()
                    .getNumberOf(getRace()) == 1) {
                    GameServer.updateRatio(getRace(), 1);
                }

                if (level >= GSConfig.RATIO_MIN_REQUIRED_LEVEL && getPlayer().getPlayerAccount()
                    .getNumberOf(getRace()) == 1) {
                    GameServer.updateRatio(getRace(), -1);
                }
            }
            if (oldLvl != level) {
                updateMaxReposte();
            }

            getPlayer().sendPck(new SM_STATUPDATE_EXP(getExpShown(), getExpRecoverable(), getExpNeed(), getCurrentReposteEnergy(),
                getMaxReposteEnergy()));
        }
    }

    private void upgradePlayerData() {
        Player player = getPlayer();
        if (player != null) {
            player.getController().upgradePlayer();
            resetSalvationPoints();
        }
    }

    public void setNoExp(boolean value) {
        noExp = value;
    }

    public boolean getNoExp() {
        return noExp;
    }

    /**
     * @return Race as from template
     */
    public final Race getRace() {
        return race;
    }

    public Race getOppositeRace() {
        return race == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS;
    }

    /**
     * @return the mentorFlagTime
     */
    public int getMentorFlagTime() {
        return mentorFlagTime;
    }

    public boolean isHaveMentorFlag() {
        return mentorFlagTime > System.currentTimeMillis() / 1000;
    }

    /**
     * @param mentorFlagTime
     *     the mentorFlagTime to set
     */
    public void setMentorFlagTime(int mentorFlagTime) {
        this.mentorFlagTime = mentorFlagTime;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public WorldPosition getPosition() {
        return position;
    }

    public Timestamp getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Timestamp timestamp) {
        lastOnline = timestamp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level <= DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel()) {
            setExp(DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(level));
        }
    }

    /**
     * ATTENTION: Only for tests
     *
     * @param level
     *     Level
     */
    public void setLevelDirect(int level) {
        this.level = level;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    /**
     * This method should be called exactly once after creating object of this class
     */
    public void setPosition(WorldPosition position) {
        if (this.position != null) {
            throw new IllegalStateException("position already set");
        }
        this.position = position;
    }

    /**
     * Gets the cooresponding Player for this common data. Returns null if the player is not online
     *
     * @return Player or null
     */
    public Player getPlayer() {
        if (online && getPosition() != null) {
            return World.getInstance().findPlayer(playerObjId);
        }
        return null;
    }

    @Override
    public int getTemplateId() {
        return 100000 + race.getRaceId() * 2 + gender.getGenderId();
    }

    @Override
    public int getNameId() {
        return 0;
    }

    /**
     * @param warehouseSize
     *     the warehouseSize to set
     */
    public void setWarehouseSize(int warehouseSize) {
        this.warehouseSize = warehouseSize;
    }

    /**
     * @return the warehouseSize
     */
    public int getWarehouseSize() {
        return warehouseSize;
    }

    public void setMailboxLetters(int count) {
        mailboxLetters = count;
    }

    public int getMailboxLetters() {
        return mailboxLetters;
    }

    /**
     * @param boundRadius
     */
    public void setBoundingRadius(BoundRadius boundRadius) {
        this.boundRadius = boundRadius;
    }

    @Override
    public BoundRadius getBoundRadius() {
        return boundRadius;
    }

    public void setDeathCount(int count) {
        soulSickness = count;
    }

    public int getDeathCount() {
        return soulSickness;
    }

    /**
     * Value returned here means % of exp bonus.
     */
    public byte getCurrentSalvationPercent() {
        if (salvationPoint <= 0) {
            return 0;
        }

        long per = salvationPoint / 1000;
        if (per > 30) {
            return 30;
        }

        return (byte) per;
    }

    public void addSalvationPoints(long points) {
        salvationPoint += points;
    }

    public void resetSalvationPoints() {
        salvationPoint = 0;
    }

    public void setLastTransferTime(long value) {
        lastTransferTime = value;
    }

    public long getLastTransferTime() {
        return lastTransferTime;
    }

    public int getWorldOwnerId() {
        return worldOwnerId;
    }

    public void setWorldOwnerId(int worldOwnerId) {
        this.worldOwnerId = worldOwnerId;
    }

    public FriendList getFriendList() {
        return friendList;
    }

    public void setFriendList(FriendList list) {
        friendList = list;
    }
}
