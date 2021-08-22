/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world;

import com.ne.gs.configs.main.WorldConfig;
import com.ne.gs.instance.handlers.GeneralEventHandler;
import com.ne.gs.instance.handlers.InstanceHandler;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.*;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.templates.quest.QuestNpc;
import com.ne.gs.model.templates.world.WorldMapTemplate;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.world.exceptions.DuplicateAionObjectException;
import com.ne.gs.world.knownlist.Visitor;
import com.ne.gs.world.zone.RegionZone;
import com.ne.gs.world.zone.ZoneInstance;
import com.ne.gs.world.zone.ZoneName;
import com.ne.gs.world.zone.ZoneService;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import mw.utils.threading.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Future;

/**
 * World map instance object.
 *
 * @author -Nemesiss-
 */
public abstract class WorldMapInstance {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(WorldMapInstance.class);
    /**
     * Size of region
     */
    public static final int regionSize = WorldConfig.WORLD_REGION_SIZE;
    /**
     * WorldMap witch is parent of this instance.
     */
    private final WorldMap parent;
    /**
     * Map of active regions.
     */
    protected final TIntObjectHashMap<MapRegion> regions = new TIntObjectHashMap<>();

    /**
     * All objects spawned in this world map instance
     */
    private final Map<Integer, VisibleObject> worldMapObjects = new FastMap<Integer, VisibleObject>().shared();

    /**
     * All players spawned in this world map instance
     */
    private final FastMap<Integer, Player> worldMapPlayers = new FastMap<Integer, Player>().shared();

    private final Set<Integer> registeredObjects = Collections.newSetFromMap(new FastMap<Integer, Boolean>().shared());

    private PlayerGroup registeredGroup = null;

    private Future<?> emptyInstanceTask = null;

    /**
     * Id of this instance (channel)
     */
    private final int instanceId;

    private final FastList<Integer> questIds = new FastList<>();

    private InstanceHandler instanceHandler;

    private Map<ZoneName, ZoneInstance> zones = new HashMap<>();

    private Integer soloPlayer;

    private PlayerAlliance registredAlliance;
    
    InstanceType instanceType = InstanceType.NORMAL;

    //private CancellationToken _despawnedToken;

    /**
     * Constructor.
     *
     * @param parent
     */
    public WorldMapInstance(WorldMap parent, int instanceId) {
        this.parent = parent;
        this.instanceId = instanceId;
        zones = ZoneService.getInstance().getZoneInstancesByWorldId(parent.getMapId());
        initMapRegions();

        //_despawnedToken = new CancellationToken();
    }

    /**
     * Post runnable task to thread pool
     * Task will can be cancelled manualy or automatically on instance destruction
     *
     * @param runnable Delegate
     * @param delay    Execution delay
     */
    public Task postTask(Runnable runnable, long delay) {

        //if (!_despawnedToken.isCancelled())
            //return Task.start(runnable, _despawnedToken, delay);

        return null;
    }

    /**
     * Post runnable task to thread pool
     * Task will be canceled automatically on instance destruction
     *
     * @param runnable Delegate
     * @param delay    Execution delay
     * @param period   Execution period
     */
    public Task postTask(Runnable runnable, long delay, long period) {

        //if (!_despawnedToken.isCancelled())
            //return Task.start(runnable, _despawnedToken, delay, period);

        return null;
    }

    /**
     * Return World map id.
     *
     * @return world map id
     */
    public Integer getMapId() {
        return getParent().getMapId();
    }

    /**
     * Returns WorldMap witch is parent of this instance
     *
     * @return parent
     */
    public WorldMap getParent() {
        return parent;
    }

    public WorldMapTemplate getTemplate() {
        return parent.getTemplate();
    }

    /**
     * Returns MapRegion that contains coordinates of VisibleObject. If the region doesn't exist, it's created.
     *
     * @param object
     * @return a MapRegion
     */
    MapRegion getRegion(VisibleObject object) {
        return getRegion(object.getX(), object.getY(), object.getZ());
    }

