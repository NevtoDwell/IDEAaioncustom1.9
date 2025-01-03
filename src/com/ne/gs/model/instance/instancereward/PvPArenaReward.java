/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.instance.instancereward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.playerreward.PvPArenaPlayerReward;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.knownlist.Visitor;

import static ch.lambdaj.Lambda.*;

/**
 * @author xTz
 */
public class PvPArenaReward extends InstanceReward<PvPArenaPlayerReward> {

    private final Map<Integer, Boolean> positions = new HashMap<>();
    private final FastList<Integer> zones = new FastList<>();
    private int round = 1;
    private Integer zone;
    private final int bonusTime;
    private final int capPoints;
    private long instanceTime;
    private final WorldMapInstance instance;

    public PvPArenaReward(Integer mapId, int instanceId, WorldMapInstance instance) {
        super(mapId, instanceId);
        this.instance = instance;
        boolean isSolo = isSoloArena();
        capPoints = isSolo ? 14400 : 50000;
        bonusTime = isSolo ? 8100 : 12000;
        Collections.addAll(zones, isSolo ? new Integer[]{1, 2, 3, 4} : new Integer[]{1, 2, 3, 4, 5, 6});
        int positionSize = isSolo ? 4 : 12;
        for (int i = 1; i <= positionSize; i++) {
            positions.put(i, Boolean.FALSE);
        }
        setRndZone();
    }

    public final boolean isSoloArena() {
        return (mapId == 300430000) || (mapId == 300360000);
    }

    public int getCapPoints() {
        return capPoints;
    }

    public final void setRndZone() {
        int index = Rnd.get(zones.size());
        zone = zones.get(index);
        zones.remove(index);
    }

    private List<Integer> getFreePositions() {
        List<Integer> p = new ArrayList<>();
        for (Integer key : positions.keySet()) {
            if (!positions.get(key)) {
                p.add(key);
            }
        }
        return p;
    }

    public synchronized void setRndPosition(Integer object) {
        PvPArenaPlayerReward reward = getPlayerReward(object);
        int position = reward.getPosition();
        if (position != 0) {
            clearPosition(position, Boolean.FALSE);
        }
        Integer key = getFreePositions().get(Rnd.get(getFreePositions().size()));
        clearPosition(key, Boolean.TRUE);
        reward.setPosition(key);
    }

