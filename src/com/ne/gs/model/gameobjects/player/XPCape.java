/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

/**
 * @author MrPoke
 */
public enum XPCape {
    _0(0),
    _1(130),
    // retail
    _2(284),
    // retail
    _3(418),
    // retail
    _4(561),
    // retail
    _5(721),
    // retail
    _6(970),
    _7(1200),
    _8(1450),
    _9(1750),
    _10(2007),
    // retail
    _11(2362),
    // retail
    _12(2592),
    _13(2909),
    _14(3336),
    _15(3899),
    _16(4896),
    _17(5555),
    _18(6721),
    _19(8169),
    _20(9947),
    _21(12108),
    _22(14035),
    // retail
    _23(17820),
    _24(21506),
    _25(25847),
    _26(30924),
    _27(36829),
    _28(43659),
    _29(51517),
    _30(60517),
    _31(70779),
    _32(82430),
    _33(95606),
    _34(110455),
    _35(127128),
    _36(
        145791),
    _37(166615),
    _38(189783),
    _39(215488),
    _40(243932),
    _41(275329),
    _42(309904),
    _43(347891),
    _44(389536),
    _45(435099),
    _46(484848),
    _47(
        539067),
    _48(598049),
    _49(662103),
    _50(731547),
    _51(806716),
    _52(887956),
    _53(975628),
    _54(1070106),
    _55(1171780),
    _56(1280906),
    _57(1397740),
    _58(
        1522538),
    _59(1655556),
    _60(1797050);
    private final int id;

    private XPCape(int id) {
        this.id = id;
    }

    public int value() {
        return id;
    }
}
