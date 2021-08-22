/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrifts;

import com.ne.gs.controllers.NpcController;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public class RiftController extends NpcController {

    private final Integer _uid;

    public RiftController(Integer uid) {
        _uid = uid;
    }

    @Override
    public void onDialogRequest(Player player) {
        CustomRiftManager.REF.tell(new CustomRiftManager.UseRift(_uid, getOwner(), player));
    }

}