    /**
     * Returns MapRegion that contains given x,y coordinates. If the region doesn't exist, it's created.
     *
     * @param x
     * @param y
     * @return a MapRegion
     */
    public abstract MapRegion getRegion(float x, float y, float z);

    /**
     * Create new MapRegion and add link to neighbours.
     *
     * @param regionId
     * @return newly created map region
     */
    protected abstract MapRegion createMapRegion(int regionId);

    protected abstract void initMapRegions();

    public abstract boolean isPersonal();

    public abstract int getOwnerId();

    /**
     * Returs {@link World} instance to which belongs this WorldMapInstance
     *
     * @return World
     */
    public World getWorld() {
        return getParent().getWorld();
    }

    /**
     * @param object
     */
    public void addObject(VisibleObject object) {
        if (worldMapObjects.put(object.getObjectId(), object) != null) {
            throw new DuplicateAionObjectException("Object with templateId " + String.valueOf(object.getObjectTemplate().getTemplateId())
                    + " already spawned in the instance " + String.valueOf(getMapId()) + " " + String.valueOf(getInstanceId()));
        }
        if (object instanceof Npc) {
            QuestNpc data = QuestEngine.getInstance().getQuestNpc(((Npc) object).getNpcId());
            if (data != null) {
                for (int id : data.getOnQuestStart()) {
                    if (!questIds.contains(id)) {
                        questIds.add(id);
                    }
                }
            }
        }
        if (object instanceof Player) {
            if (getParent().isPossibleFly()) {
                ((Player) object).setInsideZoneType(ZoneType.FLY);
            }
            worldMapPlayers.put(object.getObjectId(), (Player) object);
        }
    }

    /**
     * @param object
     */
    public void removeObject(AionObject object) {
        worldMapObjects.remove(object.getObjectId());
        if (object instanceof Player) {
            if (getParent().isPossibleFly()) {
                ((Player) object).unsetInsideZoneType(ZoneType.FLY);
            }
            worldMapPlayers.remove(object.getObjectId());
        }
    }

    /**
     * @param npcId
     * @return npc
     */
    public Npc getNpc(int npcId) {
        for (Iterator<VisibleObject> iter = objectIterator(); iter.hasNext(); ) {
            VisibleObject obj = iter.next();
            if (obj instanceof Npc) {
                Npc npc = (Npc) obj;
                if (npc.getNpcId() == npcId) {
                    return npc;
                }
            }
        }
        return null;
    }

    public List<Player> getPlayersInside() {
        List<Player> playersInside = new ArrayList<>();
        Iterator<Player> players = playerIterator();
        while (players.hasNext()) {
            playersInside.add(players.next());
        }
        return playersInside;
    }

    /**
     * @param npcId
     * @return List<npc>
     */
    public List<Npc> getNpcs(int npcId) {
        List<Npc> npcs = new ArrayList<>();
        for (Iterator<VisibleObject> iter = objectIterator(); iter.hasNext(); ) {
            VisibleObject obj = iter.next();
            if (obj instanceof Npc) {
                Npc npc = (Npc) obj;
                if (npc.getNpcId() == npcId) {
                    npcs.add(npc);
                }
            }
        }
        return npcs;
    }

    /**
     * @return List<npc>
     */
    public List<Npc> getNpcs() {
        List<Npc> npcs = new ArrayList<>();
        for (Iterator<VisibleObject> iter = objectIterator(); iter.hasNext(); ) {
            VisibleObject obj = iter.next();
            if (obj instanceof Npc) {
                npcs.add((Npc) obj);
            }
        }
        return npcs;
    }

    public List<Gatherable> getGatherables() {
        List<Gatherable> gatherables = new ArrayList<>();
        for (Iterator<VisibleObject> iter = objectIterator(); iter.hasNext(); ) {
            VisibleObject obj = iter.next();
            if (obj instanceof Gatherable) {
                gatherables.add((Gatherable) obj);
            }
        }
        return gatherables;
    }

