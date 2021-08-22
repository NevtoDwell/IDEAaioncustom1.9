/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.model;

/**
 *
 * @author userd
 */
public enum EventRankType {

  NONE(0),
  BG_1x1(1),
  BG_2x2(2),
  BG_3x3(3),
  BG_6x6(4),
  BG_SURVIVOR_1x1(5),
  BG_SURVIVOR_2x2(6),
  BG_SURVIVOR_3x3(7),
  BG_SURVIVOR_6x6(8),
  BG_KILL_COUNT_2x2(9),
  BG_KILL_COUNT_3x3(10),
  BG_KILL_COUNT_6x6(11),
  RVR(20),
  FFA(21),
  MIX_FIGHT(22),
  DREDGION(30),
  BATTLE_OF_GODS(31),
  SYSTEM_TAKE(32),
  RANK_1x1(100),
  RANK_2x2(101),
  RANK_3x3(102);

  private int id;

  private EventRankType(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public static EventRankType getEventRankType(int id) {
    for (EventRankType type : values()) {
      if (type.id == id) {
	return type;
      }
    }
    return EventRankType.NONE;
  }
}
