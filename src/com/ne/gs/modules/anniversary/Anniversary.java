/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.anniversary;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.DateUtil;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.database.SQL;
import com.ne.commons.database.SqlTable;
import com.ne.commons.database.TableColumn;
import com.ne.commons.database.TableRow;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.gs.configs.modules.AnniversaryConfig;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.network.aion.serverpackets.SM_TOLL_INFO;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.serverpackets.SM_ACCOUNT_TOLL_INFO;

/**
 * @author hex1r0
 */
public class Anniversary extends Actor {

    private final Logger _log = LoggerFactory.getLogger("anniversary");

    private void onPlayerEnterWorld(Player player) {
        if (AnniversaryConfig.RETURNERS_ENABLED) {
            processReturners(player);
        }

        // #2 bonus for registrations in period of time
        //        {
        //            final Date now = DateUtil.now();
        //            final Date start = new Date(0); // TODO config start time of action
        //            final Date end = new Date(0); // TODO config start time of action
        //
        //            if (DateUtil.between(start, now, end)) {
        //                // Все новые игроки, которые присоединятся к нам
        //                // в период, с 10 июля по 10 августа - получат 2 недели повышенных рейтов бесплатно!
        //            }
        //        }

        //        // #3 bonus for old-timers
        //        {
        //            final PlayerAccountData pad = player.getPlayerAccount().getPlayerAccountData(player.getObjectId());
        //            if (pad == null) return;
        //
        //            final DateTime now = DateTime.now();
        //            final DateTime lastOnline = new DateTime(pcd.getLastOnline());
        //
        //            final DateTime creation = new DateTime(pad.getCreationDate());
        //
        //
        //            // Вы играете на наших игровых серерах более года? Тогда вам обязательно положен подарок!
        //            // Получите 300 донат монет (эквивалент 1500 рублей) на свой счет.
        //            // Мы очень благодарны Вам за то, что вы остаетесь с нами все это время!
        //        }

    }

    private void tollQuerryResult(Player player, CampaignType type, long currentToll) {
        long toll = currentToll + AnniversaryConfig.RETURNERS_COINS;
        Account account = player.getPlayerAccount();

        if (!LoginServer.getInstance().sendPacket(new SM_ACCOUNT_TOLL_INFO(toll, player.getAcountName()))) {
            _log.warn("Unable to connect to auth server");
            return;
        }

        player.sendPck(new SM_TOLL_INFO(toll));

        String mac = player.getClientConnection().getMacAddress();
        _log.info(String.format("Campaign %s: Account %s received bonus %d, mac %s",
            type, account.getName(), toll, mac));

        SQL.insertOrUpdate(AnniversaryTable.class,
            TableRow.of(AnniversaryTable.class)
                    .set(AnniversaryTable.account_uid, account.getId())
                    .set(AnniversaryTable.type, type.ordinal())
                    .set(AnniversaryTable.state, BUSY))
           .submit();

        if (mac == null)
            return;

        List<TableRow<AccountDataTable>> rows1 =
            SQL.select(AccountDataTable.class)
               .where(AccountDataTable.last_mac, mac)
               .submit();

        for (TableRow<AccountDataTable> row : rows1) {
            if (row.get(AccountDataTable.name).equals(account.getName())) {
                continue;
            }

            SQL.insertOrUpdate(AnniversaryTable.class,
                TableRow.of(AnniversaryTable.class)
                        .set(AnniversaryTable.account_uid, row.get(AccountDataTable.id))
                        .set(AnniversaryTable.type, type.ordinal())
                        .set(AnniversaryTable.state, BUSY))
               .submit();

            _log.info(String.format("Campaign %s: Account %s will not receive bonus, mac %s",
                type, row.get(AccountDataTable.name), mac));
        }
    }

    private static final Byte BUSY = 1;

