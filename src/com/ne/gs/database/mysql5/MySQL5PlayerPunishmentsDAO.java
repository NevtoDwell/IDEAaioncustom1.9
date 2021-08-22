/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.mysql5;

import com.ne.commons.database.DB;
import com.ne.commons.database.IUStH;
import com.ne.commons.database.ParamReadStH;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerPunishmentsDAO;
import com.ne.gs.model.account.CharacterBanInfo;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.PunishmentService.PunishmentType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author lord_rex, Cura, nrg
 */
public class MySQL5PlayerPunishmentsDAO extends PlayerPunishmentsDAO {

    public static final String SELECT_QUERY = "SELECT `player_id`, `start_time`, `duration`, `reason` FROM `player_punishments` WHERE `player_id`=? AND `punishment_type`=?";
    public static final String UPDATE_QUERY = "UPDATE `player_punishments` SET `duration`=? WHERE `player_id`=? AND `punishment_type`=?";
    public static final String REPLACE_QUERY = "REPLACE INTO `player_punishments` VALUES (?,?,?,?,?)";
    public static final String DELETE_QUERY = "DELETE FROM `player_punishments` WHERE `player_id`=? AND `punishment_type`=?";

    @Override
    public void loadPlayerPunishments(final Player player, final PunishmentType punishmentType) {
        DB.select(SELECT_QUERY, new ParamReadStH() {

            @Override
            public void setParams(PreparedStatement ps) throws SQLException {
                ps.setInt(1, player.getObjectId());
                ps.setString(2, punishmentType.toString());
            }

            @Override
            public void handleRead(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    if (punishmentType == PunishmentType.PRISON) {
                        player.setPrisonTimer(rs.getLong("duration") * 1000);
                        player.setPrisonReason(rs.getString("reason"));
                    } else if (punishmentType == PunishmentType.GATHER) {
                        player.setGatherableTimer(rs.getLong("duration") * 1000);
                    } else if (punishmentType == PunishmentType.GAG) {
                        player.setGagTimer(rs.getLong("duration") * 1000);
                        player.setGagReason(rs.getString("reason"));
                    }
                }
            }
        });
    }

    @Override
    public void storePlayerPunishments(final Player player, final PunishmentType punishmentType) {
        DB.insertUpdate(UPDATE_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
                if (punishmentType == PunishmentType.PRISON) {
                    ps.setLong(1, player.getPrisonTimer() / 1000);
                } else if (punishmentType == PunishmentType.GATHER) {
                    ps.setLong(1, (player.getGatherableTimer() - (System.currentTimeMillis() - player.getStopGatherable())) / 1000);
                } else if (punishmentType == PunishmentType.GAG) {
                    ps.setLong(1, player.getGagTimer() / 1000);
                }
                ps.setInt(2, player.getObjectId());
                ps.setString(3, punishmentType.toString());
                ps.execute();
            }
        });
    }

    @Override
    public void punishPlayer(final int playerId, final PunishmentType punishmentType, final long duration, final String reason) {
        DB.insertUpdate(REPLACE_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, playerId);
                ps.setString(2, punishmentType.toString());
                ps.setLong(3, System.currentTimeMillis() / 1000);
                ps.setLong(4, duration);
                ps.setString(5, reason);
                ps.execute();
            }
        });
    }

    @Override
    public void punishPlayer(Player player, PunishmentType punishmentType, String reason) {
        if (punishmentType == PunishmentType.PRISON) {
            punishPlayer(player.getObjectId(), punishmentType, player.getPrisonTimer() / 1000, reason);
        } else if (punishmentType == PunishmentType.GATHER) {
            punishPlayer(player.getObjectId(), punishmentType, player.getGatherableTimer() / 1000, reason);
        } else if (punishmentType == PunishmentType.GAG) {
            punishPlayer(player.getObjectId(), punishmentType, player.getGagTimer() / 1000, reason);
        }
    }

    @Override
    public void unpunishPlayer(final int playerId, final PunishmentType punishmentType) {
        DB.insertUpdate(DELETE_QUERY, new IUStH() {

            @Override
            public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
                ps.setInt(1, playerId);
                ps.setString(2, punishmentType.toString());
                ps.execute();
            }
        });
    }

    @Override
    public CharacterBanInfo getCharBanInfo(final int playerId) {
        final CharacterBanInfo[] charBan = new CharacterBanInfo[1];
        DB.select(SELECT_QUERY, new ParamReadStH() {

            @Override
            public void setParams(PreparedStatement ps) throws SQLException {
                ps.setInt(1, playerId);
                ps.setString(2, PunishmentType.CHARBAN.toString());
            }

            @Override
            public void handleRead(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    charBan[0] = new CharacterBanInfo(playerId, rs.getLong("start_time"), rs.getLong("duration"), rs.getString("reason"));
                }
            }
        });
        return charBan[0];
    }

    @Override
    public boolean supports(String s, int i, int i1) {
        return MySQL5DAOUtils.supports(s, i, i1);
    }
}
