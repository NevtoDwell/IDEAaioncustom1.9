/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.shield;

import com.ne.gs.controllers.ShieldController;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.shield.ShieldTemplate;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.knownlist.SphereKnownList;

/**
 * @author Wakizashi
 */
public class Shield extends VisibleObject {

    private ShieldTemplate template = null;
    private String name = null;
    private int id = 0;

    public Shield(ShieldTemplate template) {
        super(IDFactory.getInstance().nextId(), new ShieldController(), null, null, null);

        ((ShieldController) getController()).setOwner(this);
        this.template = template;
        this.name = (template.getName() == null) ? "SHIELD" : template.getName();
        this.id = template.getId();
        setKnownlist(new SphereKnownList(this, template.getRadius() * 2));
    }

    public ShieldTemplate getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void spawn() {
        World w = World.getInstance();
        WorldPosition position = w.createPosition(template.getMap(), template.getCenter().getX(), template.getCenter().getY(), template
                .getCenter().getZ(), (byte) 0, 0);
        this.setPosition(position);
        w.storeObject(this);
        w.spawn(this);
    }
}
