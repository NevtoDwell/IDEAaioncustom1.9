/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.world;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.zone.ZoneClassName;

/**
 *
 * @author userd
 */
public enum InstanceType {

  NORMAL,
  FFA,
  PVP,
  TVT,
  LEGION_WAR,
  PEACE,
  RATING;

  public static boolean isIn(Player player, InstanceType type) {
    return player.getPosition().getWorldMapInstance().getInstanceType() == type;
  }

  public static boolean isInEventInstance(Player player) {
    return player.getPosition().getWorldMapInstance().getInstanceType() != InstanceType.NORMAL;
  }

  public static boolean isInBattleInstance(Player player) {
    return isInTVT(player) || isInFFA(player) || isInLegionWar(player) || isInPVP(player);
  }

  public static boolean isInTVT(Player player) {
    return player.getPosition().getWorldMapInstance().isTVTInstance() && !player.isInsideZoneClassName(ZoneClassName.RACE);
  }

  public static boolean isInFFA(Player player) {
    return player.getPosition().getWorldMapInstance().isFFAInstance();
  }

  public static boolean isInPVP(Player player) {
    return player.getPosition().getWorldMapInstance().isPVPInstance();
  }

  public static boolean isInLegionWar(Player player) {
    return player.getPosition().getWorldMapInstance().isLegionWarInstance();
  }

  public static boolean isInPeace(Player player) {
    return player.getPosition().getWorldMapInstance().isPeaceInstance();
  }

  public static boolean isInRating(Player player) {
    return player.getPosition().getWorldMapInstance().isRatingInstance();
  }
}
