/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.drop;

import static com.ne.gs.modules.housing.House.HouseType.PALACE;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.configs.main.DropConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.drop.Drop;
import com.ne.gs.model.drop.DropGroup;
import com.ne.gs.model.drop.DropItem;
import com.ne.gs.model.drop.NpcDrop;
import com.ne.gs.model.gameobjects.DropNpc;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.pet.PetFunctionType;
import com.ne.gs.modules.housing.HouseInfo;
import com.ne.gs.network.aion.serverpackets.SM_LOOT_STATUS;
import com.ne.gs.network.aion.serverpackets.SM_PET;
import com.ne.gs.services.QuestService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.stats.DropRewardEnum;
import com.ne.gs.world.World;

/**
 * @author xTz
 */
public class DropRegistrationService {

    private final Map<Integer, Set<DropItem>> currentDropMap = new FastMap<Integer, Set<DropItem>>().shared();
    private final Map<Integer, DropNpc> dropRegistrationMap = new FastMap<Integer, DropNpc>().shared();

    public void registerDrop(Npc npc, Player player, Collection<Player> groupMembers) {
        registerDrop(npc, player, player.getLevel(), groupMembers);
    }

    private DropRegistrationService() {
        init();
    }

    public final void init() {

        Collection<NpcTemplate> templates = DataManager.NPC_DATA.getNpcData().valueCollection();
        for(NpcTemplate npcTemplate : templates){

            int[] ids = npcTemplate.getDroplistIds();
            if (ids == null || ids.length == 0)
                continue;


            if (npcTemplate.getNpcDrop() != null) {
                NpcDrop currentDrop = npcTemplate.getNpcDrop();
                for (DropGroup dg : currentDrop.getDropGroup()) {
                    Iterator<Drop> iter = dg.getDrop().iterator();
                    while (iter.hasNext()) {
                        Drop d = iter.next();
                        for (int dg2Id : ids) {
                            DropGroup dg2 = DataManager.DROPLISTS_DATA.getDroplist(dg2Id);
                            for (Drop d2 : dg2.getDrop()) {
                                if (d.getItemId() == d2.getItemId()) {
                                    iter.remove();
                                }
                            }
                        }
                    }
                }
                List<DropGroup> list = new ArrayList<>();
                for (int dgId : ids) {
                    boolean added = false;
                    DropGroup dg = DataManager.DROPLISTS_DATA.getDroplist(dgId);
                    for (DropGroup dg2 : currentDrop.getDropGroup()) {
                        if (dg2.getGroupName().equals(dg.getGroupName())) {
                            dg2.getDrop().addAll(dg.getDrop());
                            added = true;
                        }
                    }
                    if (!added) {
                        list.add(dg);
                    }
                }
                if (!list.isEmpty()) {
                    currentDrop.getDropGroup().addAll(list);
                }
            } else {

                List<DropGroup> dg = DataManager.DROPLISTS_DATA.getDroplists(ids);

                NpcDrop nd = new NpcDrop();
                nd.setNpcId(npcTemplate.getTemplateId());
                nd.setDropGroup(dg);

                npcTemplate.setNpcDrop(nd);
            }
        }
    }