    public List<Gatherable> getGatherables(int gatherableId) {
        List<Gatherable> gatherables = new ArrayList<>();
        for (Iterator<VisibleObject> iter = objectIterator(); iter.hasNext(); ) {
            VisibleObject obj = iter.next();
            if (obj instanceof Gatherable) {
                Gatherable npc = (Gatherable) obj;
                if (npc.getObjectTemplate().getTemplateId() == gatherableId) {
                    gatherables.add(npc);
                }
            }
        }
        return gatherables;
    }

    public Map<Integer, StaticDoor> getDoors() {
        Map<Integer, StaticDoor> doors = new HashMap<>();
        for (Iterator<VisibleObject> iter = objectIterator(); iter.hasNext(); ) {
            VisibleObject obj = iter.next();
            if (obj instanceof StaticDoor) {
                StaticDoor door = (StaticDoor) obj;
                doors.put(door.getSpawn().getStaticId(), door);
            }
        }
        return doors;
    }

    public void openDoor(int doorId) {
        setDoorState(doorId, true);
    }

    public void setDoorState(int doorId, boolean open) {
        StaticDoor door = findDoorById(doorId);
        if (door != null)
            door.setOpen(open);
    }

    public StaticDoor findDoorById(int doorId) {
        for (StaticDoor door : getDoors().values()) {
            if (door.getObjectTemplate().getDoorId() == doorId)
                return door;
        }

        return null;
    }

    public List<Trap> getTraps(Creature p) {
        List<Trap> traps = new ArrayList<Trap>();
        for(Iterator<VisibleObject> iter = objectIterator(); iter.hasNext(); ) {
            VisibleObject obj = iter.next();
            if(obj instanceof Trap) {
                Trap t = (Trap)obj;
                if(t.getCreatorId() == p.getObjectId())
                    traps.add(t);
            }
        }
        return traps;
    }

    /**
     * @return the instanceIndex
     */
    public int getInstanceId() {
        return instanceId;
    }

    /**
     * Check player is in instance
     *
     * @param objId
     * @return
     */
    public boolean isInInstance(int objId) {
        return worldMapPlayers.containsKey(objId);
    }

    /**
     * @return
     */
    public Iterator<VisibleObject> objectIterator() {
        return worldMapObjects.values().iterator();
    }

    /**
     * @return
     */
    public Iterator<Player> playerIterator() {
        return worldMapPlayers.values().iterator();
    }

    public void registerGroup(PlayerGroup group) {
        registeredGroup = group;
        register(group.getTeamId());
    }

    public void registerGroup(PlayerAlliance group) {
        registredAlliance = group;
        register(group.getObjectId());
    }

    public PlayerAlliance getRegistredAlliance() {
        return registredAlliance;
    }

    /**
     * @param objectId
     */
    public void register(int objectId) {
        registeredObjects.add(objectId);
    }

    /**
     * @param objectId
     * @return
     */
    public boolean isRegistered(int objectId) {
        return registeredObjects.contains(objectId);
    }

    /**
     * @return the emptyInstanceTask
     */
    public Future<?> getEmptyInstanceTask() {
        return emptyInstanceTask;
    }

    /**
     * @param emptyInstanceTask the emptyInstanceTask to set
     */
    public void setEmptyInstanceTask(Future<?> emptyInstanceTask) {
        this.emptyInstanceTask = emptyInstanceTask;
    }

    /**
     * @return the registeredGroup
     */
    public PlayerGroup getRegisteredGroup() {
        return registeredGroup;
    }

    /**
     * @return
     */
    public int playersCount() {
        return worldMapPlayers.size();
    }

    public int playersCount(final Race race) {

        final int[] cnt = {0};
        doOnAllPlayers(player -> {

            if (player.getRace() == race)
                cnt[0]++;
        });

        return cnt[0];
    }

    public FastList<Integer> getQuestIds() {
        return questIds;
    }

