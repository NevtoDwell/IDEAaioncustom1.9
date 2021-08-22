/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.mysql5;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.database.DatabaseFactory;
import com.ne.commons.utils.GenericValidator;
import com.ne.gs.database.dao.ItemStoneListDAO;
import com.ne.gs.database.dao.MySQL5DAOUtils;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.items.GodStone;
import com.ne.gs.model.items.ItemStone;
import com.ne.gs.model.items.ItemStone.ItemStoneType;
import com.ne.gs.model.items.ManaStone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author ATracer
 */
public class MySQL5ItemStoneListDAO extends ItemStoneListDAO {

    private static final Logger log = LoggerFactory.getLogger(MySQL5ItemStoneListDAO.class);
    public static final String INSERT_QUERY = "INSERT INTO `item_stones` (`item_unique_id`, `item_id`, `slot`, `category`) VALUES (?,?,?, ?)";
    public static final String UPDATE_QUERY = "UPDATE `item_stones` SET `item_id`=?, `slot`=? where `item_unique_id`=? AND `category`=?";
    public static final String DELETE_QUERY = "DELETE FROM `item_stones` WHERE `item_unique_id`=? AND slot=? AND category=?";
    public static final String SELECT_QUERY = "SELECT `item_id`, `slot`, `category` FROM `item_stones` WHERE `item_unique_id`=?";

    private static final Predicate<ItemStone> itemStoneAddPredicate = new Predicate<ItemStone>() {
        @Override
        public boolean apply(@Nullable ItemStone itemStone) {
            return itemStone != null && PersistentState.NEW == itemStone.getPersistentState();
        }
    };

    private static final Predicate<ItemStone> itemStoneDeletedPredicate = new Predicate<ItemStone>() {
        @Override
        public boolean apply(@Nullable ItemStone itemStone) {
            return itemStone != null && PersistentState.DELETED == itemStone.getPersistentState();
        }
    };

    private static final Predicate<ItemStone> itemStoneUpdatePredicate = new Predicate<ItemStone>() {
        @Override
        public boolean apply(@Nullable ItemStone itemStone) {
            return itemStone != null && PersistentState.UPDATE_REQUIRED == itemStone.getPersistentState();
        }
    };