    /**
     * After NPC dies, it can register arbitrary drop
     */
    public void registerDrop(Npc npc, Player player, int heighestLevel, Collection<Player> groupMembers) {

        if (player == null) {
            return;
        }
        int npcObjId = npc.getObjectId();

        // Getting all possible drops for this Npc
        NpcDrop npcDrop = npc.getNpcDrop();
        Set<DropItem> droppedItems = Collections.synchronizedSet(new THashSet<DropItem>());
        int index = 1;
        int dropChance = 100;
        boolean isChest = npc.getAi2().getName().equals("chest");
        if (!DropConfig.DISABLE_DROP_REDUCTION && (isChest && npc.getLevel() != 1 || !isChest)) {
            dropChance = DropRewardEnum.dropRewardFrom(heighestLevel - npc.getLevel()); // reduce chance depending on level
        }

        Player genesis = player;

        // Distributing drops to players
        Collection<Player> dropPlayers = new ArrayList<>();
        Collection<Player> winningPlayers = new ArrayList<>();
        if (player.isInGroup2() || player.isInAlliance2()) {
            List<Integer> dropMembers = new ArrayList<>();
            LootGroupRules lootGrouRules = player.getLootGroupRules();

            switch (lootGrouRules.getLootRule()) {
                case ROUNDROBIN:
                    int size = groupMembers.size();
                    if (size > lootGrouRules.getNrRoundRobin()) {
                        lootGrouRules.setNrRoundRobin(lootGrouRules.getNrRoundRobin() + 1);
                    } else {
                        lootGrouRules.setNrRoundRobin(1);
                    }

                    int i = 0;
                    for (Player p : groupMembers) {
                        i++;
                        if (i == lootGrouRules.getNrRoundRobin()) {
                            winningPlayers.add(p);
                            setItemsToWinner(droppedItems, p.getObjectId());
                            genesis = p;
                            break;
                        }
                    }
                    break;
                case FREEFORALL:
                    winningPlayers = groupMembers;
                    break;
                case LEADER:
                    Player leader = player.isInGroup2() ? player.getPlayerGroup2().getLeaderObject() : player.getPlayerAlliance2().getLeaderObject();
                    winningPlayers.add(leader);
                    setItemsToWinner(droppedItems, leader.getObjectId());
                    genesis = leader;
                    break;
            }

            for (Player member : winningPlayers) {
                dropMembers.add(member.getObjectId());
                dropPlayers.add(member);
            }
            DropNpc dropNpc = new DropNpc(npcObjId);
            dropRegistrationMap.put(npcObjId, dropNpc);
            dropNpc.setPlayersObjectId(dropMembers);
            dropNpc.setInRangePlayers(groupMembers);
            dropNpc.setGroupSize(groupMembers.size());
        } else {
            List<Integer> singlePlayer = new ArrayList<>();
            singlePlayer.add(player.getObjectId());
            dropPlayers.add(player);
            dropRegistrationMap.put(npcObjId, new DropNpc(npcObjId));
            dropRegistrationMap.get(npcObjId).setPlayersObjectId(singlePlayer);
        }

        float boostDropRate = npc.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f;

        boostDropRate += genesis.getCommonData().getCurrentReposteEnergy() > 0 ? 0.1f : 0f;

        boostDropRate += genesis.getCommonData().getCurrentSalvationPercent() > 0 ? 0.05f : 0f;

        boostDropRate += HouseInfo.of(genesis).typeIs(PALACE) ? 0.05f : 0f;

        boostDropRate += genesis.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f - 1f;

        float dropRate = genesis.getRates().getDropRate() * boostDropRate * dropChance / 100f;
        if (npcDrop != null) {
            index = npcDrop.dropCalculator(droppedItems, index, dropRate, genesis.getRace(), groupMembers);
        }

        currentDropMap.put(npcObjId, droppedItems);
//        if (!genesis.isInAlliance2()) {
            QuestService.getQuestDrop(droppedItems, index, npc, groupMembers, genesis);
//        }
        if (npc.getPosition().isInstanceMap()) {
            npc.getPosition().getWorldMapInstance().getInstanceHandler().onDropRegistered(npc);
        }
        npc.getAi2().onGeneralEvent(AIEventType.DROP_REGISTERED);

        for (Player p : dropPlayers) {
            p.sendPck(new SM_LOOT_STATUS(npcObjId, 0));
        }
        if (player.getPet() != null && player.getPet().getPetTemplate().getPetFunction(PetFunctionType.LOOT) != null
                && player.getPet().getCommonData().isLooting()) {
            player.sendPck(new SM_PET(true, npcObjId));
            Set<DropItem> drops = geCurrentDropMap().get(npcObjId);
            if (drops == null || drops.isEmpty()) {
                npc.getController().onDelete();
            } else {
                // hex1r0: required to iterate over copy coz of crap implementation!!!
                for (DropItem dropItem : drops.toArray(new DropItem[drops.size()])) {
                    DropService.getInstance().requestDropItem(player, npcObjId, dropItem.getIndex(), true);
                }
            }
            player.sendPck(new SM_PET(false, npcObjId));

            if (drops == null || drops.isEmpty()) {
                return;
            }
        }
        DropService.getInstance().scheduleFreeForAll(npcObjId);
    }

    public void setItemsToWinner(Set<DropItem> droppedItems, Integer obj) {
        for (DropItem dropItem : droppedItems) {
            if (!dropItem.getDropTemplate().isEachMember()) {
                dropItem.setPlayerObjId(obj);
            }
        }
    }

    public DropItem regDropItem(int index, int playerObjId, int npcId, int itemId, long count) {
        DropItem item = new DropItem(new Drop(itemId, 1, 1, 100, false));
        item.setPlayerObjId(playerObjId);
        item.setNpcObj(npcId);
        item.setCount(count);
        item.setIndex(index);
        return item;
    }

    /**
     * @return dropRegistrationMap
     */
    public Map<Integer, DropNpc> getDropRegistrationMap() {
        return dropRegistrationMap;
    }

    /**
     * @return currentDropMap
     */
    public Map<Integer, Set<DropItem>> geCurrentDropMap() {
        return currentDropMap;
    }

    public static DropRegistrationService getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final DropRegistrationService instance = new DropRegistrationService();
    }
}
