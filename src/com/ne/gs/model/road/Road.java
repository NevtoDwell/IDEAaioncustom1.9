/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.road;

import com.ne.gs.controllers.RoadController;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.road.RoadTemplate;
import com.ne.gs.model.utils3d.Plane3D;
import com.ne.gs.model.utils3d.Point3D;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.SphereKnownList;

/**
 * @author SheppeR
 */
public class Road extends VisibleObject {

    private RoadTemplate template = null;
    private String name = null;
    private Plane3D plane = null;
    private Point3D center = null;
    private Point3D p1 = null;
    private Point3D p2 = null;

    public Road(RoadTemplate template) {
        super(IDFactory.getInstance().nextId(), new RoadController(), null, null, World.getInstance().createPosition(template.getMap(),
            template.getCenter().getX(), template.getCenter().getY(), template.getCenter().getZ(), (byte) 0, 0));

        ((RoadController) getController()).setOwner(this);
        this.template = template;
        name = template.getName() == null ? "ROAD" : template.getName();
        center = new Point3D(template.getCenter().getX(), template.getCenter().getY(), template.getCenter().getZ());
        p1 = new Point3D(template.getP1().getX(), template.getP1().getY(), template.getP1().getZ());
        p2 = new Point3D(template.getP2().getX(), template.getP2().getY(), template.getP2().getZ());
        plane = new Plane3D(center, p1, p2);
        setKnownlist(new SphereKnownList(this, template.getRadius() * 2));
    }

    public Plane3D getPlane() {
        return plane;
    }

    public RoadTemplate getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return name;
    }

    public void spawn() {
        World w = World.getInstance();
        w.storeObject(this);
        w.spawn(this);
    }
}
