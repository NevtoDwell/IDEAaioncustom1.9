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
import javolution.util.FastMap;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.Filter;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.FindGroup;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.callback.AllianceCallbacks;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.callback.GroupCallbacks;
import com.ne.gs.network.aion.serverpackets.SM_FIND_GROUP;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;

/**
 * Find Group Service
 *
 * @author cura, MrPoke
 */
public class FindGroupService {

    private final FastMap<Integer, FindGroup> elyosRecruitFindGroups = new FastMap<Integer, FindGroup>().shared();
    private final FastMap<Integer, FindGroup> elyosApplyFindGroups = new FastMap<Integer, FindGroup>().shared();
    private final FastMap<Integer, FindGroup> asmodianRecruitFindGroups = new FastMap<Integer, FindGroup>().shared();
    private final FastMap<Integer, FindGroup> asmodianApplyFindGroups = new FastMap<Integer, FindGroup>().shared();

    private FindGroupService() {
        EventNotifier.GLOBAL.attach(new GroupBeforeEnter());
        EventNotifier.GLOBAL.attach(new GroupAfterEnter());
        EventNotifier.GLOBAL.attach(new GroupBeforeDisband());
        EventNotifier.GLOBAL.attach(new GroupAfterCreate());
        EventNotifier.GLOBAL.attach(new AllianceBeforeEnter());
        EventNotifier.GLOBAL.attach(new AllianceAfterEnter());
        EventNotifier.GLOBAL.attach(new AllianceBeforeDisband());
        EventNotifier.GLOBAL.attach(new AllianceAfterCreate());
    }

