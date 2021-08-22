/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.curingzone;

import com.ne.gs.controllers.VisibleObjectController;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.curingzones.CuringTemplate;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.NpcKnownList;

public class CuringObject extends VisibleObject {

    private final CuringTemplate template;
    private final float range;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public CuringObject(CuringTemplate template, int instanceId) {
        super(IDFactory.getInstance().nextId(), new VisibleObjectController() {
        }, null, null, World.getInstance().createPosition(template.getMapId(), template.getX(), template.getY(), template.getZ(), (byte) 0, instanceId));

        this.template = template;
        range = template.getRange();
        setKnownlist(new NpcKnownList(this));
    }

    public CuringTemplate getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return "";
    }

    public float getRange() {
        return range;
    }

    public void spawn() {
        World w = World.getInstance();
        w.storeObject(this);
        w.spawn(this);
    }
}
