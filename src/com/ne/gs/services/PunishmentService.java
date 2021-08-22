/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Calendar;
import java.util.concurrent.Future;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.database.dao.PlayerPunishmentsDAO;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_CAPTCHA;
import com.ne.gs.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.chatserver.ChatServer;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapType;

/**
 * @author lord_rex, Cura, nrg, Jenelli
 */
public final class PunishmentService {

    /**
     * This method will handle unbanning a character
     *
     * @param playerId
     */
    public static void unbanChar(int playerId) {
        GDB.get(PlayerPunishmentsDAO.class).unpunishPlayer(playerId, PunishmentType.CHARBAN);
    }

    /**
     * This method will handle banning a character
     *
     * @param playerId
     * @param dayCount
     * @param reason
     */
    public static void banChar(int playerId, int dayCount, String reason) {
        GDB.get(PlayerPunishmentsDAO.class).punishPlayer(playerId, PunishmentType.CHARBAN, calculateDuration(dayCount), reason);

        // if player is online - kick him
        Player player = World.getInstance().findPlayer(playerId);
        if (player != null) {
            player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
        }
    }

    /**
     * Calculates the timestamp when a given number of days is over
     *
     * @param dayCount
     *
     * @return timeStamp
     */
    public static long calculateDuration(int dayCount) {
        if (dayCount == 0) {
            return Integer.MAX_VALUE; // int because client handles this with seconds timestamp in int
        }

        Calendar cal = Calendar.getInstance();
        cal.add(5, dayCount);

        return (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
    }

    /**
     * This method will handle moving or removing a player from prison
     *
     * @param player
     * @param state
     * @param delayInMinutes
     */
    public static void setIsInPrison(Player player, boolean state, long delayInMinutes, String reason) {
        stopPrisonTask(player, false);
        if (state) {
            long prisonTimer = player.getPrisonTimer();
            if (delayInMinutes > 0) {
                prisonTimer = delayInMinutes * 60000L;
                schedulePrisonTask(player, prisonTimer);
                player.sendMsg("Вы будете перемещены в тюрьму на " + delayInMinutes + " минут." +
                    "\nПричина тюрьмы: " + reason +
                    "\nПри выходе персонажа из игры время таймера останавливается. При повторном входе в игру таймер тюрьмы будет активирован вновь.");
            }

            if (GSConfig.ENABLE_CHAT_SERVER) {
                ChatServer.getInstance().sendPlayerLogout(player);
            }

            player.setStartPrison(System.currentTimeMillis());
            TeleportService.teleportToPrison(player);
            GDB.get(PlayerPunishmentsDAO.class).punishPlayer(player, PunishmentType.PRISON, reason);
        } else {
            player.sendMsg("Вы возвращены из тюрьмы.");

            if (GSConfig.ENABLE_CHAT_SERVER) {
                player.sendMsg("Для использование глобального чата сделайте релог!");
            }

            player.setPrisonTimer(0);

            TeleportService.moveToBindLocation(player, true);

            GDB.get(PlayerPunishmentsDAO.class).unpunishPlayer(player.getObjectId(), PunishmentType.PRISON);
        }
    }

    /**
     * This method will handle moving or removing a player offline from prison
     *
     * @param playerObjectId
     * @param state
     * @param delayInMinutes
     *
     * @author Jenelli
     */
    public static void setIsInPrisonOffline(Integer playerObjectId, boolean state, long delayInMinutes, String reason) {
        if (playerObjectId == null) {
            return;
        }
        if (state) {
            long prisonTimer = delayInMinutes * 60000L / 1000;
            GDB.get(PlayerPunishmentsDAO.class).punishPlayer(playerObjectId, PunishmentType.PRISON, prisonTimer, reason);
        } else {
            GDB.get(PlayerPunishmentsDAO.class).unpunishPlayer(playerObjectId, PunishmentType.PRISON);
        }
    }

    /**
     * This method will stop the prison task
     *
     * @param player
     * @param save
     */
    public static void stopPrisonTask(Player player, boolean save) {
        Future<?> prisonTask = player.getController().getTask(TaskId.PRISON);
        if (prisonTask != null) {
            if (save) {
                long delay = player.getPrisonTimer();
                if (delay < 0) {
                    delay = 0;
                }
                player.setPrisonTimer(delay);
            }
            player.getController().cancelTask(TaskId.PRISON);
        }
    }

    /**
     * This method will update the prison status
     *
     * @param player
     */
    public static void updatePrisonStatus(final Player player) {
        if (player.isInPrison()) {
            long prisonTimer = player.getPrisonTimer();
            if (prisonTimer > 0) {
                schedulePrisonTask(player, prisonTimer);
                int timeInPrison = (int) (prisonTimer / 60000);

                if (timeInPrison <= 0) {
                    timeInPrison = 1;
                }

                String msg = "Оставшееся время тюрьмы: " + timeInPrison + " мин." +
                    "\nПричина тюрьмы: " + player.getPrisonReason();
                player.sendMsg(msg);

                player.setStartPrison(System.currentTimeMillis());
            }

            if (player.getWorldId() != WorldMapType.DF_PRISON.getId() && player.getWorldId() != WorldMapType.DE_PRISON.getId()) {
                player.sendMsg("Вы будете перемещены в тюрьму в течении 1ой минуты!");
                ThreadPoolManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        TeleportService.teleportToPrison(player);
                    }
                }, 60000);
            }
        }
    }

    /**
     * This method will schedule a prison task
     *
     * @param player
     * @param prisonTimer
     */
    private static void schedulePrisonTask(final Player player, long prisonTimer) {
        player.setPrisonTimer(prisonTimer);
        player.getController().addTask(TaskId.PRISON, ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                setIsInPrison(player, false, 0, "");
            }
        }, prisonTimer));
    }

    /**
     * This method will handle can or cant gathering
     *
     * @param player
     * @param captchaCount
     * @param state
     * @param delay
     *
     * @author Cura
     */
    public static void setIsNotGatherable(Player player, int captchaCount, boolean state, long delay) {
        stopGatherableTask(player, false);

        if (state) {
            if (captchaCount < 3) {
                player.sendPck(new SM_CAPTCHA(captchaCount + 1, player.getCaptchaImage()));
            } else {
                player.setCaptchaWord(null);
                player.setCaptchaImage(null);
            }

            player.setGatherableTimer(delay);
            player.setStopGatherable(System.currentTimeMillis());
            scheduleGatherableTask(player, delay);
            GDB.get(PlayerPunishmentsDAO.class).punishPlayer(player, PunishmentType.GATHER, "Possible gatherbot");
        } else {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400269));
            player.setCaptchaWord(null);
            player.setCaptchaImage(null);
            player.setGatherableTimer(0);
            player.setStopGatherable(0);
            GDB.get(PlayerPunishmentsDAO.class).unpunishPlayer(player.getObjectId(), PunishmentType.GATHER);
        }
    }

    /**
     * This method will stop the gathering task
     *
     * @param player
     * @param save
     *
     * @author Cura
     */
    public static void stopGatherableTask(Player player, boolean save) {
        Future<?> gatherableTask = player.getController().getTask(TaskId.GATHERABLE);

        if (gatherableTask != null) {
            if (save) {
                long delay = player.getGatherableTimer();
                if (delay < 0) {
                    delay = 0;
                }
                player.setGatherableTimer(delay);
            }
            player.getController().cancelTask(TaskId.GATHERABLE);
        }
    }

    /**
     * This method will update the gathering status
     *
     * @param player
     *
     * @author Cura
     */
    public static void updateGatherableStatus(Player player) {
        if (player.isNotGatherable()) {
            long gatherableTimer = player.getGatherableTimer();

            if (gatherableTimer > 0) {
                scheduleGatherableTask(player, gatherableTimer);
                player.setStopGatherable(System.currentTimeMillis());
            }
        }
    }

    /**
     * This method will schedule a gathering task
     *
     * @param player
     * @param gatherableTimer
     *
     * @author Cura
     */
    private static void scheduleGatherableTask(final Player player, long gatherableTimer) {
        player.setGatherableTimer(gatherableTimer);
        player.getController().addTask(TaskId.GATHERABLE, ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                setIsNotGatherable(player, 0, false, 0);
            }
        }, gatherableTimer));
    }

    /**
     * PunishmentType
     *
     * @author Cura
     */
    public enum PunishmentType {
        PRISON,
        GATHER,
        GAG,
        CHARBAN
    }

    /**
     * Gag/Ungag
     *
     * @param player
     * @param state
     * @param delayInMinutes
     * @param reason
     */
    public static void setIsGag(Player player, boolean state, long delayInMinutes, String reason) {
        stopGagTask(player, false);
        if (state) {
            long gagTimer = player.getGagTimer();
            if (delayInMinutes > 0) {
                gagTimer = delayInMinutes * 60000L;
                scheduleGagTask(player, gagTimer);
                player.sendMsg("Вы получили бан чата на " + delayInMinutes + " минут." +
                    "\nПричина бана чата: " + reason +
                    "\nПри выходе персонажа из игры время таймера останавливается. При повторном входе в игру таймер бана чата будет активирован вновь.");
            }

            if (GSConfig.ENABLE_CHAT_SERVER) {
                ChatServer.getInstance().sendPlayerLogout(player);
            }

            player.setStartGag(System.currentTimeMillis());
            GDB.get(PlayerPunishmentsDAO.class).punishPlayer(player, PunishmentType.GAG, reason);
        } else {
            player.sendMsg("Бан чата снят.");

            if (GSConfig.ENABLE_CHAT_SERVER) {
                player.sendMsg("Для использование глобального чата сделайте релог!");
            }

            player.setGagTimer(0);
            GDB.get(PlayerPunishmentsDAO.class).unpunishPlayer(player.getObjectId(), PunishmentType.GAG);
        }
    }

    /**
     * Gag/Ungag offline
     *
     * @param playerObjectId
     * @param state
     * @param delayInMinutes
     */
    public static void setIsGagOffline(Integer playerObjectId, boolean state, long delayInMinutes, String reason) {
        if (playerObjectId == null) {
            return;
        }
        if (state) {
            long gagTimer = delayInMinutes * 60000L / 1000;
            GDB.get(PlayerPunishmentsDAO.class).punishPlayer(playerObjectId, PunishmentType.GAG, gagTimer, reason);
        } else {
            GDB.get(PlayerPunishmentsDAO.class).unpunishPlayer(playerObjectId, PunishmentType.GAG);
        }
    }

    /**
     * This method will stop the gag task
     *
     * @param player
     * @param save
     */
    public static void stopGagTask(Player player, boolean save) {
        Future<?> gagTask = player.getController().getTask(TaskId.GAG);
        if (gagTask != null) {
            if (save) {
                long delay = player.getGagTimer();
                if (delay < 0) {
                    delay = 0;
                }
                player.setGagTimer(delay);
            }
            player.getController().cancelTask(TaskId.GAG);
        }
    }

    /**
     * This method will update the gag status
     *
     * @param player
     */
    public static void updateGagStatus(Player player) {
        if (player.isGag()) {
            long gagTimer = player.getGagTimer();
            if (gagTimer > 0) {
                scheduleGagTask(player, gagTimer);
                int timeGag = (int) (gagTimer / 60000);
                if (timeGag <= 0) {
                    timeGag = 1;
                }

                String msg = "Оставшееся время бана чата: " + timeGag + " мин." +
                    "\nПричина бана чата: " + player.getGagReason();
                player.sendMsg(msg);
                player.setStartGag(System.currentTimeMillis());
            }
        }
    }

    /**
     * This method will schedule a gag task
     *
     * @param player
     * @param gagTimer
     */
    private static void scheduleGagTask(final Player player, long gagTimer) {
        player.setGagTimer(gagTimer);
        player.getController().addTask(TaskId.GAG, ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                setIsGag(player, false, 0, "");
            }
        }, gagTimer));
    }
}