    public void addFindGroupList(Player player, int action, String message, int groupType) {
        AionObject object;
        if (player.isInTeam()) {
            object = player.getCurrentTeam();
        } else {
            object = player;
        }

        FindGroup findGroup = new FindGroup(object, message, groupType);
        int objectId = object.getObjectId();
        switch (player.getRace()) {
            case ELYOS:
                switch (action) {
                    case 0x02:
                        elyosRecruitFindGroups.put(objectId, findGroup);
                        player.sendPck(new SM_SYSTEM_MESSAGE(1400392));
                        break;
                    case 0x06:
                        elyosApplyFindGroups.put(objectId, findGroup);
                        player.sendPck(new SM_SYSTEM_MESSAGE(1400393));
                        break;
                }
                break;
            case ASMODIANS:
                switch (action) {
                    case 0x02:
                        asmodianRecruitFindGroups.put(objectId, findGroup);
                        player.sendPck(new SM_SYSTEM_MESSAGE(1400392));
                        break;
                    case 0x06:
                        asmodianApplyFindGroups.put(objectId, findGroup);
                        player.sendPck(new SM_SYSTEM_MESSAGE(1400393));
                        break;
                }
                break;
        }

        Collection<FindGroup> findGroupList = new ArrayList<>();
        findGroupList.add(findGroup);

        player.sendPck(new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), findGroupList));
    }

    public void updateFindGroupList(Player player, String message, int objectId) {
        FindGroup findGroup;

        switch (player.getRace()) {
            case ELYOS:
                findGroup = elyosRecruitFindGroups.get(objectId);
                findGroup.setMessage(message);
                break;
            case ASMODIANS:
                findGroup = asmodianRecruitFindGroups.get(objectId);
                findGroup.setMessage(message);
                break;
        }
    }

    public Collection<FindGroup> getFindGroups(Race race, int action) {
        switch (race) {
            case ELYOS:
                switch (action) {
                    case 0x00:
                        return elyosRecruitFindGroups.values();
                    case 0x04:
                        return elyosApplyFindGroups.values();
                }
                break;
            case ASMODIANS:
                switch (action) {
                    case 0x00:
                        return asmodianRecruitFindGroups.values();
                    case 0x04:
                        return asmodianApplyFindGroups.values();
                }
                break;
        }
        return null;
    }

    public void sendFindGroups(Player player, int action) {
        player.sendPck(new SM_FIND_GROUP(action, (int) (System.currentTimeMillis() / 1000), getFindGroups(player.getRace(), action)));
    }

    public FindGroup removeFindGroup(final Race race, int action, int playerObjId) {
        FindGroup findGroup = null;
        switch (race) {
            case ELYOS:
                switch (action) {
                    case 0x00:
                        findGroup = elyosRecruitFindGroups.remove(playerObjId);
                        break;
                    case 0x04:
                        findGroup = elyosApplyFindGroups.remove(playerObjId);
                        break;
                }
                break;
            case ASMODIANS:
                switch (action) {
                    case 0x00:
                        findGroup = asmodianRecruitFindGroups.remove(playerObjId);
                        break;
                    case 0x04:
                        findGroup = asmodianApplyFindGroups.remove(playerObjId);
                        break;
                }
                break;
        }
        if (findGroup != null) {
            PacketSendUtility.broadcastFilteredPacket(new SM_FIND_GROUP(action + 1, playerObjId, findGroup.getUnk()), new Filter<Player>() {
                @Override
                public boolean accept(Player object) {
                    return race == object.getRace();
                }
            });
        }
        return findGroup;
    }

    public void clean() {
        cleanMap(elyosRecruitFindGroups, Race.ELYOS, 0x00);
        cleanMap(elyosApplyFindGroups, Race.ELYOS, 0x04);
        cleanMap(asmodianRecruitFindGroups, Race.ASMODIANS, 0x00);
        cleanMap(asmodianApplyFindGroups, Race.ASMODIANS, 0x04);
    }

    private void cleanMap(FastMap<Integer, FindGroup> map, Race race, int action) {
        for (FindGroup group : map.values()) {
            if (group.getLastUpdate() + 60 * 60 < System.currentTimeMillis() / 1000) {
                removeFindGroup(race, action, group.getObjectId());
            }
        }
    }

    public static FindGroupService getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final FindGroupService instance = new FindGroupService();
    }

    private static class GroupBeforeEnter extends GroupCallbacks.BeforeEnter {
        @Override
        public Object onEvent(@NotNull Tuple2<PlayerGroup, Player> e) {
            FindGroupService.getInstance().removeFindGroup(e._2.getRace(), 0x00, e._2.getObjectId());
            FindGroupService.getInstance().removeFindGroup(e._2.getRace(), 0x04, e._2.getObjectId());
            return null;
        }
    }

    private static class GroupAfterEnter extends GroupCallbacks.AfterEnter {
        @Override
        public Object onEvent(@NotNull Tuple2<PlayerGroup, Player> e) {
            if (e._1.isFull()) {
                FindGroupService.getInstance().removeFindGroup(e._1.getRace(), 0, e._1.getObjectId());
            }
            return null;
        }
    }

    private static class GroupBeforeDisband extends GroupCallbacks.BeforeDisband {
        @Override
        public Object onEvent(@NotNull PlayerGroup e) {
            FindGroupService.getInstance().removeFindGroup(e.getRace(), 0, e.getTeamId());
            return null;
        }
    }

    private static class GroupAfterCreate extends GroupCallbacks.AfterCreate {
        @Override
        public Object onEvent(@NotNull Player e) {
            FindGroup inviterFindGroup =
                FindGroupService.getInstance().removeFindGroup(e.getRace(), 0x00, e.getObjectId());
            if (inviterFindGroup == null) {
                inviterFindGroup =
                    FindGroupService.getInstance().removeFindGroup(e.getRace(), 0x04, e.getObjectId());
            }
            if (inviterFindGroup != null) {
                FindGroupService.getInstance()
                                .addFindGroupList(e, 0x02, inviterFindGroup.getMessage(), inviterFindGroup.getGroupType());
            }
            return null;
        }
    }

    static class AllianceBeforeDisband extends AllianceCallbacks.BeforeDisband {
        @Override
        public Object onEvent(@NotNull PlayerAlliance e) {
            FindGroupService.getInstance().removeFindGroup(e.getRace(), 0, e.getTeamId());
            return null;
        }
    }

    static class AllianceAfterCreate extends AllianceCallbacks.AfterCreate {
        @Override
        public Object onEvent(@NotNull Player e) {
            FindGroup inviterFindGroup =
                FindGroupService.getInstance().removeFindGroup(e.getRace(), 0x00, e.getObjectId());
            if (inviterFindGroup == null) {
                inviterFindGroup =
                    FindGroupService.getInstance().removeFindGroup(e.getRace(), 0x04, e.getObjectId());
            }
            if (inviterFindGroup != null) {
                FindGroupService.getInstance()
                                .addFindGroupList(e, 0x02, inviterFindGroup.getMessage(), inviterFindGroup.getGroupType());
            }
            return null;
        }
    }

    static class AllianceBeforeEnter extends AllianceCallbacks.BeforeEnter {
        @Override
        public Object onEvent(@NotNull Tuple2<PlayerAlliance, Player> e) {
            FindGroupService.getInstance().removeFindGroup(e._2.getRace(), 0x00, e._2.getObjectId());
            FindGroupService.getInstance().removeFindGroup(e._2.getRace(), 0x04, e._2.getObjectId());
            return null;
        }
    }

    static class AllianceAfterEnter extends AllianceCallbacks.AfterEnter {
        @Override
        public Object onEvent(@NotNull Tuple2<PlayerAlliance, Player> e) {
            if (e._1.isFull()) {
                FindGroupService.getInstance().removeFindGroup(e._1.getRace(), 0, e._1.getObjectId());
            }
            return null;
        }
    }
}
