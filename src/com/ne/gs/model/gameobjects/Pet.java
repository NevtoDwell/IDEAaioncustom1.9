/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import com.ne.gs.controllers.PetController;
import com.ne.gs.controllers.movement.MoveController;
import com.ne.gs.controllers.movement.PetMoveController;
import com.ne.gs.model.gameobjects.player.PetCommonData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.pet.PetTemplate;
import com.ne.gs.world.WorldPosition;

/**
 * @author ATracer
 */
public class Pet extends VisibleObject {

    private final Player master;
    private final MoveController moveController;
    private final PetTemplate petTemplate;

    /**
     * @param petTemplate
     * @param controller
     * @param commonData
     * @param master
     */
    public Pet(PetTemplate petTemplate, PetController controller, PetCommonData commonData, Player master) {
        super(commonData.getObjectId(), controller, null, commonData, new WorldPosition());
        controller.setOwner(this);
        this.master = master;
        this.petTemplate = petTemplate;
        moveController = new PetMoveController();
    }

    public Player getMaster() {
        return master;
    }

    public int getPetId() {
        return objectTemplate.getTemplateId();
    }

    @Override
    public String getName() {
        return objectTemplate.getName();
    }

    public final PetCommonData getCommonData() {
        return (PetCommonData) objectTemplate;
    }

    public final MoveController getMoveController() {
        return moveController;
    }

    public final PetTemplate getPetTemplate() {
        return petTemplate;
    }

}