    public final InstanceHandler getInstanceHandler() {
        return instanceHandler;
    }

    public final void setInstanceHandler(InstanceHandler instanceHandler) {
        this.instanceHandler = instanceHandler;
    }

    /**
     * @param visitor
     */
    public void doOnAllObjects(Visitor<VisibleObject> visitor) {
        try {
            for (VisibleObject vo : worldMapObjects.values()) {

                if (vo != null)
                    visitor.visit(vo);
            }

        } catch (Exception ex) {
            log.error("Exception when running visitor on all objects" + ex);
        }
    }

    /**
     * @param visitor
     */
    public void doOnAllPlayers(Visitor<Player> visitor) {
        try {
            for (Player player : worldMapPlayers.values()) {
                if (player != null) {
                    visitor.visit(player);
                }
            }
        } catch (Exception ex) {
            log.error("Exception when running visitor on all players" + ex);
        }
    }

    protected ZoneInstance[] filterZones(int mapId, int regionId, float startX, float startY, float minZ, float maxZ) {
        if (zones.isEmpty()) {
            log.debug("No zones for map " + mapId);
            return new ZoneInstance[0];
        }

        List<ZoneInstance> regionZones = new ArrayList<>();
        RegionZone regionZone = new RegionZone(startX, startY, minZ, maxZ);

        for (ZoneInstance zoneInstance : zones.values()) {
            if (zoneInstance.getAreaTemplate().intersectsRectangle(regionZone)) {
                regionZones.add(zoneInstance);
            }
        }
        return regionZones.toArray(new ZoneInstance[regionZones.size()]);
    }

    /**
     * @param object
     * @param zoneName
     * @return
     */
    public boolean isInsideZone(VisibleObject object, ZoneName zoneName) {
        ZoneInstance zoneTemplate = zones.get(zoneName);
        if (zoneTemplate == null) {
            return false;
        }
        return isInsideZone(object.getPosition(), zoneName);
    }

    /**
     * @param pos
     * @param zoneName
     * @return
     */
    public boolean isInsideZone(WorldPosition pos, ZoneName zoneName) {
        MapRegion mapRegion = this.getRegion(pos.getX(), pos.getY(), pos.getZ());
        return mapRegion.isInsideZone(zoneName, pos.getX(), pos.getY(), pos.getZ());
    }

    public void setSoloPlayerObj(Integer obj) {
        soloPlayer = obj;
    }

    public Integer getSoloPlayerObj() {
        return soloPlayer;
    }

    public void cancelTasks() {
        //try {
            //_despawnedToken.cancel();
        //} catch (InterruptedException e) {
            //throw new Error(e);
        //}
    }
    
    public InstanceType getInstanceType() {
    return instanceType;
  }

  public void setInstanceType(InstanceType type) {
    this.instanceType = type;
  }

  public InstanceHandler getGeneralEventHandler() {
        if (instanceHandler instanceof GeneralEventHandler) {
            return (GeneralEventHandler) instanceHandler;
        } else {
            return instanceHandler;
        }
    }
  
  public boolean isTVTInstance() {
    return instanceType == InstanceType.TVT;
  }

  public boolean isFFAInstance() {
    return instanceType == InstanceType.FFA;
  }

  public boolean isPVPInstance() {
    return instanceType == InstanceType.PVP;
  }

  public boolean isLegionWarInstance() {
    return instanceType == InstanceType.LEGION_WAR;
  }

  public boolean isPeaceInstance() {
    return instanceType == InstanceType.PEACE;
  }

  public boolean isRatingInstance() {
    return instanceType == InstanceType.RATING;
  }

  public boolean isEventInstance() {
    return instanceType == InstanceType.TVT || instanceType == InstanceType.FFA || instanceType == InstanceType.PVP || instanceType == InstanceType.LEGION_WAR
	    || instanceType == InstanceType.RATING || instanceType == InstanceType.PEACE;
  }
}