    public synchronized void clearPosition(int position, Boolean result) {
        positions.put(position, result);
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public synchronized void regPlayerReward(Player player) {
        if (!containPlayer(player.getObjectId())) {
            addPlayerReward(new PvPArenaPlayerReward(player, bonusTime));
        }
    }

    @Override
    public void addPlayerReward(PvPArenaPlayerReward reward) {
        super.addPlayerReward(reward);
    }

    @Override
    public PvPArenaPlayerReward getPlayerReward(Integer object) {
        return (PvPArenaPlayerReward) super.getPlayerReward(object);
    }

    public void portToPosition(Player player, boolean result) {
        Integer object = player.getObjectId();
        regPlayerReward(player);
        setRndPosition(object);
        PvPArenaPlayerReward playerReward = getPlayerReward(object);
        if (result) {
            playerReward.endBoostMoraleEffect();
        }
        playerReward.applyBoostMoraleEffect();
        Integer position = playerReward.getPosition();
        if (mapId == 300430000 || mapId == 300360000) {
            switch (position) {
                case 1:
                    switch (zone) {
                        case 1:
                            teleport(player, 1841.294f, 1041.223f, 338.20056f, (byte) 15);
                            break;
                        case 2:
                            teleport(player, 278.18478f, 1265.8389f, 263.1712f, (byte) 73);
                            break;
                        case 3:
                            teleport(player, 709.78845f, 1766.1855f, 183.43953f, (byte) 60);
                            break;
                        case 4:
                            teleport(player, 1817.1067f, 1737.4899f, 311.49692f, (byte) 1);
                            break;
                    }
                    break;
                case 2:
                    switch (zone) {
                        case 1:
                            teleport(player, 1869.4803f, 1041.8444f, 337.9918f, (byte) 43);
                            break;
                        case 2:
                            teleport(player, 251.03516f, 1297.7039f, 248.11426f, (byte) 105);
                            break;
                        case 3:
                            teleport(player, 693.93176f, 1761.0234f, 196.12753f, (byte) 21);
                            break;
                        case 4:
                            teleport(player, 1851.6932f, 1765.4813f, 305.23187f, (byte) 90);
                            break;
                    }
                    break;
                case 3:
                    switch (zone) {
                        case 1:
                            teleport(player, 1869.0569f, 1069.1344f, 337.6657f, (byte) 71);
                            break;
                        case 2:
                            teleport(player, 315.8269f, 1221.0648f, 263.4517f, (byte) 51);
                            break;
                        case 3:
                            teleport(player, 686.09247f, 1756.8987f, 163.4386f, (byte) 25);
                            break;
                        case 4:
                            teleport(player, 1851.7856f, 1709.3085f, 305.23566f, (byte) 31);
                            break;
                    }
                    break;
                case 4:
                    switch (zone) {
                        case 1:
                            teleport(player, 1841.7906f, 1069.6471f, 338.10706f, (byte) 107);
                            break;
                        case 2:
                            teleport(player, 346.1267f, 1185.1802f, 244.43742f, (byte) 44);
                            break;
                        case 3:
                            teleport(player, 693.11945f, 1771.6886f, 236.5583f, (byte) 17);
                            break;
                        case 4:
                            teleport(player, 1887.0206f, 1737.6492f, 311.49692f, (byte) 62);
                            break;
                    }
                    break;
            }
        } else {
            switch (position) {
                case 1:
                    switch (zone) {
                        case 1:
                            teleport(player, 1936.75f, 943.5f, 222.40979f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 674.22394f, 1771.7374f, 221.87901f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1905.0468f, 1257.5624f, 288.64221f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1378.2219f, 1067.3834f, 340.29468f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 685.27051f, 287.09622f, 514.20245f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1870.8004f, 1700.0099f, 300.64191f, (byte) 0);
                            break;
                    }
                    break;
                case 2:
                    switch (zone) {
                        case 1:
                            teleport(player, 1961.5468f, 930.42542f, 222.22997f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 631.9718f, 1777.4031f, 185.03346f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1905.8645f, 1235.8971f, 288.47339f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1375.5717f, 1096.2384f, 340.04309f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 680.8587f, 287.26782f, 512.21454f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1809.6421f, 1704.156f, 300.66394f, (byte) 0);
                            break;
                    }
                    break;
                case 3:
                    switch (zone) {
                        case 1:
                            teleport(player, 1936.75f, 962.65997f, 222.54306f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 676.61804f, 1785.7284f, 222.07115f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1937.2339f, 1221.2653f, 269.76581f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1300.9225f, 1058.9489f, 340.4682f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 690.13184f, 244.60457f, 514.25085f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1885.8636f, 1738.2178f, 300.64774f, (byte) 0);
                            break;
                    }
                    break;
                case 4:
                    switch (zone) {
                        case 1:
                            teleport(player, 1961.6898f, 943.5f, 222.65001f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 669.04681f, 1769.1235f, 222.166f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1929.1152f, 1237.0225f, 270.32001f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1379.5814f, 1076.59f, 340.73944f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 685.57544f, 240.64261f, 513.90967f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1814.7386f, 1765.3486f, 300.63184f, (byte) 0);
                            break;
                    }
                    break;
                case 5:
                    switch (zone) {
                        case 1:
                            teleport(player, 1890.7455f, 1001.882f, 230.59641f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 665.71942f, 1772.1372f, 223.36128f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1934.9464f, 1263.8007f, 272.70129f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1302.1024f, 1037.2123f, 340.72864f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 689.70703f, 248.95837f, 514.29877f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1826.0723f, 1689.4574f, 311.47198f, (byte) 0);
                            break;
                    }
                    break;
                case 6:
                    switch (zone) {
                        case 1:
                            teleport(player, 1983.3049f, 1002.3879f, 228.68951f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 720.59412f, 1777.4561f, 174.83665f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1877.8018f, 1205.6583f, 269.97739f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1299.4155f, 1048.0157f, 341.02997f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 689.65814f, 239.0927f, 514.0719f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1860.2178f, 1776.2961f, 311.39948f, (byte) 0);
                            break;
                    }
                    break;
                case 7:
                    switch (zone) {
                        case 1:
                            teleport(player, 1936.75f, 930.6394f, 222.22997f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 671.78864f, 1785.5397f, 223.25864f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1929.8038f, 1157.0305f, 281.04517f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1378.1992f, 1052.0417f, 339.97583f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 691.20624f, 279.70129f, 514.32678f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1837.5425f, 1775.2424f, 300.62946f, (byte) 0);
                            break;
                    }
                    break;
                case 8:
                    switch (zone) {
                        case 1:
                            teleport(player, 1943.397f, 885.37384f, 231.46317f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 680.71082f, 1784.8301f, 221.63185f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1960.0089f, 1259.953f, 288.6601f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1291.4095f, 1088.9802f, 339.99475f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 686.07214f, 245.58424f, 514.08667f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1799.6967f, 1749.8506f, 311.4411f, (byte) 0);
                            break;
                    }
                    break;
                case 9:
                    switch (zone) {
                        case 1:
                            teleport(player, 1961.47f, 962.65704f, 222.57222f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 679.51208f, 1772.2958f, 221.57156f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1925.5928f, 1224.0499f, 269.80841f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1379.8954f, 1086.8831f, 340.6073f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 688.94763f, 287.30219f, 514.50201f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1886.9553f, 1715.6755f, 311.42749f, (byte) 0);
                            break;
                    }
                    break;
                case 10:
                    switch (zone) {
                        case 1:
                            teleport(player, 1936.752f, 949.46191f, 222.65584f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 682.93726f, 1777.0594f, 221.07982f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1940.4576f, 1232.8687f, 270.27747f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1290.8484f, 1079.3834f, 340.8075f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 690.72253f, 283.63785f, 514.38959f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1799.9487f, 1727.0955f, 300.6908f, (byte) 0);
                            break;
                    }
                    break;
                case 11:
                    switch (zone) {
                        case 1:
                            teleport(player, 2006.7396f, 891.59174f, 230.5414f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 665.55548f, 1776.9498f, 222.78941f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1978.6932f, 1282.8806f, 286.25754f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1295.7224f, 1099.4498f, 340.24512f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 685.85437f, 281.09637f, 514.96991f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1847.9542f, 1689.8381f, 300.81805f, (byte) 0);
                            break;
                    }
                    break;
                case 12:
                    switch (zone) {
                        case 1:
                            teleport(player, 1961.7798f, 949.46002f, 222.70183f, (byte) 0);
                            break;
                        case 2:
                            teleport(player, 669.14801f, 1781.4385f, 222.5672f, (byte) 0);
                            break;
                        case 3:
                            teleport(player, 1989.6035f, 1192.4663f, 273.16217f, (byte) 0);
                            break;
                        case 4:
                            teleport(player, 1381.2518f, 1060.0963f, 340.26367f, (byte) 0);
                            break;
                        case 5:
                            teleport(player, 680.53888f, 240.78772f, 511.87012f, (byte) 0);
                            break;
                        case 6:
                            teleport(player, 1875.7604f, 1761.2601f, 300.68347f, (byte) 0);
                            break;
                    }
                    break;
            }
        }
    }

    public int getRankBonus(int playerRank) {
        if (!isRewarded()) {
            return 0;
        }
        switch (mapId) {
            case 300430000:
            case 300360000:
                switch (playerRank) {
                    case 0:
                        return 48700;
                    case 1:
                        return 20300;
                }
                break;
            case 300350000:
            case 300420000:
                switch (playerRank) {
                    case 0:
                        return 75900;
                    case 1:
                        return 57000;
                    case 2:
                        return 47900;
                    case 3:
                        return 39500;
                    case 4:
                        return 31100;
                    case 5:
                        return 22700;
                    default:
                        return 14200;
                }
        }
        return 0;
    }

    public List<PvPArenaPlayerReward> sortPoints() {
        return sort(getPlayersInside(), on(PvPArenaPlayerReward.class).getPoints(), new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 != null ? o2.compareTo(o1) : -o1.compareTo(o2);
            }

        });
    }

    private void teleport(Player player, float x, float y, float z, byte h) {
        TeleportService.teleportTo(player, mapId, instanceId, x, y, z, h);
    }

    public boolean canRewardOpportunityToken(PvPArenaPlayerReward rewardedPlayer) {
        if (rewardedPlayer != null) {
            int rank = getRank(rewardedPlayer.getPoints());
            return isSoloArena() && rank == 1 || rank > 2;
        }
        return false;
    }

    public int getRank(int points) {
        int rank = -1;
        for (PvPArenaPlayerReward reward : sortPoints()) {
            if (reward.getPoints() >= points) {
                rank++;
            }
        }

        return rank;
    }

    public boolean hasCapPoints() {
        if (isSoloArena() && maxFrom(getPlayersInside()).getPoints() - minFrom(getPlayersInside()).getPoints() >= 1500) {
            return true;
        }
        return maxFrom(getPlayersInside()).getPoints() >= capPoints;
    }

    public boolean canRewarded() {
        return mapId == 300350000 || mapId == 300360000;
    }

    public int getNpcBonus(int npcId) {
        switch (npcId) {
			case 701188:
				return 1750;
            case 218690:
            case 218703:
            case 218716:
                return 1500;
            case 218682:
            case 218695:
            case 218708:
                return 1250;
            case 218685:
            case 218686:
            case 218687:
            case 218698:
            case 218699:
            case 218700:
            case 218711:
            case 218712:
            case 218713:
            case 701169:
            case 701170:
            case 701171:
            case 701172:
                return 250;
            case 218806:
            case 218807:
            case 218808:
            case 701317:
            case 701318:
            case 701319:
                return 500;
            case 218688:
            case 218701:
            case 218714:
                return 650;
            case 218689:
            case 218702:
            case 218715:
            case 701181:
            case 701195:
            case 701209:
                return 750;
            case 218683:
            case 218684:
            case 218693:
            case 218696:
            case 218697:
            case 218706:
            case 218709:
            case 218710:
            case 218719:
            case 701215:
            case 701216:
            case 701220:
            case 701221:
            case 701225:
            case 701226:
                return 100;
        }
        return 0;
    }

    public int getNpcBonusSkill(int npcId) {
        switch (npcId) {
            case 701175:
            case 701176:
            case 701177:
            case 701178:
                return 5134130;
            case 701189:
            case 701190:
            case 701191:
            case 701192:
                return 5233468;
            case 701317:
                return 5211442;
            case 701318:
                return 5211447;
            case 701319:
                return 5211452;
            case 701220:
                return 5133623;
        }
        return 0;
    }

    private int getTime() {
        long result = System.currentTimeMillis() - instanceTime;
        if (isRewarded()) {
            return 0;
        }
        if (result < 120000) {
            return (int) (120000 - result);
        }

        return (int) (180000 * getRound() - (result - 120000));
    }

    public void setInstanceStartTime() {
        instanceTime = System.currentTimeMillis();
    }

    public void sendPacket() {
        instance.doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                player.sendPck(new SM_INSTANCE_SCORE(getTime(), getInstanceReward()));
            }
        });
    }

    private InstanceReward<?> getInstanceReward() {
        return this;
    }

    @Override
    public void clear() {
        super.clear();
        positions.clear();
    }
}
