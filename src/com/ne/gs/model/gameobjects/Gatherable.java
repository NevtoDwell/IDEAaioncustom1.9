/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import com.ne.gs.controllers.GatherableController;
import com.ne.gs.model.templates.VisibleObjectTemplate;
import com.ne.gs.model.templates.gather.GatherableTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.world.WorldPosition;

/**
 * @author ATracer
 */
public class Gatherable extends VisibleObject {

    public Gatherable(SpawnTemplate spawnTemplate, VisibleObjectTemplate objectTemplate, int objId, GatherableController controller) {
        super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition());
        controller.setOwner(this);
    }

    @Override
    public String getName() {
        return objectTemplate.getName();
    }

    @Override
    public GatherableTemplate getObjectTemplate() {
        return (GatherableTemplate) objectTemplate;
    }

    @Override
    public GatherableController getController() {
        return (GatherableController) super.getController();
    }
}
