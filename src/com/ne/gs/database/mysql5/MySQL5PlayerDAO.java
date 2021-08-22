/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.mysql5;

import com.google.common.collect.Maps;
import com.ne.commons.database.DB;
import com.ne.commons.database.DatabaseFactory;
import com.ne.commons.database.IUStH;
import com.ne.commons.database.ParamReadStH;
import com.ne.commons.utils.GenericValidator;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.PlayerInitialData;
import com.ne.gs.dataholders.PlayerInitialData.LocationData;
import com.ne.gs.model.Gender;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.gameobjects.player.Mailbox;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.world.MapRegion;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.WorldPosition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * @author SoulKeeper, Saelya
 * @author cura
 */
public class MySQL5PlayerDAO extends PlayerDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerDAO.class);

	/**
	 * {@inheritDoc}
	 */
	//@author Jenelli
	@Override
	public Integer getPlayerObjectIdByName(String name) {
		PreparedStatement s = DB.prepareStatement("SELECT id FROM players WHERE ? = players.name");
		try {
			s.setString(1, name);
			ResultSet rs = s.executeQuery();
			if (rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			log.error("Can't find player with name " + name + ".", e);
			return null;
		} finally {
			DB.close(s);
		}
		return null;
	}

	@Override
	public boolean isNameUsed(String name) {
		PreparedStatement s = DB.prepareStatement("SELECT count(id) as cnt FROM players WHERE ? = players.name");
		try {
			s.setString(1, name);
			ResultSet rs = s.executeQuery();
			rs.next();
			return rs.getInt("cnt") > 0;
		} catch (SQLException e) {
			log.error("Can't check if name " + name + ", is used, returning possitive result", e);
			return true;
		} finally {
			DB.close(s);
		}
	}

	@Override
	public Map<Integer, String> getPlayerNames(Collection<Integer> playerObjectIds) {

		if (GenericValidator.isBlankOrNull(playerObjectIds)) {
			return Collections.emptyMap();
		}

		Map<Integer, String> result = Maps.newHashMap();

		String sql = "SELECT id, `name` FROM players WHERE id IN(%s)";
		sql = String.format(sql, StringUtils.join(playerObjectIds, ", "));
		PreparedStatement s = DB.prepareStatement(sql);
		try {
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				result.put(id, name);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to load player names", e);
		} finally {
			DB.close(s);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storePlayer(Player player) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con
					.prepareStatement("UPDATE players SET name=?, exp=?, recoverexp=?, x=?, y=?, z=?, heading=?, world_id=?, gender=?, race=?, player_class=?, last_online=?, quest_expands=?, " +
							"npc_expands=?, warehouse_size=?, note=?, title_id=?, soul_sickness=?, mailbox_letters=?, reposte_energy=?, mentor_flag_time=?, " +
							"world_owner=? WHERE id=?");
            if (log.isDebugEnabled()) {
			    log.debug("[DAO: MySQL5PlayerDAO] storing player " + player.getObjectId() + " " + player.getName());
            }
			PlayerCommonData pcd = player.getCommonData();
			stmt.setString(1, player.getName());
			stmt.setLong(2, pcd.getExp());
			stmt.setLong(3, pcd.getExpRecoverable());
			stmt.setFloat(4, player.getX());
			stmt.setFloat(5, player.getY());
			stmt.setFloat(6, player.getZ());
			stmt.setInt(7, player.getHeading());
			stmt.setInt(8, player.getWorldId());
			stmt.setString(9, player.getGender().toString());
			stmt.setString(10, player.getRace().toString());
			stmt.setString(11, pcd.getPlayerClass().toString());
			stmt.setTimestamp(12, pcd.getLastOnline());
			stmt.setInt(13, player.getQuestExpands());
			stmt.setInt(14, player.getNpcExpands());
			stmt.setInt(15, player.getWarehouseSize());
			stmt.setString(16, pcd.getNote());
			stmt.setInt(17, pcd.getTitleId());
			stmt.setInt(18, pcd.getDeathCount());
			Mailbox mailBox = player.getMailbox();
			int mails = mailBox != null ? mailBox.size() : pcd.getMailboxLetters();
			stmt.setInt(19, mails);
			stmt.setLong(20, pcd.getCurrentReposteEnergy());
			stmt.setInt(21, pcd.getMentorFlagTime());
			WorldMapInstance channel = player.getPosition().getWorldMapInstance();
			if (channel == null) // may be null if player received Critical Error
			{
				stmt.setInt(22, 0);
			} else {
				stmt.setInt(22, channel.getOwnerId());
			}
			stmt.setInt(23, player.getObjectId());
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			log.error("Error saving player: " + player.getObjectId() + " " + player.getName(), e);
		} finally {
			DatabaseFactory.close(con);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveNewPlayer(PlayerCommonData pcd, int accountId, String accountName) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement preparedStatement = con
					.prepareStatement("INSERT INTO players(id, `name`, account_id, account_name, x, y, z, heading, world_id, gender, race, player_class , quest_expands, npc_expands, warehouse_size, " +
							"online) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)");

			log.debug("[DAO: MySQL5PlayerDAO] saving new player: " + pcd.getPlayerObjId() + " " + pcd.getName());

			preparedStatement.setInt(1, pcd.getPlayerObjId());
			preparedStatement.setString(2, pcd.getName());
			preparedStatement.setInt(3, accountId);
			preparedStatement.setString(4, accountName);
			preparedStatement.setFloat(5, pcd.getPosition().getX());
			preparedStatement.setFloat(6, pcd.getPosition().getY());
			preparedStatement.setFloat(7, pcd.getPosition().getZ());
			preparedStatement.setInt(8, pcd.getPosition().getH());
			preparedStatement.setInt(9, pcd.getPosition().getMapId());
			preparedStatement.setString(10, pcd.getGender().toString());
			preparedStatement.setString(11, pcd.getRace().toString());
			preparedStatement.setString(12, pcd.getPlayerClass().toString());
			preparedStatement.setInt(13, pcd.getQuestExpands());
			preparedStatement.setInt(14, pcd.getNpcExpands());
			preparedStatement.setInt(15, pcd.getWarehouseSize());
			preparedStatement.execute();
			preparedStatement.close();
		} catch (Exception e) {
			log.error("Error saving new player: " + pcd.getPlayerObjId() + " " + pcd.getName(), e);
			return false;
		} finally {
			DatabaseFactory.close(con);
		}
		return true;
	}

	@Override
	public PlayerCommonData loadPlayerCommonDataByName(String name) {
		Player player = World.getInstance().findPlayer(name);
		if (player != null) {
			return player.getCommonData();
		}

		int playerObjId = 0;

		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT id FROM players WHERE name = ?");
			stmt.setString(1, name);
			ResultSet rset = stmt.executeQuery();
			if (rset.next()) {
				playerObjId = rset.getInt("id");
			}
			rset.close();
			stmt.close();
		} catch (Exception e) {
			log.error("Could not restore playerId data for player name: " + name + " from GDB: " + e.getMessage(), e);
		} finally {
			DatabaseFactory.close(con);
		}

		if (playerObjId == 0) {
			return null;
		}
		return loadPlayerCommonData(playerObjId);
	}

	@Override
	public PlayerCommonData loadPlayerCommonData(int playerObjId) {

		PlayerCommonData cd = new PlayerCommonData(playerObjId);
		boolean success = false;
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT * FROM players WHERE id = ?");
			stmt.setInt(1, playerObjId);
			ResultSet resultSet = stmt.executeQuery();
			log.debug("[DAO: MySQL5PlayerDAO] loading from db " + playerObjId);

			if (resultSet.next()) {
				success = true;
				cd.setName(resultSet.getString("name"));
				// set player class before exp
				cd.setPlayerClass(PlayerClass.valueOf(resultSet.getString("player_class")));
				cd.setExp(resultSet.getLong("exp"));
				cd.setRecoverableExp(resultSet.getLong("recoverexp"));
				cd.setRace(Race.valueOf(resultSet.getString("race")));
				cd.setGender(Gender.valueOf(resultSet.getString("gender")));
				cd.setLastOnline(resultSet.getTimestamp("last_online"));
				cd.setNote(resultSet.getString("note"));
				cd.setQuestExpands(resultSet.getInt("quest_expands"));
				cd.setNpcExpands(resultSet.getInt("npc_expands"));
				cd.setTitleId(resultSet.getInt("title_id"));
				cd.setWarehouseSize(resultSet.getInt("warehouse_size"));
				cd.setOnline(resultSet.getBoolean("online"));
				cd.setMailboxLetters(resultSet.getInt("mailbox_letters"));
				cd.setDeathCount(resultSet.getInt("soul_sickness"));
				cd.setCurrentReposteEnergy(resultSet.getLong("reposte_energy"));

				float x = resultSet.getFloat("x");
				float y = resultSet.getFloat("y");
				float z = resultSet.getFloat("z");
				byte heading = resultSet.getByte("heading");
				int worldId = resultSet.getInt("world_id");
				PlayerInitialData playerInitialData = DataManager.PLAYER_INITIAL_DATA;
				MapRegion mr = World.getInstance().getWorldMap(worldId).getWorldMapInstance().getRegion(x, y, z);
				if (mr == null && playerInitialData != null) {
					// unstuck unlucky characters :)
					LocationData ld = playerInitialData.getSpawnLocation(cd.getRace());
					x = ld.getX();
					y = ld.getY();
					z = ld.getZ();
					heading = ld.getHeading();
					worldId = ld.getMapId();
				}

				WorldPosition position = World.getInstance().createPosition(worldId, x, y, z, heading, 0);
				cd.setPosition(position);
				cd.setWorldOwnerId(resultSet.getInt("world_owner"));
				cd.setMentorFlagTime(resultSet.getInt("mentor_flag_time"));
				//cd.setLastTransferTime(resultSet.getLong("last_transfer_time"));
			} else {
				log.info("Missing PlayerCommonData from db " + playerObjId);
			}
			resultSet.close();
			stmt.close();
		} catch (Exception e) {
			log.error("Could not restore PlayerCommonData data for player: " + playerObjId + " from GDB: " + e.getMessage(), e);
		} finally {
			DatabaseFactory.close(con);
		}

		return success ? cd : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deletePlayer(int playerId) {
		PreparedStatement statement = DB.prepareStatement("DELETE FROM players WHERE id = ?");
		try {
			statement.setInt(1, playerId);
		} catch (SQLException e) {
			log.error("Some crap, can't set int parameter to PreparedStatement", e);
		}
		DB.executeUpdateAndClose(statement);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getPlayerOidsOnAccount(final int accountId) {
		final List<Integer> result = new ArrayList<>();
		boolean success = DB.select("SELECT id FROM players WHERE account_id = ?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					result.add(resultSet.getInt("id"));
				}
			}

			@Override
			public void setParams(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setInt(1, accountId);
			}
		});

		return success ? result : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCreationDeletionTime(final PlayerAccountData acData) {
		DB.select("SELECT creation_date, deletion_date FROM players WHERE id = ?", new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, acData.getPlayerCommonData().getPlayerObjId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				rset.next();

				acData.setDeletionDate(rset.getTimestamp("deletion_date"));
				acData.setCreationDate(rset.getTimestamp("creation_date"));
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateDeletionTime(final int objectId, final Timestamp deletionDate) {
		DB.insertUpdate("UPDATE players set deletion_date = ? where id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setTimestamp(1, deletionDate);
				preparedStatement.setInt(2, objectId);
				preparedStatement.execute();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeCreationTime(final int objectId, final Timestamp creationDate) {
		DB.insertUpdate("UPDATE players set creation_date = ? where id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setTimestamp(1, creationDate);
				preparedStatement.setInt(2, objectId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public void storeLastOnlineTime(final int objectId, final Timestamp lastOnline) {
		DB.insertUpdate("UPDATE players set last_online = ? where id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setTimestamp(1, lastOnline);
				preparedStatement.setInt(2, objectId);
				preparedStatement.execute();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getUsedIDs() {
		PreparedStatement statement = DB.prepareStatement("SELECT id FROM players", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		try {
			ResultSet rs = statement.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; i < count; i++) {
				rs.next();
				ids[i] = rs.getInt("id");
			}
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of id's from players table", e);
		} finally {
			DB.close(statement);
		}

		return new int[0];
	}

	/**
	 * {@inheritDoc} - Saelya
	 */
	@Override
	public void onlinePlayer(final Player player, final boolean online) {
		DB.insertUpdate("UPDATE players SET online=? WHERE id=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				log.debug("[DAO: MySQL5PlayerDAO] online status " + player.getObjectId() + " " + player.getName());

				stmt.setBoolean(1, online);
				stmt.setInt(2, player.getObjectId());
				stmt.execute();
			}
		});
	}

	/**
	 * {@inheritDoc} - Nemiroff
	 */
	@Override
	public void setPlayersOffline(final boolean online) {
		DB.insertUpdate("UPDATE players SET online=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setBoolean(1, online);
				stmt.execute();
			}
		});
	}

	@Override
	public String getPlayerNameByObjId(final int playerObjId) {
		final String[] result = new String[1];
		DB.select("SELECT name FROM players WHERE id = ?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.next();
				result[0] = arg0.getString("name");
			}

			@Override
			public void setParams(PreparedStatement arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.setInt(1, playerObjId);
			}
		});
		return result[0];
	}

	@Override
	public int getPlayerIdByName(final String playerName) {
		final int[] result = new int[1];
		DB.select("SELECT id FROM players WHERE name = ?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.next();
				result[0] = arg0.getInt("id");
			}

			@Override
			public void setParams(PreparedStatement arg0) throws SQLException {
				// TODO: Auto-generated method stub
				arg0.setString(1, playerName);
			}
		});
		return result[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getAccountIdByName(String name) {
		Connection con = null;
		int accountId = 0;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement s = con.prepareStatement("SELECT `account_id` FROM `players` WHERE `name` = ?");
			s.setString(1, name);
			ResultSet rs = s.executeQuery();
			rs.next();
			accountId = rs.getInt("account_id");
			rs.close();
			s.close();
		} catch (Exception e) {
			return 0;
		} finally {
			DatabaseFactory.close(con);
		}
		return accountId;
	}

	/**
	 * @author xTz
	 */
	@Override
	public void storePlayerName(PlayerCommonData recipientCommonData) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE players SET name=? WHERE id=?");

			log.debug("[DAO: MySQL5PlayerDAO] storing playerName " + recipientCommonData.getPlayerObjId() + " " + recipientCommonData.getName());

			stmt.setString(1, recipientCommonData.getName());
			stmt.setInt(2, recipientCommonData.getPlayerObjId());
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			log.error("Error saving playerName: " + recipientCommonData.getPlayerObjId() + " " + recipientCommonData.getName(), e);
		} finally {
			DatabaseFactory.close(con);
		}
	}

	@Override
	public void setPlayerPosition(int objId, WorldPosition position) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE players SET x=?, y=?, z=?, heading=?, world_id=? WHERE id=?");

			log.debug("[DAO: MySQL5PlayerDAO] storing playerPosition " + objId);

			stmt.setFloat(1, position.getX());
			stmt.setFloat(2, position.getY());
			stmt.setFloat(3, position.getZ());
			stmt.setInt(4, position.getH());
			stmt.setInt(5, position.getMapId());
			stmt.setInt(6, objId);
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			log.error("Error setting player position : " + objId, e);
		} finally {
			DatabaseFactory.close(con);
		}
	}

	@Override
	public int getCharacterCountOnAccount(int accountId) {
		Connection con = null;
		int cnt = 0;

		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con
					.prepareStatement("SELECT COUNT(*) AS cnt FROM `players` WHERE `account_id` = ? AND (players.deletion_date IS NULL || players.deletion_date > CURRENT_TIMESTAMP)");
			stmt.setInt(1, accountId);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			cnt = rs.getInt("cnt");
			rs.close();
			stmt.close();
		} catch (Exception e) {
			return 0;
		} finally {
			DatabaseFactory.close(con);
		}

		return cnt;
	}

	@Override
	public int getCharacterCountForRace(Race race) {
		Connection con = null;
		int count = 0;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con
					.prepareStatement("SELECT COUNT(DISTINCT(`account_name`)) AS `count` FROM `players` WHERE `race` = ? AND `exp` >= ?");
			stmt.setString(1, race.name());
			stmt.setLong(2, DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(GSConfig.RATIO_MIN_REQUIRED_LEVEL));
			ResultSet rs = stmt.executeQuery();
			rs.next();
			count = rs.getInt("count");
			rs.close();
			stmt.close();
		} catch (Exception e) {
			return 0;
		} finally {
			DatabaseFactory.close(con);
		}

		return count;
	}

	@Override
	public int getOnlinePlayerCount() {
		Connection con = null;
		int count = 0;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT COUNT(*) AS `count` FROM `players` WHERE `online` = ?");
			stmt.setBoolean(1, true);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			count = rs.getInt("count");
			rs.close();
			stmt.close();
		} catch (Exception e) {
			return 0;
		} finally {
			DatabaseFactory.close(con);
		}

		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Integer> getPlayersToDelete(final int daysOfInactivity, int limitation) {
		String SELECT_QUERY = "SELECT id FROM players WHERE UNIX_TIMESTAMP(CURDATE())-UNIX_TIMESTAMP(last_online) > ? * 24 * 60 * 60";

		// limitation
		if (limitation > 0) {
			SELECT_QUERY += " LIMIT " + limitation;
		}

		final List<Integer> playersToDelete = new ArrayList<>();

		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, daysOfInactivity);
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int id = rset.getInt("id");
					playersToDelete.add(id);
				}
			}
		});
		return playersToDelete;
	}

	/**
	 * {@inheritDoc} - KID
	 */
	@Override
	public void setPlayerLastTransferTime(final int playerId, final long time) {
		DB.insertUpdate("UPDATE players SET last_transfer_time=? WHERE id=?", new IUStH() {
			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setLong(1, time);
				stmt.setInt(2, playerId);
				stmt.execute();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
