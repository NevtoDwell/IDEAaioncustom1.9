/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import java.util.List;

import com.ne.gs.controllers.observer.ObserverType;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.controllers.observer.StartMovingListener;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Gatherable;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RewardType;
import com.ne.gs.model.templates.gather.GatherableTemplate;
import com.ne.gs.model.templates.gather.Material;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.PunishmentService;
import com.ne.gs.services.RespawnService;
import com.ne.gs.skillengine.task.GatheringTask;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.captcha.CAPTCHAUtil;
import com.ne.gs.world.World;

/**
 * @author ATracer, sphinx, Cura
 */
public class GatherableController extends VisibleObjectController<Gatherable> {

    private int gatherCount;

    private int currentGatherer;

    private GatheringTask task;

    public enum GatherState {
        GATHERED,
        GATHERING,
        IDLE
    }

    private GatherState state = GatherState.IDLE;

    /**
     * Start gathering process
     */
    public void onStartUse(final Player player) {
        // basic actions, need to improve here
        GatherableTemplate template = getOwner().getObjectTemplate();
        if (template.getLevelLimit() > 0) {
            // You must be at least level %0 to perform extraction.
            if (player.getLevel() < template.getLevelLimit()) {
                player.sendPck(new SM_SYSTEM_MESSAGE(1400737, template.getLevelLimit()));
                return;
            }
        }

        if (player.getInventory().isFull()) {
            // You must have at least one free space in your cube to gather.
            player.sendPck(new SM_SYSTEM_MESSAGE(1330036));
            return;
        }
        if (MathUtil.getDistance(getOwner(), player) > 6) {
            return;
        }

        // check is gatherable
        if (!checkGatherable(player, template)) {
            return;
        }

        if (!checkPlayerSkill(player, template)) {
            return;
        }

        // check for extractor in inventory
        byte result = checkPlayerRequiredExtractor(player, template);
        if (result == 0) {
            return;
        }
        //quest 41453 TODO move to checkPlayerRequiredExtractor when it finished
        if (player.getWorldId() == 600020000 || player.getWorldId() == 600030000) {
            if (player.getEquipment().getEquippedItemIds().contains(122001454)) {
                result = 1;
            } else {
                result = 2;
            }
        }

        // CAPTCHA
        if (SecurityConfig.CAPTCHA_ENABLE) {
            if (SecurityConfig.CAPTCHA_APPEAR.equals(template.getSourceType()) || SecurityConfig.CAPTCHA_APPEAR
                                                                                                .equals("ALL")) {
                int rate = SecurityConfig.CAPTCHA_APPEAR_RATE;
                if (template.getCaptchaRate() > 0) {
                    rate = (int) (template.getCaptchaRate() * 0.1f);
                }

                if (Rnd.chance(rate)) {
                    player.setCaptchaWord(CAPTCHAUtil.getRandomWord());
                    player.setCaptchaImage(CAPTCHAUtil.createCAPTCHA(player.getCaptchaWord()).array());
                    PunishmentService.setIsNotGatherable(player, 0, true, SecurityConfig.CAPTCHA_EXTRACTION_BAN_TIME * 1000L);
                }
            }
        }

        List<Material> materials = null;
        switch (result) {
            case 1: // player has equipped item, or have a consumable in inventory, so he will obtain extra items
                materials = template.getExtraMaterials().getMaterial();
                break;
            case 2:// regular thing
                materials = template.getMaterials().getMaterial();
                break;
        }

        if (materials == null) {
            LoggerFactory.getLogger(GatherableController.class).warn("missing data for " + template.getTemplateId() + " res " + result);
            return;
        }

        Material curMaterial = null;

        int chance = Rnd.get(10000000);
        int current = 0;
        for (Material mat : materials) {
            current += mat.getRate();
            if (current >= chance) {
                curMaterial = mat;
                break;
            }
        }

        if (curMaterial == null) {
            curMaterial = new FakeMaterial(Rnd.get(materials).getItemid());
        }

        synchronized (state) {
            if (state != GatherState.GATHERING) {

                player.getObserveController().notifyObservers(ObserverType.GATHER);

                state = GatherState.GATHERING;
                currentGatherer = player.getObjectId();
                player.getObserveController().attach(new StartMovingListener() {

                    @Override
                    public void moved() {
                        finishGathering(player);
                    }
                });
                int skillLvlDiff = player.getSkillList()
                                         .getSkillLevel(template.getHarvestSkill()) - template.getSkillLevel();
                task = new GatheringTask(player, getOwner(), curMaterial, skillLvlDiff);
                task.start();
            }
        }
    }

