/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

public enum DialogAction {
    ERROR(-1),
    NULL(1),
    BUY(2),
    SELL(3),
    OPEN_STIGMA_WINDOW(4),
    CREATE_LEGION(5),
    DISPERSE_LEGION(6),
    RECREATE_LEGION(7),
    RESURRECT_PET(19),
    RETRIEVE_CHAR_WAREHOUSE(20),
    DEPOSIT_CHAR_WAREHOUSE(21),
    RETRIEVE_ACCOUNT_WAREHOUSE(22),
    DEPOSIT_ACCOUNT_WAREHOUSE(23),
    OPEN_VENDOR(28),
    RESURRECT_BIND(29),
    RECOVERY(30),
    ENTER_PVP(31),
    LEAVE_PVP(32),
    OPEN_POSTBOX(33),
    GIVE_ITEM_PROC(36),
    REMOVE_MANASTONE(37),
    CHANGE_ITEM_SKIN(38),
    AIRLINE_SERVICE(39),
    GATHER_SKILL_LEVELUP(40),
    COMBINE_SKILL_LEVELUP(41),
    EXTEND_INVENTORY(42),
    EXTEND_CHAR_WAREHOUSE(43),
    EXTEND_ACCOUNT_WAREHOUSE(44),
    LEGION_LEVELUP(45),
    LEGION_CREATE_EMBLEM(46),
    LEGION_CHANGE_EMBLEM(47),
    OPEN_LEGION_WAREHOUSE(48),
    OPEN_PERSONAL_WAREHOUSE(49),
    BUY_BY_AP(50),
    CLOSE_LEGION_WAREHOUSE(51),
    PASS_DOORMAN(52),
    CRAFT(53),
    EXCHANGE_COIN(54),
    SHOW_MOVIE(55),
    EDIT_CHARACTER(56),
    EDIT_GENDER(57),
    MATCH_MAKER(58),
    MAKE_MERCENARY(59),
    INSTANCE_ENTRY(60),
    COMPOUND_WEAPON(61),
    DECOMPOUND_WEAPON(62),
    FACTION_JOIN(63),
    FACTION_SEPARATE(64),
    BUY_AGAIN(65),
    PET_ADOPT(66),
    PET_ABANDON(67),
    HOUSING_BUILD(68),
    HOUSING_DESTRUCT(69),
    CHARGE_ITEM_SINGLE(70),
    CHARGE_ITEM_MULTI(71),
    INSTANCE_PARTY_MATCH(72),
    TRADE_IN(73),
    GIVEUP_CRAFT_EXPERT(74),
    GIVEUP_CRAFT_MASTER(75),
    HOUSING_FRIENDLIST(76),
    HOUSING_RANDOM_TELEPORT(77),
    HOUSING_PERSONAL_INS_TELEPORT(78),
    HOUSING_PERSONAL_AUCTION(79),
    HOUSING_PAY_RENT(80),
    HOUSING_KICK(81),
    HOUSING_CHANGE_BUILDING(82),
    HOUSING_CONFIG(83),
    HOUSING_GIVEUP(84),
    HOUSING_CANCEL_GIVEUP(85),
    HOUSING_CREATE_PERSONAL_INS(86);

    private final int id;

    private DialogAction(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}