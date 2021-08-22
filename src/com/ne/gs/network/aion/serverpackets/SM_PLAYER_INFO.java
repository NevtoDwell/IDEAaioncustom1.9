/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.List;

import com.ne.commons.func.tuple.Tuple;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerAppearance;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.items.GodStone;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.team.legion.LegionEmblemType;
import com.ne.gs.modules.ffaloc.FFALoc;
import com.ne.gs.modules.housing.HouseInfo;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.utils.ChatUtil;

/**
 * This packet is displaying visible players.
 *
 * @author -Nemesiss-, Avol, srx47 modified cura
 */
public class SM_PLAYER_INFO extends AionServerPacket {

    /**
     * Visible player
     */
    private final Player player;
    private final boolean enemy;

    /**
     * Constructs new <tt>SM_PLAYER_INFO </tt> packet
     *
     * @param player
     *     actual player.
     * @param enemy
     */
    public SM_PLAYER_INFO(Player player, boolean enemy) {
        this.player = player;
        this.enemy = enemy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        Player activePlayer = con.getActivePlayer();
        if (activePlayer == null || player == null) {
            return;
        }
        PlayerCommonData pcd = player.getCommonData();
        int raceId;
        if (player.getAdminNeutral() > 1 || activePlayer.getAdminNeutral() > 1) {
            raceId = activePlayer.getRace().getRaceId();
        } else if (activePlayer.isEnemy(player)) {
            raceId = (activePlayer.getRace().getRaceId() == 0 ? 1 : 0);
        } else {
            raceId = player.getRace().getRaceId();
        }

        int genderId = pcd.getGender().getGenderId();
        PlayerAppearance playerAppearance = player.getPlayerAppearance();

        writeF(player.getX());// x
        writeF(player.getY());// y
        writeF(player.getZ());// z
        writeD(player.getObjectId());
        /**
         * A3 female asmodian A2 male asmodian A1 female elyos A0 male elyos
         */
        writeD(pcd.getTemplateId());
        /**
         * Transformed state - send transformed model id Regular state - send player model id (from common data)
         */
        int model = player.getTransformModel().getModelId();
        writeD(model != 0 ? model : pcd.getTemplateId());
        writeC(0x00); // new 2.0 Packet --- probably pet info?
        writeD(player.getTransformModel().getType().getId());
        writeC(enemy ? 0 : 38);

        writeC(raceId); // race
        writeC(pcd.getPlayerClass().getClassId());
        writeC(genderId); // sex
        writeH(player.getState());

        writeB(new byte[8]);

        writeC(player.getHeading());
        String name = player.getNewName();
        String nameFormat = player.getNameFormat();
        writeS(String.format(nameFormat, name)); // player name
        writeH(pcd.getTitleId()); // title id
        writeH(player.getCommonData().isHaveMentorFlag() ? 1 : 0);

        writeH(player.getCastingSkillId());

        if (player.isLegionMember()) {
            writeD(player.getLegion().getLegionId());
            writeC(player.getLegion().getLegionEmblem().getEmblemId());
            writeC(player.getLegion().getLegionEmblem().getEmblemType().getValue());
            writeC(player.getLegion().getLegionEmblem().getEmblemType() == LegionEmblemType.DEFAULT ? 0x00 : 0xFF);
            writeC(player.getLegion().getLegionEmblem().getColor_r());
            writeC(player.getLegion().getLegionEmblem().getColor_g());
            writeC(player.getLegion().getLegionEmblem().getColor_b());
            writeS(player.getCustomLegionName());
        } else {
           if (!player.getCustomLegionName().isEmpty()) {
                writeD(0);
                writeC(0);
                writeC(0);
                writeC(0);
                writeC(0);
                writeC(0);
                writeC(0);
                writeS(player.getCustomLegionName());
            } else {
                writeB(new byte[12]);
            }
        }
        int maxHp = player.getLifeStats().getMaxHp();
        int currHp = player.getLifeStats().getCurrentHp();
	    int curDp = player.getLifeStats().getCurrentDp();
        writeC(100 * currHp / maxHp);// %hp
        writeH(curDp);// current dp
        writeC(0x00);// unk (0x00)

        List<Item> items = player.getEquipment().getEquippedForAppearance();
        short mask = 0;
        for (Item item : items) {
            mask |= item.getEquipmentSlot();
        }

        writeH(mask);

        for (Item item : items) {
            if (item.getEquipmentSlot() < Short.MAX_VALUE * 2) {
                writeD(item.getItemSkinTemplate().getTemplateId());
                GodStone godStone = item.getGodStone();
                writeD(godStone != null ? godStone.getItemId() : 0);
                writeD(item.getItemColor());
                writeH(0x00);// unk (0x00)
            }
        }

        writeD(playerAppearance.getSkinRGB());
        writeD(playerAppearance.getHairRGB());
        writeD(playerAppearance.getEyeRGB());
        writeD(playerAppearance.getLipRGB());
        writeC(playerAppearance.getFace());
        writeC(playerAppearance.getHair());
        writeC(playerAppearance.getDeco());
        writeC(playerAppearance.getTattoo());
        writeC(playerAppearance.getFaceContour());
        writeC(playerAppearance.getExpression());

        writeC(5);// always 5 o0

        writeC(playerAppearance.getJawLine());
        writeC(playerAppearance.getForehead());

        writeC(playerAppearance.getEyeHeight());
        writeC(playerAppearance.getEyeSpace());
        writeC(playerAppearance.getEyeWidth());
        writeC(playerAppearance.getEyeSize());
        writeC(playerAppearance.getEyeShape());
        writeC(playerAppearance.getEyeAngle());

        writeC(playerAppearance.getBrowHeight());
        writeC(playerAppearance.getBrowAngle());
        writeC(playerAppearance.getBrowShape());

        writeC(playerAppearance.getNose());
        writeC(playerAppearance.getNoseBridge());
        writeC(playerAppearance.getNoseWidth());
        writeC(playerAppearance.getNoseTip());

        writeC(playerAppearance.getCheek());
        writeC(playerAppearance.getLipHeight());
        writeC(playerAppearance.getMouthSize());
        writeC(playerAppearance.getLipSize());
        writeC(playerAppearance.getSmile());
        writeC(playerAppearance.getLipShape());
        writeC(playerAppearance.getJawHeigh());
        writeC(playerAppearance.getChinJut());
        writeC(playerAppearance.getEarShape());
        writeC(playerAppearance.getHeadSize());
        // 1.5.x 0x00, shoulderSize, armLength, legLength (BYTE) after HeadSize

        writeC(playerAppearance.getNeck());
        writeC(playerAppearance.getNeckLength());
        writeC(playerAppearance.getShoulderSize());

        writeC(playerAppearance.getTorso());
        writeC(playerAppearance.getChest()); // only woman
        writeC(playerAppearance.getWaist());

        writeC(playerAppearance.getHips());
        writeC(playerAppearance.getArmThickness());
        writeC(playerAppearance.getHandSize());
        writeC(playerAppearance.getLegThicnkess());

        writeC(playerAppearance.getFootSize());
        writeC(playerAppearance.getFacialRate());

        writeC(0x00); // always 0
        writeC(playerAppearance.getArmLength());
        writeC(playerAppearance.getLegLength());
        writeC(playerAppearance.getShoulders());
        writeC(playerAppearance.getFaceShape());
        writeC(0x00); // always 0

        writeC(playerAppearance.getVoice());

        writeF(playerAppearance.getHeight());
        writeF(0.25f); // scale
        writeF(2.0f); // gravity or slide surface o_O
        writeF(player.getGameStats().getMovementSpeedFloat()); // move speed

        Stat2 attackSpeed = player.getGameStats().getAttackSpeed();
        writeH(attackSpeed.getBase());
        writeH(attackSpeed.getCurrent());
        writeC(player.getPortAnimation());

        writeS(player.hasStore() ? player.getStore().getStoreMessage() : "");// private store message

        /**
         * Movement
         */
        writeF(0);
        writeF(0);
        writeF(0);

        writeF(player.getX());// x
        writeF(player.getY());// y
        writeF(player.getZ());// z
        writeC(0x00); // move type

        if (player.isUsingFlyTeleport()) {
            writeD(player.getFlightTeleportId());
            writeD(player.getFlightDistance());
        } else if (player.isInPlayerMode(PlayerMode.WINDSTREAM_STARTED)) {
            writeD(player.windstreamPath.teleportId);
            writeD(player.windstreamPath.distance);
        }
        writeC(player.getVisualState()); // visualState
        writeS(player.getCommonData().getNote()); // note show in right down windows if your target on player

        writeH(player.getLevel()); // [level]
        writeH(player.getPlayerSettings().getDisplay()); // unk - 0x04
        writeH(player.getPlayerSettings().getDeny()); // unk - 0x00
        writeH(player.getImplementator().result(FFALoc.VisualRank.class, player));
        writeH(0x00); // unk - 0x01
        writeD(player.getTarget() == null ? 0 : player.getTarget().getObjectId());
        writeC(0); // suspect id
        writeD(0);
        writeC(player.isMentor() ? 1 : 0);
        writeD(HouseInfo.of(player).getId());
    }
}
