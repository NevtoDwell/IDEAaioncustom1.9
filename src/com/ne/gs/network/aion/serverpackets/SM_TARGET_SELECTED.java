/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_TARGET_SELECTED extends AionServerPacket {

  private int level;
  private int maxHp;
  private int currentHp;
  private int maxMp;
  private int currentMp;
  private int targetObjId;

  public SM_TARGET_SELECTED(Player player) {
    if (player != null) {
      if (player.getTarget() instanceof Creature) {
	Creature target = (Creature) player.getTarget();
	this.level = target.getLevel();
	this.maxHp = target.getLifeStats().getMaxHp();
	this.currentHp = target.getLifeStats().getCurrentHp();
	this.maxMp = target.getLifeStats().getMaxMp();
	this.currentMp = target.getLifeStats().getCurrentMp();
      } else {
	// TODO: check various gather on retail
	this.level = 0;
	this.maxHp = 0;
	this.currentHp = 0;
	this.maxMp = 0;
	this.currentMp = 0;
      }

      if (player.getTarget() != null) {
	targetObjId = player.getTarget().getObjectId();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void writeImpl(AionConnection con) {
    writeD(targetObjId);
    writeH(level);
    writeD(maxHp);
    writeD(currentHp);
    writeD(maxMp);//new 4.0
    writeD(currentMp);//new 4.0
  }

}
