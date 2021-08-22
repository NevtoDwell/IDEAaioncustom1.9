/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.ItemSlot;
import com.ne.gs.model.templates.item.RequireSkill;
import com.ne.gs.model.templates.item.Stigma;
import com.ne.gs.model.templates.item.Stigma.StigmaSkill;
import com.ne.gs.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.ne.gs.network.aion.serverpackets.SM_STIGMA_SKILL_REMOVE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.utils.audit.AuditLogger;

import static com.ne.gs.services.StigmaService.AdvStigmaPerm.accessibleSlotCountFor;
/**
 * @author ATracer
 */
public final class StigmaService {
	public static final int MAX_ADV_COUNT = 6;

    private static final Logger log = LoggerFactory.getLogger(StigmaService.class);

    public static boolean notifyEquipAction(Player player, Item resultItem, int slot) {
        if (resultItem.getItemTemplate().isStigma()) {
            if (ItemSlot.isRegularStigma(slot)) {
                int possible = getPossibleStigmaCount(player);
                int real = player.getEquipment().getEquippedItemsRegularStigma().size();
                if (possible <= real) {
                    AuditLogger.info(player, "Regular stigma count big "
                        + possible + " " + real + " " + slot);
                    return false;
                }
            } else if (ItemSlot.isAdvancedStigma(slot)) {
                int possible = accessibleSlotCountFor(player);
                int real = player.getEquipment().getEquippedItemsAdvencedStigma().size();
                if (possible <= real) {
                    AuditLogger.info(player, "Adv stigma count big "
                        + possible + " " + real + " " + slot);
                    return false;
                }
            }

            if (!resultItem.getItemTemplate().isClassSpecific(player.getCommonData().getPlayerClass())) {
                AuditLogger.info(player, "Possible client hack not valid for class.");
                return false;
            }

            Stigma stigmaInfo = resultItem.getItemTemplate().getStigma();

            if (stigmaInfo == null) {
                log.warn("Stigma info missing for item: " + resultItem.getItemTemplate().getTemplateId());
                return false;
            }

            int shardCount = stigmaInfo.getShard();
            if (player.getInventory().getItemCountByItemId(141000001) < shardCount) {
                AuditLogger.info(player, "Possible client hack stigma shard count low.");
                return false;
            }
            // TODO @hex1ro: rework inspection. it does not work as expected
//            for (RequireSkill rs : stigmaInfo.getRequireSkill()) {
//                for (int id : rs.getSkillId()) {
//                    if (!player.getSkillList().isSkillPresent(id)) {
//                        AuditLogger.info(player, "Possible client hack advenced stigma skill.");
//                        return false;
//                    }
//                }
//            }
            if (!player.getInventory().decreaseByItemId(141000001, shardCount)) {
                return false;
            }
            player.getSkillList().addStigmaSkill(player, stigmaInfo.getSkills(), true);
        }
        return true;
    }

