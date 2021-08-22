/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.DescId;
import com.ne.gs.model.siege.ArtifactLocation;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * Opens a yes/no question window on the client. Question based on the code given, defined in client_strings.xml
 *
 * @author Ben, avol, Lyahim
 */
public class SM_QUESTION_WINDOW extends AionServerPacket {

    public static final int STR_DUEL_DO_YOU_ACCEPT_REQUEST = 50028;
    public static final int STR_DUEL_DO_YOU_WITHDRAW_REQUEST = 50030;
    public static final int STR_PARTY_DO_YOU_ACCEPT_INVITATION = 60000;
    public static final int STR_PARTY_ALLIANCE_DO_YOU_ACCEPT_HIS_INVITATION = 70000;
    public static final int STR_PARTY_ALLIANCE_CHANGE_LOOT_TO_FREE_HE_ASKED = 70001;
    public static final int STR_PARTY_ALLIANCE_CHANGE_LOOT_TO_RANDOM_HE_ASKED = 70002;
    public static final int STR_PARTY_ALLIANCE_PICKUP_ITEM_HE_ASKED = 70003;
    public static final int STR_FORCE_DO_YOU_ACCEPT_INVITATION = 70004;
    public static final int STR_GUILD_CREATE_DO_YOU_ACCEPT_PAY = 80000;
    public static final int STR_GUILD_INVITE_DO_YOU_ACCEPT_INVITATION = 80001;
    public static final int STR_GUILD_TRANSFER_GUILDMASTER = 80005;
    public static final int STR_GUILD_DO_YOU_LEAVE = 80006;
    public static final int STR_GUILD_DO_YOU_BANISH = 80007;
    public static final int STR_GUILD_DISPERSE_STAYMODE = 80008;
    public static final int STR_GUILD_DISPERSE_STAYMODE_CANCEL = 80009;
    public static final int STR_GUILD_CHANGE_LEVEL_DO_YOU_ACCEPT_PAY = 80010;
    public static final int STR_GUILD_CHANGE_MASTER_DO_YOU_ACCEPT_OFFER = 80011;
    public static final int STR_BUY_SELL_CONFIRM_PURCHASE_EXCESSIVE_PRICE = 90000;
    public static final int STR_EXCHANGE_DO_YOU_ACCEPT_EXCHANGE = 90001;
    public static final int STR_QUEST_GIVEUP = 150000;
    public static final int STR_QUEST_GIVEUP_WHEN_DELETE_QUEST_ITEM = 150001;
    public static final int STR_ASK_RECOVER_EXPERIENCE = 160011;
    public static final int STR_ASK_REGISTER_RESURRECT_POINT = 160012;
    public static final int STR_ASK_GROUP_GATE_DO_YOU_ACCEPT_MOVE = 160014;
    public static final int STR_ASK_USE_ARTIFACT = 160016;
    public static final int STR_ASK_PASS_BY_GATE = 160017;
    public static final int STR_ASK_REGISTER_BINDSTONE = 160018;
    public static final int STR_ASK_PASS_BY_DIRECT_PORTAL = 160019;
    public static final int STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR = 160021;
    public static final int STR_ASK_DOOR_REPAIR_POPUPDIALOG = 160027;
    public static final int STR_ASK_JOIN_NEW_FACTION = 160033;
    public static final int STR_CONFIRM_LOOT = 900495;
    public static final int STR_WAREHOUSE_EXPAND_WARNING = 900686;
    public static final int STR_CRAFT_ADDSKILL_CONFIRM = 900852;
    public static final int STR_SUMMON_PARTY_DO_YOU_ACCEPT_REQUEST = 901721;
    public static final int STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM = 902050;
    public static final int STR_MSGBOX_UNION_INVITE_ME = 902249;
    public static final int STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND = 95006;
    public static final int STR_ITEM_CHARGE_ALL_CONFIRM = 903026;
    public static final int STR_ITEM_CHARGE2_ALL_CONFIRM = 904039;
    public static final int STR_ITEM_CHARGE_CONFIRM_SOME_ALREADY_CHARGED = 903028;
    public static final int STR_ASSEMBLY_ITEM_POPUP_CONFIRM = 903441;
    public static final int STR_HOUSING_TELEPORT_HOME_CONFIRM = 903533;
    public static final int STR_HOUSING_TELEPORT_BUDDY_CONFIRM = 903534;
    public static final int STR_HOUSING_TELEPORT_RANDOM_CONFIRM = 903535;
    public static final int STR_HOUSING_TELEPORT_GUILD_CONFIRM = 903536;
    public static final int STR_BUDDYLIST_ADD_BUDDY_REQUEST = 1300911;
    public static final int STR_EXCHANGE_HE_REJECTED_EXCHANGE = 1300354;
    private final int code;
    private final int senderId;
    private final int range;
    private final Object[] params;
    private ArtifactLocation artifact;

    /**
     * Creates a new <tt>SM_QUESTION_WINDOW<tt> packet
     *
     * @param code
     *     code The string code to display, found in client_strings.xml
     * @param senderId
     *     sender Object id
     * @param params
     *     params The parameters for the string, if any
     */
    public SM_QUESTION_WINDOW(int code, int senderId, int range, Object... params) {
        this.code = code;
        this.senderId = senderId;
        this.range = range;
        this.params = params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(code);

        if (code == STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM) {
            writeH(0x33);
            writeH(0x30);
            writeH(0x30);
            writeH(0x31);
            writeH(0x37);
            writeH(0x30);
            writeH(0x30);
            writeH(0x30);
            writeH(0x30);
            writeH(0x00);
        }

        for (Object param : params) {
            if (param instanceof DescId) {
                writeH(0x24);
                writeD(((DescId) param).getValue());
                writeH(0x00); // unk
            } else if (param instanceof ArtifactLocation) {
                artifact = (ArtifactLocation) param;
            } else {
                writeS(String.valueOf(param));
            }
        }

        // Guardian Stone Activation Window
        if (code == 160027) {
            writeD(0x00);
            writeD(0x00);
            writeD(0x00);
            writeH(0x00);
            writeC(1);
            writeD(senderId);
            writeD(0x05);
        }
        // ArtifactLocation Activation Window
        else if (code == 160028) {
            writeD(0x00);
            writeD(0x00);
            writeH(0x00);
            writeC(0x00);
            writeD(0x00);
            if (artifact == null) {
                writeD(0x00);
            } else {
                writeD(artifact.getCoolDown());// ArtifactLocation reuse
            }
        } else if (code == STR_BUDDYLIST_ADD_BUDDY_REQUEST) {
            writeB(new byte[17]);
        } else if (code == STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM) {
            writeD(0x00);
            writeH(0x00);
            writeC(0x01);
            writeD(senderId);
            writeD(0x05);
        } else {
            writeD(0x00);// unk
            writeD(0);
            writeH(0x00);// unk
            writeC(range > 0 ? 1 : 0);
            writeD(senderId);
            writeD(range);
        }
    }
}