    @Override
    public void load(Collection<Item> items) {
        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
            for (Item item : items) {
                if (item.getItemTemplate().isArmor() || item.getItemTemplate().isWeapon()) {
                    stmt.setInt(1, item.getObjectId());
                    ResultSet rset = stmt.executeQuery();
                    while (rset.next()) {
                        int itemId = rset.getInt("item_id");
                        int slot = rset.getInt("slot");
                        int stoneType = rset.getInt("category");
                        if (stoneType == 0) {
                            if (item.getSockets(false) < item.getItemStonesSize()) {
                                log.warn("Manastone slots overloaded. ObjectId: " + item.getObjectId());
                                continue;
                            }
                            item.getItemStones().add(new ManaStone(item.getObjectId(), itemId, slot, PersistentState.UPDATED));
                        } else if (stoneType == 1) {
                            item.setGoodStone(new GodStone(item.getObjectId(), itemId, PersistentState.UPDATED));
                        } else {
                            if (item.getSockets(true) < item.getFusionStonesSize()) {
                                log.warn("Manastone slots overloaded. ObjectId: " + item.getObjectId());
                                continue;
                            }
                            item.getFusionStones().add(new ManaStone(item.getObjectId(), itemId, slot, PersistentState.UPDATED));
                        }
                    }
                    rset.close();
                }
            }
            stmt.close();
        } catch (Exception e) {
            log.error("Could not restore ItemStoneList data from GDB: " + e.getMessage(), e);
        } finally {
            DatabaseFactory.close(con);
        }
    }

    @Override
    public void save(List<Item> items) {
        if (GenericValidator.isBlankOrNull(items)) {
            return;
        }

        Set<ManaStone> manaStones = Sets.newHashSet();
        Set<ManaStone> fusionStones = Sets.newHashSet();
        Set<GodStone> godStones = Sets.newHashSet();

        for (Item item : items) {
            if (item.hasManaStones()) {
                manaStones.addAll(item.getItemStones());
            }

            if (item.hasFusionStones()) {
                fusionStones.addAll(item.getFusionStones());
            }

            GodStone godStone = item.getGodStone();
            if (godStone != null) {
                godStones.add(godStone);
            }
        }

        store(manaStones, ItemStoneType.MANASTONE);
        store(fusionStones, ItemStoneType.FUSIONSTONE);
        store(godStones, ItemStoneType.GODSTONE);
    }

    @Override
    public void storeManaStones(Set<ManaStone> manaStones) {
        store(manaStones, ItemStoneType.MANASTONE);
    }

    @Override
    public void storeFusionStone(Set<ManaStone> manaStones) {
        store(manaStones, ItemStoneType.FUSIONSTONE);
    }

    private void store(Set<? extends ItemStone> stones, ItemStoneType ist) {
        if (GenericValidator.isBlankOrNull(stones)) {
            return;
        }

        Set<? extends ItemStone> stonesToAdd = Sets.filter(stones, itemStoneAddPredicate);
        Set<? extends ItemStone> stonesToDelete = Sets.filter(stones, itemStoneDeletedPredicate);
        Set<? extends ItemStone> stonesToUpdate = Sets.filter(stones, itemStoneUpdatePredicate);

        Connection con = null;
        try {
            con = DatabaseFactory.getConnection();
            con.setAutoCommit(false);

            deleteItemStones(con, stonesToDelete, ist);
            addItemStones(con, stonesToAdd, ist);
            updateItemStones(con, stonesToUpdate, ist);

        } catch (SQLException e) {
            log.error("Can't save stones", e);
        } finally {
            DatabaseFactory.close(con);
        }

        for (ItemStone is : stones) {
            is.setPersistentState(PersistentState.UPDATED);
        }
    }

    private void addItemStones(Connection con, Collection<? extends ItemStone> itemStones, ItemStoneType ist) {

        if (GenericValidator.isBlankOrNull(itemStones)) {
            return;
        }

        PreparedStatement st = null;
        try {
            st = con.prepareStatement(INSERT_QUERY);

            for (ItemStone is : itemStones) {
                st.setInt(1, is.getItemObjId());
                st.setInt(2, is.getItemId());
                st.setInt(3, is.getSlot());
                st.setInt(4, ist.ordinal());
                st.addBatch();
            }

            st.executeBatch();
            con.commit();
        } catch (SQLException e) {
            log.error("Error occured while saving item stones", e);
        } finally {
            DatabaseFactory.close(st);
        }
    }

    private void updateItemStones(Connection con, Collection<? extends ItemStone> itemStones, ItemStoneType ist) {
        if (GenericValidator.isBlankOrNull(itemStones)) {
            return;
        }

        PreparedStatement st = null;
        try {
            st = con.prepareStatement(UPDATE_QUERY);

            for (ItemStone is : itemStones) {
                st.setInt(1, is.getItemId());
                st.setInt(2, is.getSlot());
                st.setInt(3, is.getItemObjId());
                st.setInt(4, ist.ordinal());
                st.addBatch();
            }

            st.executeBatch();
            con.commit();
        } catch (SQLException e) {
            log.error("Error occured while saving item stones", e);
        } finally {
            DatabaseFactory.close(st);
        }
    }

    private void deleteItemStones(Connection con, Collection<? extends ItemStone> itemStones, ItemStoneType ist) {
        if (GenericValidator.isBlankOrNull(itemStones)) {
            return;
        }

        PreparedStatement st = null;
        try {
            st = con.prepareStatement(DELETE_QUERY);

            // TODO: Shouldn't we update stone slot?
            for (ItemStone is : itemStones) {
                st.setInt(1, is.getItemObjId());
                st.setInt(2, is.getSlot());
                st.setInt(3, ist.ordinal());
                st.execute();
                st.addBatch();
            }

            st.executeBatch();
            con.commit();
        } catch (SQLException e) {
            log.error("Error occured while saving item stones", e);
        } finally {
            DatabaseFactory.close(st);
        }
    }

    @Override
    public boolean supports(String s, int i, int i1) {
        return MySQL5DAOUtils.supports(s, i, i1);
    }
}
