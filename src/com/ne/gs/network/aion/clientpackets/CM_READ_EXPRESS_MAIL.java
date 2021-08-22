/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import java.util.concurrent.Future;

import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author antness thx to Guapo for sniffing
 */
public class CM_READ_EXPRESS_MAIL extends AionClientPacket {

    private int action;

    @Override
    protected void readImpl() {
        action = readC();
    }

    @Override
    protected void runImpl() {

        Player player = getConnection().getActivePlayer();
        boolean haveUnreadExpress = player.getMailbox().haveUnreadByType(LetterType.EXPRESS)
            || player.getMailbox().haveUnreadByType(LetterType.BLACKCLOUD);
        switch (action) {
            case 0:
                // window is closed
                if (player.getPostman() != null) {
                    player.getPostman().getController().onDelete();
                    player.setPostman(null);
                }
                break;
            case 1:
                // spawn postman
                if (player.getPostman() != null) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_POSTMAN_ALREADY_SUMMONED);
                } else if (player.isFlying()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_POSTMAN_UNABLE_IN_FLIGHT);
                } else if (player.getController().hasTask(TaskId.EXPRESS_MAIL_USE) && !player.getMailbox().canSkipPostmanCooldown()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_POSTMAN_UNABLE_IN_COOLTIME);
                } else if (haveUnreadExpress || player.getMailbox().canSkipPostmanCooldown()) {
                    VisibleObjectSpawner.spawnPostman(player);
                    player.getMailbox().setSkipPostmanCooldown(false);
                    Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                        }
                    }, 600000);

                    player.getController().addTask(TaskId.EXPRESS_MAIL_USE, task);
                }
                break;
        }
    }
}
