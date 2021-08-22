/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.antihack;

import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.controllers.movement.PlayerMoveController;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_FORCED_MOVE;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.world.World;

public final class AntiHackService {

    public static boolean canMove(Player player, float x, float y, float z, float speed, byte type) {
        AionServerPacket forcedMove = new SM_FORCED_MOVE(player, player.getObjectId(), x, y, z);
        AionServerPacket normalMove = new SM_MOVE(player);
        
        //if (player.getAccessLevel() > 0) {
        //    return true;
        //}
        
        if (SecurityConfig.ABNORMAL) {
            if ((!player.canPerformMove()) && (!player.getEffectController().isAbnormalSet(AbnormalState.CANNOT_MOVE)) && ((type & 0x4) != 4)) {
                if (player.abnormalHackCounter > SecurityConfig.ABNORMAL_COUNTER) {
                    punish(player, x, y, type, forcedMove, "Detected illegal Action (Anti-Abnormal Hack)");
                    return false;
                }

                player.abnormalHackCounter += 1;
            } else {
                player.abnormalHackCounter = 0;
            }
        }
        if (SecurityConfig.SPEEDHACK) {
            if (type != 0) {
                if ((type == -64) || (type == -128)) {
                    PlayerMoveController m = player.getMoveController();
                    double vector2D = MathUtil.getDistance(x, y, m.getTargetX2(), m.getTargetY2());

                    if (vector2D != 0) {
                        if ((type == -64) && (vector2D > 5.0) && (vector2D > speed + 0.001)) {
                            player.speedHackCounter += 1;
                        } else if ((vector2D > 37.5) && (vector2D > 1.5 * speed * speed + 0.001)) {
                            player.speedHackCounter += 1;
                        } else if (player.speedHackCounter > 0) {
                            player.speedHackCounter -= 1;
                        }
                        if (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER) {
                            return punish(player, x, y, type, forcedMove, "Detected illegal action (Speed Hack) SHC:" + player.speedHackCounter + " S:" + speed
                                + " V:" + Math.rint(1000 * vector2D) / 1000 + " type:" + type);
                        }

                    }

                } else if (((type & 0x20) == 32) && ((type & 0x4) != 4)) {
                    double vector = MathUtil.getDistance(x, y, player.prevPos.getX(), player.prevPos.getY());
                    long timeDiff = System.currentTimeMillis() - player.prevPosUT;

                    if ((type & 0xFFFFFFC0) == -64) {
                        boolean isMoveToTarget = false;
                        if ((player.getTarget() != null) && (player.getTarget() != player)) {
                            PlayerMoveController m = player.getMoveController();
                            double distDiff = MathUtil
                                .getDistance(player.getTarget().getX(), player.getTarget().getY(), m.getTargetX2(), m.getTargetY2());
                            isMoveToTarget = distDiff < 0.75;
                        }

                        if ((timeDiff > 1000) && (player.speedHackCounter > 0)) {
                            player.speedHackCounter -= 1;
                        }
                        if (vector > timeDiff * (speed + 0.85) * 0.001) {
                            player.speedHackCounter += 1;
                        } else if ((isMoveToTarget) && (player.speedHackCounter > 0)) {
                            player.speedHackCounter -= 1;
                        }
                    } else if (vector > timeDiff * (speed + 0.25D) * 0.001) {
                        player.speedHackCounter += 1;
                    } else if (player.speedHackCounter > 0) {
                        player.speedHackCounter -= 1;
                    }
                    if ((SecurityConfig.PUNISH > 0) && (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER + 5)) {
                        return punish(
                            player,
                            x,
                            y,
                            type,
                            forcedMove,
                            "Detected illegal action (Speed Hack) SHC:" + player.speedHackCounter + " SMS:"
                                + Math.rint(100 * (timeDiff * (speed + 0.25) * 0.001)) / 100 + " TDF:" + timeDiff + " VTD:"
                                + Math.rint(1000 * (timeDiff * (speed + 0.85) * 0.001)) / 1000 + " VS:" + Math.rint(100 * vector) / 100 + " type:"
                                + type);
                    }

                    if (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER) {
                        moveBack(player, x, y, type, forcedMove);
                        return false;
                    }
                }
            } else {
                double vector = MathUtil.getDistance(x, y, player.prevPos.getX(), player.prevPos.getY());
                long timeDiff = System.currentTimeMillis() - player.prevPosUT;

                if ((player.prevMoveType == 0) && (vector > timeDiff * speed * 0.00075)) {
                    player.speedHackCounter += 1;
                }
                if ((SecurityConfig.PUNISH > 0) && (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER + 5)) {
                    return punish(
                        player,
                        x,
                        y,
                        type,
                        forcedMove,
                        "Detected illegal action (Speed Hack) SHC:" + player.speedHackCounter + " TD:" + Math.rint(1000 * timeDiff) / 1000 + " VTD:"
                            + Math.rint(1000 * (timeDiff * speed * 0.00075)) / 1000 + " VS:" + Math.rint(100 * vector) / 100 + " type:" + type);
                }

                if (player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER + 2) {
                    moveBack(player, x, y, type, forcedMove);
                    return false;
                }

            }

            player.prevPos.setXYZH(x, y, z, player.getHeading());
            player.prevPosUT = System.currentTimeMillis();
            if (player.prevMoveType != type) {
                player.prevMoveType = type;
            }
        }
        if (SecurityConfig.TELEPORTATION) {
            double delta = MathUtil.getDistance(x, y, player.getX(), player.getY()) / speed;
            if ((speed > 5d) && (delta > 5) && ((type & 0x4) != 4)) {
                World.getInstance().updatePosition(player, player.getX(), player.getY(), player.getZ(), player.getHeading());
                return punish(player, x, y, type, normalMove, "Detected illegal action (Teleportation) S:" + speed + " D:" + Math.rint(1000 * delta) / 1000
                    + " type:" + type);
            }

        }

        return true;
    }

    protected static boolean punish(Player player, float x, float y, byte type, AionServerPacket pkt, String message) {
        switch (SecurityConfig.PUNISH) {
            case 1:
                AuditLogger.info(player, message);
                moveBack(player, x, y, type, pkt);
                return false;
            case 2:
                AuditLogger.info(player, message);
                moveBack(player, x, y, type, pkt);
                if ((player.speedHackCounter > SecurityConfig.SPEEDHACK_COUNTER * 3) || (player.abnormalHackCounter > SecurityConfig.ABNORMAL_COUNTER * 3)) {
                    player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
                }
                return false;
            case 3:
                AuditLogger.info(player, message);
                player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
                return false;
        }
        AuditLogger.info(player, message);
        return true;
    }

    protected static void moveBack(Player player, float x, float y, byte type, AionServerPacket pkt) {
        PacketSendUtility.broadcastPacketAndReceive(player, pkt);
        player.getMoveController().updateLastMove();
        player.prevPos.setXYZH(x, y, 0f, (byte) 0);
        player.prevPosUT = System.currentTimeMillis();
        if (player.prevMoveType != type) {
            player.prevMoveType = type;
        }
    }
}
