/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import com.ne.gs.controllers.VisibleObjectController;
import com.ne.gs.model.templates.VisibleObjectTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.MapRegion;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.WorldType;
import com.ne.gs.world.knownlist.KnownList;

/**
 * This class is representing visible objects. It's a base class for all in-game objects that can be spawned in the world at some particular position (such as
 * players, npcs).<br>
 * <br>
 * Objects of this class, as can be spawned in game, can be seen by other visible objects. To keep track of which objects are already "known" by this visible
 * object and which are not, VisibleObject is containing {@link KnownList} which is responsible for holding this information.
 *
 * @author -Nemesiss-
 */
public abstract class VisibleObject extends AionObject {

    protected VisibleObjectTemplate objectTemplate;
    public static final float VisibilityDistance = 95.0F;
    public static final float maxZvisibleDistance = 95.0F;

    /**
     * Position of object in the world.
     */
    protected WorldPosition position;

    /**
     * KnownList of this VisibleObject.
     */
    private KnownList knownlist;

    /**
     * Controller of this VisibleObject
     */
    private final VisibleObjectController<? extends VisibleObject> controller;

    /**
     * Visible object's target
     */
    private VisibleObject target;

    /**
     * Spawn template of this visibleObject. .
     */
    private SpawnTemplate spawn;

    private String _description;

    private boolean isNewSpawn = true;

    public VisibleObject(Integer objectId,
                         VisibleObjectController<? extends VisibleObject> controller,
                         SpawnTemplate spawnTemplate,
                         VisibleObjectTemplate objectTemplate,
                         WorldPosition position) {
        super(objectId);
        this.controller = controller;
        this.position = position;
        spawn = spawnTemplate;
        this.objectTemplate = objectTemplate;
    }

    public MapRegion getActiveRegion() {
        return position.getMapRegion();
    }

    public int getInstanceId() {
        return position.getInstanceId();
    }

    /**
     * Return World map id.
     */
    public int getWorldId() {
        return position.getMapId();
    }

    /**
     * Return the WorldType of the current location
     */
    public WorldType getWorldType() {
        return World.getInstance().getWorldMap(getWorldId()).getWorldType();
    }

    /**
     * Return World position x
     */
    public float getX() {
        return position.getX();
    }

    /**
     * Return World position y
     */
    public float getY() {
        return position.getY();
    }

    /**
     * Return World position z
     */
    public float getZ() {
        return position.getZ();
    }

    public void setXYZH(float x, float y, float z, int h) {
        position.setXYZH(x, y, z, h);
    }

    /**
     * Heading of the object. Values from <0,120)
     */
    public int getHeading() {
        return position.getH();
    }

    /**
     * Return object position
     *
     * @return position.
     */
    public WorldPosition getPosition() {
        return position;
    }

    /**
     * Check if object is spawned.
     *
     * @return true if object is spawned.
     */
    public boolean isSpawned() {
        return position.isSpawned();
    }

    /**
     * @return
     */
    public boolean isInWorld() {
        return World.getInstance().findVisibleObject(getObjectId()) != null;
    }

    /**
     * Check if map is instance
     *
     * @return true if object in one of the instance maps
     */
    public boolean isInInstance() {
        return position.isInstanceMap();
    }

    public void clearKnownlist() {
        getKnownList().clear();
    }

    public void updateKnownlist() {
        getKnownList().doUpdate();
    }

    public boolean canSee(Creature creature) {
        return creature != null;
    }

    /**
     * Set KnownList to this VisibleObject
     *
     * @param knownlist
     */
    public void setKnownlist(KnownList knownlist) {
        this.knownlist = knownlist;
    }

    public void setDesc(String description){
        _description = description;
    }

    public String getDesc(){
        return _description;
    }

    /**
     * Returns KnownList of this VisibleObject.
     *
     * @return knownList.
     */
    public KnownList getKnownList() {
        return knownlist;
    }

    /**
     * Return VisibleObjectController of this VisibleObject
     *
     * @return VisibleObjectController.
     */
    public VisibleObjectController<? extends VisibleObject> getController() {
        return controller;
    }

    /**
     * @return VisibleObject
     */
    public final VisibleObject getTarget() {
        return target;
    }

    /**
     * @return distance to target or 0 if no target
     */
    public float getDistanceToTarget() {
        VisibleObject currTarget = target;
        if (currTarget == null) {
            return 0;
        }
        return (float) MathUtil.getDistance(getX(), getY(), getZ(), currTarget.getX(), currTarget.getY(), currTarget.getZ())
            - getObjectTemplate().getBoundRadius().getCollision() - currTarget.getObjectTemplate().getBoundRadius().getCollision();
    }

    /**
     * @param creature
     */
    public void setTarget(VisibleObject creature) {
        target = creature;
    }

    /**
     * @param objectId
     *
     * @return target is object with id equal to objectId
     */
    public boolean isTargeting(int objectId) {
        return target != null && target.getObjectId() == objectId;
    }

    /**
     * Return spawn template of this VisibleObject
     *
     * @return SpawnTemplate
     */
    public SpawnTemplate getSpawn() {
        return spawn;
    }

    public void setSpawn(SpawnTemplate spawn) {
        this.spawn = spawn;
    }

    /**
     * @return the objectTemplate
     */
    public VisibleObjectTemplate getObjectTemplate() {
        return objectTemplate;
    }

    /**
     * @param objectTemplate
     *     the objectTemplate to set
     */
    public void setObjectTemplate(VisibleObjectTemplate objectTemplate) {
        this.objectTemplate = objectTemplate;
    }

    /**
     * @param position
     *     to do remove this after reworked transformSumonAI
     */
    public void setPosition(WorldPosition position) {
        this.position = position;
    }

    public boolean isNewSpawn() {
        return isNewSpawn;
    }

    public void setIsNewSpawn(boolean isNewSpawn) {
        this.isNewSpawn = isNewSpawn;
    }

    public float getVisibilityDistance() {
        return VisibilityDistance;
    }

    public float getMaxZVisibleDistance() {
        return maxZvisibleDistance;
    }

    public boolean affectedByObstacles(){
        return true;
    }

    @Override
    public String toString() {
        if (objectTemplate == null) {
            return super.toString();
        }
        return objectTemplate.getName() + " (" + objectTemplate.getTemplateId() + ")";
    }
}
