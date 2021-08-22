/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.controllers.ItemUseObserver;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ExtractedItemsCollection;
import com.ne.gs.model.templates.item.RandomItem;
import com.ne.gs.model.templates.item.ResultedItem;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author oslo(a00441234)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DecomposeAction")
public class DecomposeAction extends AbstractItemAction {

    private static final Logger log = LoggerFactory.getLogger(DecomposeAction.class);

    private static final int USAGE_DELAY = CustomConfig.DECOMPOSE_ACTION_TIME;
    private static Map<Integer, int[]> manastones = new HashMap<>();
    private static Map<Race, int[]> chunkEarth;
    private static Map<Race, int[]> chunkSand;
    private static int[] chunkRock = {152000106, 152000206, 152000308, 152000310, 152000312, 152000109, 152000209, 152000314, 152000316, 152000318, 152000115,
                                      152000216, 152000219, 152000321, 152000323, 152000325};

    private static int[] chunkGemstone = {152000112, 152000213, 152000116, 152000212, 152000217, 152000326, 152000327, 152000328};

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        List<ExtractedItemsCollection> itemsCollections = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());
        if (itemsCollections == null || itemsCollections.isEmpty()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVALID_STANCE(parentItem.getNameID()));
            return false;
        }
        if (player.getInventory().isFull()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVENTORY_IS_FULL);
            return false;
        }
        return true;
    }

    @Override
    public void act(final Player player, final Item parentItem, final Item targetItem) {
        player.getController().cancelUseItem();
        List<ExtractedItemsCollection> itemsCollections = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());

        Collection<ExtractedItemsCollection> levelSuitableItems = filterItemsByLevel(player, itemsCollections);
        final ExtractedItemsCollection selectedCollection = selectItemByChance(levelSuitableItems);

        PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(),
            USAGE_DELAY, 0, 0));
        final ItemUseObserver observer = new ItemUseObserver() {

            @Override
            public void abort() {
                player.getController().cancelTask(TaskId.ITEM_USE);
                player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
                player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(DescId.of(parentItem.getItemTemplate().getNameId())));
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
                    .getItemTemplate().getTemplateId(), 0, 2, 0), true);

                player.getObserveController().removeObserver(this);
            }
        };
        player.getObserveController().attach(observer);
        player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                player.getObserveController().removeObserver(observer);
                boolean validAction = postValidate(player, parentItem);
                if (validAction) {
                    if (selectedCollection.getItems().size() > 0) {
                        for (ResultedItem resultItem : selectedCollection.getItems()) {
                            if (canAcquire(player, resultItem)) {
                                ItemService.addItem(player, resultItem.getItemId(), resultItem.getResultCount());
                            }
                        }
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_SUCCEED(parentItem.getNameID()));
                    } else if (selectedCollection.getRandomItems().size() > 0) {
                        for (RandomItem randomItem : selectedCollection.getRandomItems()) {
                            if (randomItem.getType() != null) {
                                int randomId = 0;
                                int i = 0;
                                int itemLvl = parentItem.getItemTemplate().getLevel();
                                switch (randomItem.getType()) {
                                    case ENCHANTMENT: {
                                        do {
                                            randomId = 166000000 + itemLvl + Rnd.get(50);
                                            i++;
                                            if (i > 50) {
                                                randomId = 0;
                                                log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
                                                break;
                                            }
                                        } while (!ItemService.checkRandomTemplate(randomId));
                                        break;
                                    }
                                    case MANASTONE:
                                        break;
                                    case MANASTONE_COMMON_GRADE_10:
                                        List<Integer> itemsCommon10 = new ArrayList<Integer>();
                                        itemsCommon10.add(Rnd.get(167000226, 167000233)); // ID 167000234 Unavailable
                                        itemsCommon10.add(Rnd.get(167000525, 167000526));
                                        itemsCommon10.add(167000235);
                                        int randomItemOutCommon10 = itemsCommon10.get(Rnd.get(0, 2));
                                        randomId = randomItemOutCommon10;
                                        break;
                                    case MANASTONE_COMMON_GRADE_20:
                                        List<Integer> itemsCommon20 = new ArrayList<Integer>();
                                        itemsCommon20.add(Rnd.get(167000258, 167000261)); //ID 167000262 + 167000266 Unavailable
                                        itemsCommon20.add(Rnd.get(167000263, 167000265));
                                        itemsCommon20.add(Rnd.get(167000527, 167000528));
                                        itemsCommon20.add(167000267);
                                        int randomItemOutCommon20 = itemsCommon20.get(Rnd.get(0, 3));
                                        randomId = randomItemOutCommon20;
                                        break;
                                    case MANASTONE_COMMON_GRADE_30:
                                        List<Integer> itemsCommon30 = new ArrayList<Integer>();
                                        itemsCommon30.add(Rnd.get(167000290, 167000297)); //ID 167000298 Unavailable
                                        itemsCommon30.add(Rnd.get(167000529, 167000530));
                                        itemsCommon30.add(167000299);
                                        int randomItemOutCommon30 = itemsCommon30.get(Rnd.get(0, 2));
                                        randomId = randomItemOutCommon30;
                                        break;
                                    case MANASTONE_COMMON_GRADE_40:
                                        List<Integer> itemsCommon40 = new ArrayList<Integer>();
                                        itemsCommon40.add(Rnd.get(167000322, 167000329)); //ID 167000330 Unavailable
                                        itemsCommon40.add(Rnd.get(167000531, 167000532));
                                        itemsCommon40.add(167000331);
                                        int randomItemOutCommon40 = itemsCommon40.get(Rnd.get(0, 2));
                                        randomId = randomItemOutCommon40;
                                        break;
                                    case MANASTONE_COMMON_GRADE_50:
                                        List<Integer> itemsCommon50 = new ArrayList<Integer>();
                                        itemsCommon50.add(Rnd.get(167000354, 167000361)); //ID 167000362 Unavailable
                                        itemsCommon50.add(Rnd.get(167000533, 167000534));
                                        itemsCommon50.add(167000363);
                                        int randomItemOutCommon50 = itemsCommon50.get(Rnd.get(0, 2));
                                        randomId = randomItemOutCommon50;
                                        break;
                                    case MANASTONE_COMMON_GRADE_60:
                                        randomId = Rnd.get(167000543, 167000550);
                                        break;
                                    case MANASTONE_RARE_GRADE_10: // Unavailable
                                        break;
                                    case MANASTONE_RARE_GRADE_20:
                                        List<Integer> itemsRare20 = new ArrayList<Integer>();
                                        itemsRare20.add(Rnd.get(167000418, 167000425)); //ID 167000426 Unavailable
                                        itemsRare20.add(Rnd.get(167000535, 167000536));
                                        itemsRare20.add(167000427);
                                        int randomItemOutRare20 = itemsRare20.get(Rnd.get(0, 2));
                                        randomId = randomItemOutRare20;
                                        break;
                                    case MANASTONE_RARE_GRADE_30:
                                        List<Integer> itemsRare30 = new ArrayList<Integer>();
                                        itemsRare30.add(Rnd.get(167000450, 167000457)); //ID 167000458 Unavailable
                                        itemsRare30.add(Rnd.get(167000537, 167000538));
                                        itemsRare30.add(167000459);
                                        itemsRare30.add(167000465);
                                        int randomItemOutRare30 = itemsRare30.get(Rnd.get(0, 3));
                                        randomId = randomItemOutRare30;
                                        break;
                                    case MANASTONE_RARE_GRADE_40:
                                        List<Integer> itemsRare40 = new ArrayList<Integer>();
                                        itemsRare40.add(Rnd.get(167000482, 167000491)); //ID 167000492 - 167000496 Unavailable
                                        itemsRare40.add(Rnd.get(167000539, 167000540));
                                        itemsRare40.add(167000497);
                                        int randomItemOutRare40 = itemsRare40.get(Rnd.get(0, 2));
                                        randomId = randomItemOutRare40;
                                        break;
                                    case MANASTONE_RARE_GRADE_50:
                                        List<Integer> itemsRare50 = new ArrayList<Integer>();
                                        itemsRare50.add(Rnd.get(167000514, 167000523));
                                        itemsRare50.add(Rnd.get(167000541, 167000542));
                                        int randomItemOutRare50 = itemsRare50.get(Rnd.get(0, 1));
                                        randomId = randomItemOutRare50;
                                        break;
                                    case MANASTONE_RARE_GRADE_60:
                                        randomId = Rnd.get(167000551, 167000563);
                                        break;
                                    case CHUNK_EARTH:
                                        int[] earth = chunkEarth.get(player.getRace());

                                        randomId = earth[Rnd.get(earth.length)];
                                        if (!ItemService.checkRandomTemplate(randomId)) {
                                            log.warn("DecomposeAction random item id not found. " + randomId);
                                            return;
                                        }
                                        break;
                                    case CHUNK_SAND:
                                        int[] sand = chunkSand.get(player.getRace());

                                        randomId = sand[Rnd.get(sand.length)];

                                        if (!ItemService.checkRandomTemplate(randomId)) {
                                            DecomposeAction.log.warn("DecomposeAction random item id not found. " + randomId);
                                            return;
                                        }
                                        break;
                                    case CHUNK_ROCK:
                                        randomId = DecomposeAction.chunkRock[Rnd.get(DecomposeAction.chunkRock.length)];

                                        if (!ItemService.checkRandomTemplate(randomId)) {
                                            DecomposeAction.log.warn("DecomposeAction random item id not found. " + randomId);
                                            return;
                                        }
                                        break;
                                    case CHUNK_GEMSTONE:
                                        randomId = DecomposeAction.chunkGemstone[Rnd.get(DecomposeAction.chunkGemstone.length)];

                                        if (!ItemService.checkRandomTemplate(randomId)) {
                                            DecomposeAction.log.warn("DecomposeAction random item id not found. " + randomId);
                                            return;
                                        }
                                        break;
                                    case ANCIENTITEMS:
                                        do {
                                            randomId = Rnd.get(186000051, 186000066);
                                            i++;
                                            if (i > 50) {
                                                randomId = 0;
                                                log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
                                                break;
                                            }
                                        } while (!ItemService.checkRandomTemplate(randomId));
                                        break;
                                }
                                if (randomId != 0 && randomId != 167000524) {
                                    ItemService.addItem(player, randomId, randomItem.getResultCount());
                                }
                            }
                        }
                    }
                }
                PacketSendUtility.broadcastPacketAndReceive(player,
                    new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, validAction ? 1 : 2, 0));
            }

            private boolean canAcquire(Player player, ResultedItem resultItem) {
                Race race = resultItem.getRace();
                if (race != Race.PC_ALL && !race.equals(player.getRace())) {
                    return false;
                }
                PlayerClass playerClass = resultItem.getPlayerClass();
                if (!playerClass.equals(PlayerClass.ALL) && !playerClass.equals(player.getPlayerClass())) {
                    return false;
                }
                return true;
            }

            boolean postValidate(Player player, Item parentItem) {
                if (!canAct(player, parentItem, targetItem)) {
                    return false;
                }
                if (player.getInventory().getFreeSlots() < calcMaxCountOfSlots(selectedCollection, player)) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DECOMPRESS_INVENTORY_IS_FULL);
                    return false;
                }
                if (player.getLifeStats().isAlreadyDead() || !player.isSpawned()) {
                    return false;
                }
                if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM);
                    return false;
                }
                if (selectedCollection.getItems().isEmpty() && selectedCollection.getRandomItems().isEmpty()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_FAILED(parentItem.getNameID()));
                    return false;
                }
                return true;
            }
        }, USAGE_DELAY));
    }

    /**
     * Add to result collection only items wich suits player's level
     */
    private Collection<ExtractedItemsCollection> filterItemsByLevel(Player player, List<ExtractedItemsCollection> itemsCollections) {
        int playerLevel = player.getLevel();
        Collection<ExtractedItemsCollection> result = new ArrayList<>();
        for (ExtractedItemsCollection collection : itemsCollections) {
            if (collection.getMinLevel() <= playerLevel && (collection.getMaxLevel() <= 0 || collection.getMaxLevel() >= playerLevel)) {

                result.add(collection);
            }
        }
        return result;
    }

    /**
     * Select only 1 item based on chance attributes
     */
    private ExtractedItemsCollection selectItemByChance(Collection<ExtractedItemsCollection> itemsCollections) {
        float sumOfChances = calcSumOfChances(itemsCollections);
        float currentSum = 0;
        float rnd = Rnd.get(0, (int) (sumOfChances - 1) * 1000) / 1000;
        ExtractedItemsCollection selectedCollection = null;
        for (ExtractedItemsCollection collection : itemsCollections) {
            currentSum += collection.getChance();
            if (rnd < currentSum) {
                selectedCollection = collection;
                break;
            }
        }
        return selectedCollection;
    }

    private int calcMaxCountOfSlots(ExtractedItemsCollection itemsCollections, Player player) {
        int maxCount = 0;
        for (ResultedItem item : itemsCollections.getItems()) {
            if (item.getRace().equals(Race.PC_ALL) || player.getRace().equals(item.getRace())) {
                if (item.getPlayerClass().equals(PlayerClass.ALL) || player.getPlayerClass().equals(item.getPlayerClass())) {
                    maxCount++;
                }
            }
        }
        return maxCount;
    }

    private float calcSumOfChances(Collection<ExtractedItemsCollection> itemsCollections) {
        int sum = 0;
        for (ExtractedItemsCollection collection : itemsCollections) {
            sum += collection.getChance();
        }
        return sum;
    }

    static {
        manastones.put(10, new int[]{167000226, 167000227, 167000228, 167000229, 167000230, 167000231, 167000232, 167000233, 167000235});

        manastones.put(20, new int[]{167000258, 167000259, 167000260, 167000261, 167000263, 167000264, 167000265, 167000267, 167000418,
                                                      167000419, 167000420, 167000421, 167000423, 167000424, 167000425, 167000427});

        manastones.put(30, new int[]{167000290, 167000291, 167000292, 167000293, 167000294, 167000295, 167000296, 167000297, 167000299,
                                                      167000450, 167000451, 167000452, 167000453, 167000454, 167000455, 167000456, 167000457, 167000459});

        manastones.put(40, new int[]{167000322, 167000323, 167000324, 167000325, 167000327, 167000328, 167000329, 167000331, 167000482,
                                                      167000483, 167000484, 167000485, 167000487, 167000488, 167000489, 167000491, 167000539, 167000540});

        manastones.put(50, new int[]{167000354, 167000355, 167000356, 167000357, 167000358, 167000359, 167000360, 167000361, 167000363,
                                                      167000514, 167000515, 167000516, 167000517, 167000518, 167000519, 167000520, 167000521, 167000522, 167000541, 167000542});

        manastones.put(60, new int[]{167000543, 167000544, 167000545, 167000546, 167000547, 167000548, 167000549, 167000550, 167000551,
                                                      167000552, 167000553, 167000554, 167000555, 167000556, 167000557, 167000558, 167000560, 167000561});

        chunkEarth = new HashMap<>();

        chunkEarth.put(Race.ASMODIANS, new int[]{152000051, 152000052, 152000053, 152000451, 152000453, 152000551, 152000651, 152000751, 152000752,
                                                 152000753, 152000851, 152000852, 152000853, 152001051, 152001052, 152000201, 152000102, 152000054, 152000055, 152000455, 152000457, 152000552,
                                                 152000652, 152000754, 152000755, 152000854, 152000855, 152000102, 152000202, 152000056, 152000057, 152000459, 152000461, 152000553, 152000653,
                                                 152000756, 152000757, 152000856, 152000857, 152000104, 152000204, 152000058, 152000059, 152000463, 152000465, 152000554, 152000654, 152000758,
                                                 152000759, 152000760, 152000858, 152001053, 152000107, 152000207, 152003004, 152003005, 152003006, 152000061, 152000062, 152000063, 152000468,
                                                 152000470, 152000556, 152000656, 152000657, 152000762, 152000763, 152000860, 152000861, 152000862, 152001055, 152001056, 152000113, 152000117,
                                                 152000214, 152000606, 152000713, 152000811});

        chunkEarth.put(Race.ELYOS, new int[]{152000001, 152000002, 152000003, 152000401, 152000403, 152000501, 152000601, 152000701, 152000702, 152000703,
                                             152000801, 152000802, 152000803, 152001001, 152001002, 152000101, 152000201, 152000004, 152000005, 152000405, 152000407, 152000502, 152000602,
                                             152000704, 152000705, 152000804, 152000805, 152000102, 152000202, 152000006, 152000007, 152000409, 152000411, 152000503, 152000603, 152000706,
                                             152000707, 152000806, 152000807, 152000104, 152000204, 152000008, 152000009, 152000413, 152000415, 152000504, 152000604, 152000708, 152000709,
                                             152000710, 152000808, 152001003, 152000107, 152000207, 152003004, 152003005, 152003006, 152000010, 152000011, 152000012, 152000417, 152000419,
                                             152000505, 152000605, 152000607, 152000711, 152000712, 152000809, 152000810, 152000812, 152001004, 152001005, 152000113, 152000117, 152000214,
                                             152000606, 152000713, 152000811});

        chunkSand = new HashMap<>();

        chunkSand.put(Race.ASMODIANS, new int[]{152000452, 152000454, 152000301, 152000302, 152000303, 152000456, 152000458, 152000103, 152000203, 152000304,
                                                152000305, 152000306, 152000460, 152000462, 152000105, 152000205, 152000307, 152000309, 152000311, 152000464, 152000466, 152000108, 152000208,
                                                152000313, 152000315, 152000317, 152000469, 152000471, 152000114, 152000215, 152000320, 152000322, 152000324});

        chunkSand.put(Race.ELYOS, new int[]{152000402, 152000404, 152000301, 152000302, 152000303, 152000406, 152000408, 152000103, 152000203, 152000304,
                                            152000305, 152000306, 152000410, 152000412, 152000105, 152000205, 152000307, 152000309, 152000311, 152000414, 152000416, 152000108, 152000208,
                                            152000313, 152000315, 152000317, 152000418, 152000420, 152000114, 152000215, 152000320, 152000322, 152000324});
    }
}
