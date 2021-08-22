/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.PlayerAppearance;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.items.GodStone;
import com.ne.gs.model.items.ItemSlot;
import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author AEJTester
 * @author Nemesiss
 * @author Niato
 */
public abstract class PlayerInfo extends AionServerPacket {

    private static final Logger log = LoggerFactory.getLogger(PlayerInfo.class);

    protected PlayerInfo() {
    }

    protected void writePlayerInfo(PlayerAccountData accPlData) {
        PlayerCommonData pbd = accPlData.getPlayerCommonData();
        int raceId = pbd.getRace().getRaceId();
        int genderId = pbd.getGender().getGenderId();
        PlayerAppearance playerAppearance = accPlData.getAppereance();
        writeD(pbd.getPlayerObjId());
        writeS(pbd.getName(), 52);
        writeD(genderId);
        writeD(raceId);
        writeD(pbd.getPlayerClass().getClassId());
        writeD(playerAppearance.getVoice());
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
        writeC(4);// always 4 o0
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
        writeC(playerAppearance.getShoulderSize()); // shoulderSize

        writeC(playerAppearance.getTorso());
        writeC(playerAppearance.getChest());
        writeC(playerAppearance.getWaist());
        writeC(playerAppearance.getHips());
        writeC(playerAppearance.getArmThickness());
        writeC(playerAppearance.getHandSize());
        writeC(playerAppearance.getLegThicnkess());
        writeC(playerAppearance.getFootSize());
        writeC(playerAppearance.getFacialRate());
        writeC(0x00); // 0x00
        writeC(playerAppearance.getArmLength()); // armLength
        writeC(playerAppearance.getLegLength()); // legLength
        writeC(playerAppearance.getShoulders());
        writeC(playerAppearance.getFaceShape());
        writeC(0x00); // always 0 may be acessLevel
        writeC(0x00); // always 0 - unk
        writeC(0x00);
        writeF(playerAppearance.getHeight());
        int raceSex = 100000 + raceId * 2 + genderId;
        writeD(raceSex);
        writeD(pbd.getPosition().getMapId());// mapid for preloading map
        writeF(pbd.getPosition().getX());
        writeF(pbd.getPosition().getY());
        writeF(pbd.getPosition().getZ());
        writeD(pbd.getPosition().getH());
        writeH(pbd.getLevel());
        writeH(0); // unk 2.5
        writeD(pbd.getTitleId());
        if (accPlData.isLegionMember()) {
            writeD(accPlData.getLegion().getLegionId());
            writeS(accPlData.getLegion().getLegionName(), 82);
        } else {
            writeB(new byte[86]);
        }

        writeH(accPlData.isLegionMember() ? 1 : 0);
        writeD((int) pbd.getLastOnline().getTime());
        int itemsDataSize = 0;
        // TODO figure out this part when fully equipped
        List<Item> items = accPlData.getEquipment();

        for (Item item : items) {
            if (itemsDataSize >= 208) {
                break;
            }

            ItemTemplate itemTemplate = item.getItemTemplate();
            if (itemTemplate == null) {
                log.warn("Missing item. PlayerId: " + pbd.getPlayerObjId() + " ItemId: " + item.getObjectId());
                continue;
            }
            long slot = item.getEquipmentSlot();
      //(items lenght * slot count = stupidNc byte[]) 16 * 13 = 208
      if (itemTemplate.isArmor() || itemTemplate.isWeapon()) {
	if (slot <= ItemSlot.PANTS.id()) {
	  // this flas is needed to show equipment on selection screen
	  //2 weapon, rings and earrings: 0 - show 50/50 1 - show right 2 - show left 3 - no show
	  writeC(slot == 2 || slot == 64 || slot == 256 ? 2 : 1);
	  writeD(item.getItemSkinTemplate().getTemplateId());
	  GodStone godStone = item.getGodStone();
	  writeD(godStone != null ? godStone.getItemId() : 0);
	  writeD(item.getItemColor());
	  itemsDataSize += 13;
	}
      }
        }

        byte[] stupidNc = new byte[208 - itemsDataSize];
        writeB(stupidNc);
        writeD(accPlData.getDeletionTimeInSeconds());
    }
}