    /**
     * Бонусы всем, кто возвращается на родной сервер!
     * Если вы не играли более 2х месяцев и возвращаетесь к нам,
     * вы получаете бонус, в размере 200 донат монет (эквивалент 1000 рублей)!
     */
    @SuppressWarnings("unchecked")
    private void processReturners(final Player player) {
        PlayerCommonData pcd = player.getCommonData();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            final CampaignType type = CampaignType.RETURNERS;
            Date campaignStart = dateFormat.parse(AnniversaryConfig.RETURNERS_START);
            Date campaignEnd = dateFormat.parse(AnniversaryConfig.RETURNERS_END);
            if (!DateUtil.between(campaignStart, DateUtil.now(), campaignEnd)) {
                return;
            }

            DateTime now = DateTime.now();
            DateTime lastOnline = new DateTime(player.getPlayerAccount().getAccountTime().getPrevAuthTime());
            int daysOffline = Days.daysBetween(lastOnline, now).getDays();
            if (!(daysOffline >= AnniversaryConfig.RETURNERS_DAYSOFFLINE)) {
                return;
            }

            Account account = player.getPlayerAccount();
            List<TableRow<AnniversaryTable>> rows =
                SQL.select(AnniversaryTable.class)
                   .where(AnniversaryTable.account_uid, account.getId())
                   .and(AnniversaryTable.type, type.ordinal())
                   .submit();

            for (TableRow<AnniversaryTable> row : rows) {
                if (row.get(AnniversaryTable.state).equals(BUSY)) {
                    return;
                }
            }

            InGameShopEn.getInstance().querryToll(player, new InGameShopEn.TollQuerry() {
                @Override
                public Object onEvent(@NotNull InGameShopEn.TollQuerryResult env) {
                    getInstance().tell(new TollQuerryResult(player, type, env.toll));
                    return null;
                }
            });
        } catch (Exception e) {
            _log.error("", e);
        }
    }

    enum CampaignType {
        RETURNERS
    }

    public static final class PlayerEnterWorld extends Message<Anniversary> {

        private final Player _player;

        public PlayerEnterWorld(Player player) {
            _player = player;
        }

        @Override
        public void run() {
            actor().onPlayerEnterWorld(_player);
        }
    }

    private static final class TollQuerryResult extends Message<Anniversary> {

        private final Player _player;
        private final CampaignType _type;
        private final long _toll;

        private TollQuerryResult(Player player, CampaignType type, long toll) {
            _player = player;
            _type = type;
            _toll = toll;
        }

        @Override
        public void run() {
            actor().tollQuerryResult(_player, _type, _toll);
        }
    }


    @SqlTable(name = "anniversary")
    private static enum AnniversaryTable implements TableColumn {
        account_uid("account_uid", Integer.class),
        type("type", Byte.class),
        state("state", Byte.class);

        private final String _name;
        private final Class<?> _type;

        private AnniversaryTable(String name, Class<?> type) {
            _name = name;
            _type = type;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public Class<?> getType() {
            return _type;
        }
    }

    @SqlTable(name = "account_data")
    private static enum AccountDataTable implements TableColumn {
        id("id", Integer.class),
        name("name", String.class),
        password("password", String.class),
        activated("activated", Byte.class),
        access_level("access_level", Byte.class),
        membership("membership", Byte.class),
        old_membership("old_membership", Byte.class),
        last_server("last_server", Byte.class),
        last_ip("last_ip", String.class),
        last_mac("last_mac", String.class),
        ip_force("ip_force", String.class),
        expire("expire", Timestamp.class),
        toll("toll", Integer.class),
        email("email", String.class),
        question("question", String.class),
        answer("answer", String.class),
        balance("balance", Float.class);

        private final String _name;
        private final Class<?> _type;

        private AccountDataTable(String name, Class<?> type) {
            _name = name;
            _type = type;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public Class<?> getType() {
            return _type;
        }
    }

    public static ActorRef<Anniversary> getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {

        private static final ActorRef<Anniversary> INSTANCE = ActorRef.of(new Anniversary());
    }
}