    public static boolean notifyUnequipAction(Player player, Item resultItem) {
        if (resultItem.getItemTemplate().isStigma()) {
            Stigma stigmaInfo = resultItem.getItemTemplate().getStigma();
            for (Item item : player.getEquipment().getEquippedItemsAllStigma()) {
                Stigma si = item.getItemTemplate().getStigma();
                if (resultItem.equals(item) || si == null) {
                    continue;
                }
                for (StigmaSkill skill : stigmaInfo.getSkills()) {
                    for (RequireSkill rs : si.getRequireSkill()) {
                        if (rs.getSkillId().contains(skill.getSkillId())) {
                            DescId descId = DescId.of(resultItem.getItemTemplate().getNameId());
                            player.sendPck(new SM_SYSTEM_MESSAGE(1300410, descId, descId));
                            return false;
                        }
                    }
                }
                int itemId = resultItem.getItemId();
                if ((itemId == 140000007 || itemId == 140000005) && (player.getEquipment().isDualWeaponEquipped())) {
                    player.sendPck(SM_SYSTEM_MESSAGE
                        .STR_STIGMA_CANNT_UNEQUIP_STONE_FIRST_UNEQUIP_CURRENT_EQUIPPED_ITEM);
                    return false;
                }
            }

            player.sendPck(new SM_INVENTORY_UPDATE_ITEM(player, resultItem, ItemPacketService.ItemUpdateType
                .EQUIP_UNEQUIP));

            for (StigmaSkill skill : stigmaInfo.getSkills()) {
                SkillLearnService.removeSkill(player, skill.getSkillId());
                player.getEffectController().removeEffect(skill.getSkillId());
                int nameId = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId()).getNameId();
                player.sendPck(new SM_SYSTEM_MESSAGE(1300403, DescId.of(nameId)));
                player.sendPck(new SM_STIGMA_SKILL_REMOVE(skill.getSkillId()));
            }
        }
        return true;
    }

    public static void onPlayerLogin(Player player) {
        List<Item> equippedItems = player.getEquipment().getEquippedItemsAllStigma();
        for (Item item : equippedItems) {
            if (item.getItemTemplate().isStigma()) {
                Stigma stigmaInfo = item.getItemTemplate().getStigma();

                if (stigmaInfo == null) {
                    log.warn("Stigma info missing for item: " + item.getItemTemplate().getTemplateId());
                    return;
                }
                player.getSkillList().addStigmaSkill(player, stigmaInfo.getSkills(), false);
            }
        }

        for (Item item : equippedItems) {
            if (item.getItemTemplate().isStigma()) {
//                if (!isPossibleEquippedStigma(player, item)) {
//                    AuditLogger.info(player, "Possible client hack stigma count big :O");
//                    player.getEquipment().unEquipItem(item.getObjectId(), 0);
//                    continue;
//                }
                Stigma stigmaInfo = item.getItemTemplate().getStigma();

                if (stigmaInfo == null) {
                    log.warn("Stigma info missing for item: " + item.getItemTemplate().getTemplateId());
                    player.getEquipment().unEquipItem(item.getObjectId(), 0);
                    continue;
                }
//                int needSkill = stigmaInfo.getRequireSkill().size();
//                for (RequireSkill rs : stigmaInfo.getRequireSkill()) {
//                    for (int id : rs.getSkillId()) {
//                        if (player.getSkillList().isSkillPresent(id)) {
//                            needSkill--;
//                            break;
//                        }
//                    }
//                }
//                if (needSkill != 0) {
//                    AuditLogger.info(player, "Possible client hack advenced stigma skill.");
//                    player.getEquipment().unEquipItem(item.getObjectId(), 0);
//                    continue;
//                }
                if (!item.getItemTemplate().isClassSpecific(player.getCommonData().getPlayerClass())) {
                    AuditLogger.info(player, "Possible client hack not valid for class.");
                    player.getEquipment().unEquipItem(item.getObjectId(), 0);
                    //continue;
                }
            }
        }

        player.sendPck(SM_CUBE_UPDATE.stigmaSlots(accessibleSlotCountFor(player)));
    }

    /**
     * Get the number of available Stigma
     */
    private static int getPossibleStigmaCount(Player player) {
        if (player == null || player.getLevel() < 20) {
            return 0;
        }

        if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
            return 6;
        }
        int playerLevel = player.getLevel();

        if (playerLevel < 30) {
            return 2;
        } else if (playerLevel < 40) {
            return 3;
        } else if (playerLevel < 50) {
            return 4;
        } else if (playerLevel < 55) {
            return 5;
        } else {
            return 6;
        }
    }

    private static boolean isCompleteQuest(Player p, int questId, int param) {
        QuestState qs = p.getQuestStateList().getQuestState(questId);
        return p.isCompleteQuest(questId)
            || (qs.getStatus() == QuestStatus.START && qs.getQuestVars().getQuestVars() == param);
    }

    /**
     * Stigma is a worn check available slots
     */
    private static boolean isPossibleEquippedStigma(Player player, Item item) {
        if (player == null || (item == null || !item.getItemTemplate().isStigma())) {
            return false;
        }

        int itemSlotToEquip = item.getEquipmentSlot();
        // Stigma Quest Elyos: 1929, Asmodians: 2900
        boolean isCompleteQuest = player.getRace() == Race.ELYOS
            ? isCompleteQuest(player, 1929, 98)
            : isCompleteQuest(player, 2900, 99);

        if (!isCompleteQuest) {
            return false;
        }

        if (ItemSlot.isRegularStigma(itemSlotToEquip)) {
            if (player.getLevel() < 20) {
                return false;
            }

            if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
                return true;
            }

            if (checkSlot(itemSlotToEquip, maxSlotIdFor(player.getLevel()))) {
                return true;
            }
        } else if (ItemSlot.isAdvancedStigma(itemSlotToEquip)) {
            if (player.getLevel() < 45) {
                return false;
            }

            if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
                return true;
            }

            if (checkSlot(itemSlotToEquip, AdvStigmaPerm.maxSlotIdFor(player))) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkSlot(int requested, int allowed) {
        return allowed != 0 && requested <= allowed;
    }

    private static int maxSlotIdFor(int playerLevel) {
        if (playerLevel < 30) {
            return ItemSlot.STIGMA2.id();
        } else if (playerLevel < 40) {
            return ItemSlot.STIGMA3.id();
        } else if (playerLevel < 50) {
            return ItemSlot.STIGMA4.id();
        } else if (playerLevel < 55) {
            return ItemSlot.STIGMA5.id();
        } else {
            return ItemSlot.STIGMA6.id();
        }
    }

    public enum AdvStigmaPerm {
        E(
            Cond.of(ItemSlot.ADV_STIGMA6, 11550),
            Cond.of(ItemSlot.ADV_STIGMA5, 30217, 11276),
            Cond.of(ItemSlot.ADV_STIGMA4, 11049),
            Cond.of(ItemSlot.ADV_STIGMA3, 3932),
            Cond.of(ItemSlot.ADV_STIGMA2, 3931),
            Cond.of(ItemSlot.ADV_STIGMA1, 3930)),
        A(
            Cond.of(ItemSlot.ADV_STIGMA6, 21550),
            Cond.of(ItemSlot.ADV_STIGMA5, 30317, 21278),
            Cond.of(ItemSlot.ADV_STIGMA4, 21049),
            Cond.of(ItemSlot.ADV_STIGMA3, 4936),
            Cond.of(ItemSlot.ADV_STIGMA2, 4935),
            Cond.of(ItemSlot.ADV_STIGMA1, 4934));
        private final Cond[] _conds;

        private AdvStigmaPerm(Cond... conds) {
            _conds = conds;
        }

        public static int maxSlotIdFor(Player player) {
            ItemSlot slot = maxSlotFor(player);
            return slot != null ? slot.id() : 0;
        }

        @Nullable
        public static ItemSlot maxSlotFor(Player player) {
            switch (player.getRace()) {
                case ELYOS:
                    return _findSlotId(player, E);
                case ASMODIANS:
                    return _findSlotId(player, A);
            }
            return null;
        }

        public static int accessibleSlotCountFor(Player player) {
            ItemSlot slot = maxSlotFor(player);
            return slot != null ? slot2maxCount(slot) : 0;
        }
		
        public static int slot2maxCount(@NotNull ItemSlot slot) {
            switch (slot) {
                case ADV_STIGMA6:
                    return MAX_ADV_COUNT;
                case ADV_STIGMA5:
                    return 5;
                case ADV_STIGMA4:
                    return 4;
                case ADV_STIGMA3:
                    return 3;
                case ADV_STIGMA2:
                    return 2;
                case ADV_STIGMA1:
                    return 1;
            }
            return 0;
        }
		
        private static ItemSlot _findSlotId(Player player, AdvStigmaPerm a) {

            int slots = 0;

            for (Cond cond : a._conds) {
                for (int value : cond.values) {

                    if(player.isCompleteQuest(value)){
                        slots++;
                        break;
                    }
                }
            }

            if(slots == 0)
                return null;

            switch (slots){
                case 1:
                    return ItemSlot.ADV_STIGMA1;
                case 2:
                    return ItemSlot.ADV_STIGMA2;
                case 3:
                    return ItemSlot.ADV_STIGMA3;
                case 4:
                    return ItemSlot.ADV_STIGMA4;
                case 5:
                    return ItemSlot.ADV_STIGMA5;
                default:
                    return ItemSlot.ADV_STIGMA6;
            }
        }

        private static class Cond {

            final ItemSlot slot;
            final int[] values;

            Cond(ItemSlot slot, int[] values) {
                this.slot = slot;
                this.values = values;
            }

            static Cond of(ItemSlot slot, int... values) {
                return new Cond(slot, values);
            }
        }
    }
}