    /**
     * Checks whether player have needed skill for gathering and skill level is sufficient
     */
    private boolean checkPlayerSkill(Player player, GatherableTemplate template) {
        int harvestSkillId = template.getHarvestSkill();
        if (!player.getSkillList().isSkillPresent(harvestSkillId)) {
            if (harvestSkillId == 30001) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_GATHER_INCORRECT_SKILL);
            } else {
                player.sendPck(new SM_SYSTEM_MESSAGE(1330054,
                    DescId.of(DataManager.SKILL_DATA.getSkillTemplate(harvestSkillId).getNameId())));
            }
            return false;
        }
        if (player.getSkillList().getSkillLevel(harvestSkillId) < template.getSkillLevel()) {
            // Your %0 skill level is not high enough.
            player.sendPck(new SM_SYSTEM_MESSAGE(1330001,
                DescId.of(DataManager.SKILL_DATA.getSkillTemplate(harvestSkillId).getNameId())));
            return false;
        }
        return true;
    }

    // TODO items also may require mapId like in quest 41453,
    //      so rework this & make it flexible to handle multiple conditions
    @Deprecated
    private byte checkPlayerRequiredExtractor(Player player, GatherableTemplate template) {
        if (template.getRequiredItemId() > 0) {
            if (template.getCheckType() == 1) {
                List<Item> items = player.getEquipment().getEquippedItemsByItemId(template.getRequiredItemId());
                boolean condOk = false;
                for (Item item : items) {
                    if (item.isEquipped()) {
                        condOk = true;
                        break;
                    }
                }
                return (byte) (condOk ? 1 : 2);

            } else if (template.getCheckType() == 2) {
                if (player.getInventory().getItemCountByItemId(template.getRequiredItemId()) < 1) {
                    // You do not have enough %0 to gather.
                    player.sendPck(new SM_SYSTEM_MESSAGE(1400376, DescId.of(template.getRequiredItemNameId())));
                    return 0;
                } else {
                    return 1;
                }
            }
        }

        return 2;
    }

    /**
     * @author Cura
     */
    private boolean checkGatherable(Player player, GatherableTemplate template) {
        if (player.isNotGatherable()) {
            // You are currently poisoned and unable to extract. (Time remaining: %DURATIONTIME0)
            player.sendPck(new SM_SYSTEM_MESSAGE(1400273, (int) ((player.getGatherableTimer() - (System.currentTimeMillis() - player
                .getStopGatherable())) / 1000)));
            return false;
        }
        return true;
    }

    public void completeInteraction() {
        state = GatherState.IDLE;
		gatherCount++;
        if (gatherCount >= getOwner().getObjectTemplate().getHarvestCount()) {
            onDespawn();
        }
    }

    public void rewardPlayer(Player player) {
        if (player != null) {
            int skillLvl = getOwner().getObjectTemplate().getSkillLevel();
            int xpReward = (int) ((0.0031 * (skillLvl + 5.3) * (skillLvl + 1592.8) + 60));

            if (player.getSkillList().addSkillXp(player, getOwner().getObjectTemplate().getHarvestSkill(),
                (int) RewardType.GATHERING.calcReward(player, xpReward), skillLvl)) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHERING_SUCCESS_GETEXP);
                player.getCommonData().addExp(xpReward, RewardType.GATHERING);
            } else {
                player.sendPck(
                    SM_SYSTEM_MESSAGE.STR_MSG_DONT_GET_PRODUCTION_EXP(
                        DescId.of(DataManager.SKILL_DATA.getSkillTemplate(
                            getOwner().getObjectTemplate().getHarvestSkill()).getNameId())));
            }
        }
    }

    /**
     * Called by client when some action is performed or on finish gathering Called by move observer on player move
     */
    public void finishGathering(Player player) {
        if (currentGatherer == player.getObjectId()) {
            if (state == GatherState.GATHERING) {
                task.abort();
            }
            currentGatherer = 0;
            state = GatherState.IDLE;
        }
    }

    @Override
    public void onDespawn() {
        Gatherable owner = getOwner();
        if (!getOwner().isInInstance()) {
            RespawnService.scheduleRespawnTask(owner);
        }
        World.getInstance().despawn(owner);
    }

    @Override
    public void onBeforeSpawn() {
        gatherCount = 0;
    }

    @Override
    public Gatherable getOwner() {
        return super.getOwner();
    }

    // FIXMe temp workaround
    public static final class FakeMaterial extends Material {
        public FakeMaterial(int itemId) {
            this.itemid = itemId;
        }
    }
}
