/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.skill;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import com.google.common.collect.ImmutableList;

import com.ne.commons.utils.collections.CopyOnWriteMap;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.Stigma.StigmaSkill;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_LIST;

/**
 * @author IceReaper, orfeo087, Avol, AEJTester
 */
public final class PlayerSkillList implements SkillList<Player> {

    private static final List<Integer> craftingQuests = Collections.unmodifiableList(Arrays.asList(30001, 30002, 30003, 40001, 40002, 40003, 40004, 40007, 40008, 40010));
    private static final List<Integer> craftingQuestsRefreshLevels = Collections.unmodifiableList(Arrays.asList(99, 199, 299, 399, 449, 549));

    private final Map<Integer, PlayerSkillEntry> skills = CopyOnWriteMap.of();

    private final List<PlayerSkillEntry> deletedSkills = new CopyOnWriteArrayList<>();

    public PlayerSkillList() {
    }

    public PlayerSkillList(Map<Integer, PlayerSkillEntry> skills) {
        this.skills.putAll(skills);
    }

    public List<PlayerSkillEntry> getAllSkills() {
        return ImmutableList.copyOf(skills.values());
    }

    public List<PlayerSkillEntry> getDeletedSkills() {
        return ImmutableList.copyOf(deletedSkills);
    }

    public PlayerSkillEntry getSkillEntry(int skillId) {
        return skills.get(skillId);
    }

    @Override
    public boolean addSkill(Player player, int skillId, int skillLevel) {
        return add(player, skillId, skillLevel, PersistentState.NEW);
    }

    /**
     * Add temporary skill which will not be saved in db
     *
     * @param player
     * @param skillId
     * @param skillLevel
     * @return
     */
    public boolean addTemporarySkill(Player player, int skillId, int skillLevel) {
        return add(player, skillId, skillLevel, PersistentState.NOACTION);
    }

    public void addStigmaSkill(Player player, List<StigmaSkill> skills, boolean equipedByNpc) {
        for (StigmaSkill sSkill : skills) {
            PlayerSkillEntry skill = new PlayerSkillEntry(sSkill.getSkillId(), true, sSkill.getSkillLvl(), PersistentState.NOACTION);
            this.skills.put(sSkill.getSkillId(), skill);
            if (equipedByNpc) {
                player.sendPck(new SM_SKILL_LIST(skill, 1300401, false));
            }
        }
    }

    private synchronized boolean add(Player player, int skillId, int skillLevel, PersistentState state) {
        PlayerSkillEntry existingSkill = skills.get(skillId);
        boolean isNew = false;
        if (existingSkill != null) {
            if (existingSkill.getSkillLevel() >= skillLevel) {
                return false;
            }

            existingSkill.setSkillLvl(skillLevel);
        } else {
            existingSkill = new PlayerSkillEntry(skillId, false, skillLevel, state);
            skills.put(skillId, existingSkill);
            isNew = true;
        }
        if (player.isSpawned()) {
            sendMessage(player, skillId, isNew);
            player.getController().updatePassiveStats();
        }

        tryUpdateCraftSkillsQuest(player, skillId, existingSkill.getSkillLevel());

        return true;
    }

    /**
     * @param player
     * @param skillId
     * @param xpReward
     *
     * @return
     */
    public boolean addSkillXp(Player player, int skillId, int xpReward, int objSkillPoints) {
        PlayerSkillEntry skillEntry = getSkillEntry(skillId);
        int maxDiff = 40;
        int SkillLvlDiff = skillEntry.getSkillLevel() - objSkillPoints;
        if (maxDiff < SkillLvlDiff) {
            return false;
        }
        switch (skillEntry.getSkillId()) {
            case 30001:
                if (skillEntry.getSkillLevel() == 49) {
                    return false;
                }
            case 30002:
            case 30003:
                if (skillEntry.getSkillLevel() == 449) {
                    break;
                }
            case 40001:
            case 40002:
            case 40003:
            case 40004:
            case 40007:
            case 40008:
            case 40010:
                switch (skillEntry.getSkillLevel()) {
                    case 99:
                    case 199:
                    case 299:
                    case 399:
                    case 449:
                    case 499:
                    case 549:
                        return false;
                }
                player.getRecipeList().autoLearnRecipe(player, skillId, skillEntry.getSkillLevel());
        }
        boolean updateSkill = skillEntry.addSkillXp(xpReward);
        if (updateSkill) {
            sendMessage(player, skillId, false);
        }

        tryUpdateCraftSkillsQuest(player, skillId, skillEntry.getSkillLevel());

        return true;
    }


    private void tryUpdateCraftSkillsQuest(Player player, int skill, int skillLvl){
        if(craftingQuests.contains(skill) && craftingQuestsRefreshLevels.contains(skillLvl))
            player.getController().updateNearbyQuests();
    }

    @Override
    public boolean isSkillPresent(int skillId) {
        return skills.containsKey(skillId);
    }

    @Override
    public int getSkillLevel(int skillId) {
        return skills.get(skillId).getSkillLevel();
    }

    @Override
    public synchronized boolean removeSkill(int skillId) {
        PlayerSkillEntry entry = skills.get(skillId);
        if (entry != null) {
            entry.setPersistentState(PersistentState.DELETED);
            deletedSkills.add(entry);
            skills.remove(skillId);
        }
        return entry != null;
    }

    @Override
    public int size() {
        return skills.size();
    }

    /**
     * @param player
     * @param skillId
     */
    private void sendMessage(Player player, int skillId, boolean isNew) {
        switch (skillId) {
            case 30001:
            case 30002:
                player.sendPck(new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330005, false));
                break;
            case 30003:
                player.sendPck(new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330005, false));
                break;
            case 40001:
            case 40002:
            case 40003:
            case 40004:
            case 40005:
            case 40006:
            case 40007:
            case 40008:
            case 40009:
            case 40010:
                player.sendPck(new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330061, false));
                break;
            default:
                player.sendPck(new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1300050, isNew));
        }
    }
}
