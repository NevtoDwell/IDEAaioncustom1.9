package mw.engines.geo;

import com.ne.gs.controllers.movement.PlayableMoveController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.templates.world.WorldMapTemplate;
import com.ne.gs.world.WorldPosition;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResult;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.math.Ray;
import mw.engines.geo.math.Vector2f;
import mw.engines.geo.math.Vector3f;
import mw.engines.geo.scene.AionMap;
import mw.engines.geo.scene.AionMesh;
import mw.engines.geo.scene.AionModel;
import mw.engines.geo.templates.AionMapTemplate;
import mw.engines.geo.templates.AionMeshTemplate;
import mw.utils.GeomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * @author MetaWind
 */
public class GeoEngine {

    /* Engine logger */
    private static final Logger Log = LoggerFactory.getLogger(GeoEngine.class);

    /* Vertical direction to Z check ray trace */
    private static final Vector3f VERTICAL_TRACE_DIRECTION = new Vector3f(0,0,-1);

    /* Ray limit for vertical ray trace */
    private static final float VERTICAL_TRACE_DIST = 100;

    /* Geodata relative path */
    private static final String GEODATA_PATH = "./data/aion.mwg";

    /* Loaded geo maps */
    private static final Map<Integer, AionMap> _loadedMaps = new HashMap<>();

    /* Meshes templates */
    private static Map<Short, List<AionMeshTemplate>> _meshesTemplates;

    /* Map templates */
    private static Map<Integer, AionMapTemplate> _mapTemplates;

    /* Model names containeer */
    private static Map<Short, String> _modelNames;

    /* Initialize geo engine */
    public static void Initialize() {

        if (_loadedMaps.size() > 0 || _meshesTemplates != null || _mapTemplates != null)
            throw new Error("GeoEngine already initialized");

        File geoFile = new File(GEODATA_PATH);

        MappedByteBuffer geo;
        try {

            FileChannel roChannel = new RandomAccessFile(geoFile, "r").getChannel();
            int size = (int) roChannel.size();
            geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();

        } catch (Exception e) {
            throw new Error(e);
        }
        geo.order(ByteOrder.LITTLE_ENDIAN);

        if (geo.get() != 'm' || geo.get() != 'w' || geo.get() != 'g')
            throw new Error("Invalid or corrupted geodata file");

        int version = geo.getInt();

        /* Array of ASCII null terminated strings short-string */

        //int stringsSize = geo.getInt();
        //geo.position(geo.position() + stringsSize);

        _modelNames = new HashMap<>();
        int stringsEndOffset = geo.getInt() + geo.position();
        while (geo.position() < stringsEndOffset) {

            short modelId = geo.getShort();

            StringBuilder stringBuilder = new StringBuilder();

            char c;
            while ((c = (char) geo.get()) != '\0')
                stringBuilder.append(c);

            _modelNames.put(modelId, stringBuilder.toString());
        }

        int modelsOffset = geo.getInt();
        int levelsOffset = geo.getInt();

        geo.position(modelsOffset);

        int modelsCounter = geo.getInt();

        _meshesTemplates = new HashMap<>(modelsCounter);
        for (int im = 0; im < modelsCounter; im++) {

            short id = geo.getShort();

            List<AionMeshTemplate> meshes = new ArrayList<>();

            int meshesEndOffset = geo.getInt() + geo.position();

            while (geo.position() < meshesEndOffset) {
                try {
                    AionMeshTemplate template = AionMeshTemplate.Read(geo);
                    meshes.add(template);
                } catch (Exception e) {
                    Log.warn("Model {} mesh not loaded properly", getModelAlias(id));
                }
            }

            _meshesTemplates.put(id, meshes);
        }

        geo.position(levelsOffset);

        int levelsCount = geo.getInt();
        _mapTemplates = new HashMap<>(levelsCount);

        for (int i = 0; i < levelsCount; i++) {
            AionMapTemplate mapTemplate = AionMapTemplate.Read(geo);
            _mapTemplates.put(mapTemplate.Id, mapTemplate);
        }

        Log.info("[GEODATA] Loaded {} models and {} levels", modelsCounter, levelsCount);

        Set<Map.Entry<Integer, AionMapTemplate>> mapEntries = _mapTemplates.entrySet();

        for (Map.Entry<Integer, AionMapTemplate> mapTemplateKvp : mapEntries) {

            AionMapTemplate mapTemplate = mapTemplateKvp.getValue();

            List<AionModel> mapObjects = new ArrayList<>(mapTemplate.Spots.size());
            for (AionMapTemplate.MapSpot spot : mapTemplate.Spots) {

                List<AionMesh> meshes = new ArrayList<>();

                List<AionMeshTemplate> meshesTemplates = _meshesTemplates.get(spot.ModelId);
                if (meshesTemplates == null)
                    throw new Error("[GEODATA] Mesh '" + spot.ModelId + "' template not found!");


                for (AionMeshTemplate meshTemplate : meshesTemplates)
                    meshes.add(new AionMesh(spot.ModelId, meshTemplate, spot.Transform, spot.Position, 1f));


                AionModel aobject = new AionModel(spot.ModelId, spot.Position, meshes);
                mapObjects.add(aobject);
            }

            AionMap map = new AionMap(mapTemplate, mapObjects);
            _loadedMaps.put(mapTemplate.Id, map);
        }

        destroyDirectByteBuffer(geo);

        for (WorldMapTemplate map : DataManager.WORLD_MAPS_DATA) {

            if (!_loadedMaps.containsKey(map.getMapId()))
                _loadedMaps.put(map.getMapId(), new AionMap.DummyMap());
        }
    }


    /**
     * Determines all ray collisions
     *
     * @param mapId          Game map id
     * @param instanceId     Map instance index
     * @param ray            Source ray
     * @param collisionFlags ...deprecated, can be any
     */
    public static boolean collide(int mapId, int instanceId, Ray ray, byte collisionFlags) {

        AionMap map = _loadedMaps.get(mapId);

        CollisionResults results = new CollisionResults(true, instanceId);
        int result = map.collideWith(ray, results);
        return result > 0;
    }


    public static Vector3f setGroundZ(Creature creature, Vector3f pos) {
        if (creature.isInState(CreatureState.FLYING))
            return pos;

        return setGroundZ(creature.getWorldId(), creature.getInstanceId(), pos);
    }

    public static Vector3f setGroundZ(int mapId, int instanceId, Vector3f position) {

        AionMap map = _loadedMaps.get(mapId);

        CollisionResults results = new CollisionResults(false, instanceId, Selector.VERTICAL_CHECK);

        Vector3f pos = new Vector3f(
                position.x,
                position.y,
                position.z + Selector.VERTICAL_CHECK.Infelicity);


        Ray r = new Ray(pos, VERTICAL_TRACE_DIRECTION);
        r.setLimit(VERTICAL_TRACE_DIST);

        map.collideWith(r, results);

        if (results.size() == 0)
            return null;

        float mindist = Short.MAX_VALUE;
        Vector3f realPoint = null;

        for (CollisionResult result : results){

            Vector3f contact = result.getContactPoint();
            float dist = contact.distance(position);

            if(dist < mindist){
                realPoint = contact;
                mindist = dist;
            }

        }

        if(realPoint == null)
            realPoint = results.getClosestCollision().getContactPoint();

        position.z = realPoint.z;

        return position;
    }

    /**
     * Old style method with custom retail
     */

    @SuppressWarnings( "deprecation" )
    public static float getZ(int mapId, int instanceId, float x, float y, float defaultZ) {

        AionMap map = _loadedMaps.get(mapId);

        CollisionResults results = new CollisionResults(false, instanceId);


        Vector3f pos = new Vector3f(x, y, defaultZ + 2);
        Vector3f dir = new Vector3f(x, y, defaultZ - 100);

        Float limit = pos.distance(dir);
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit);
        map.collideWith(r, results);

        //float averrageZ = map.getAverageTerrainZ(x, y);
        float resultZ = 0;
        if (results.size() > 0) {

            resultZ = results.getClosestCollision().getContactPoint().z;

            //if (Float.isNaN(averrageZ))
            //averrageZ = contactZ;
            //else
            //averrageZ = Math.max(contactZ, averrageZ);
        } else
            resultZ = defaultZ;

        return resultZ;
    }


    /**
     * Old style method with custom retail
     */
    @SuppressWarnings( "deprecation" )
    public static float getZ(int mapId, int instanceId, float x, float y) {

        AionMap map = _loadedMaps.get(mapId);

        CollisionResults results = new CollisionResults(false, instanceId);

        Vector3f pos = new Vector3f(x, y, 4000);
        Vector3f dir = new Vector3f(x, y, 0);

        Float limit = pos.distance(dir);
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit);
        map.collideWith(r, results);

        //float averrageZ = map.getAverageTerrainZ(x, y);`
        float resultZ = 0;
        if (results.size() > 0) {

            resultZ = results.getClosestCollision().getContactPoint().z;

            //if (Float.isNaN(averrageZ))
            //averrageZ = contactZ;
            //else
            //averrageZ = Math.max(contactZ, averrageZ);
        }

        return resultZ;
    }

    /**
     * Calculates collisions of ray from center of source object to selected coordinates
     *
     * @param object     Source object
     * @param x          Target x
     * @param y          Target y
     * @param intentions Acceptible obstacles types
     * @param selector   Collision selector type
     * @return Collision check result
     */
    public static CollisionResults getCollisions(Creature object, float x, float y, byte intentions, Selector selector) {
        return getCollisions(
                object,
                x, y, object.getObjectTemplate().getBoundRadius().getUpper(),
                intentions,
                selector);
    }

    /**
     * Calculates collisions of ray from center of source object to selected coordinates
     *
     * @param object       Source object
     * @param x            Target x
     * @param y            Target y
     * @param heightOffset Target point height offset
     * @param intentions   Acceptible obstacles types
     * @param selector     Collision selector type
     * @return Collision check result
     */
    public static CollisionResults getCollisions(Creature object, float x, float y, float heightOffset, byte intentions, Selector selector) {


        float ox, oy, oz;
        ox = object.getMoveController().beginX();
        oy = object.getMoveController().beginY();
        oz = GeoEngine.getZ(
                object.getWorldId(),
                object.getInstanceId(),
                ox, oy, object.getMoveController().beginZ());

        Vector3f source = new Vector3f(ox, oy, oz);
        Vector3f target = new Vector3f(x, y, oz);

        source.z += object.getObjectTemplate().getBoundRadius().getUpper();
        target.z += heightOffset;

        Vector3f direction = GeomUtil.getDirection3D(source, target);

        /*
        float collision = object.getCollision();
        source = GeomUtil.getNextPoint3D(source, direction.negate(), collision);
        */

        Ray ray = new Ray(source, direction);
        ray.setLimit(source.distance(target));
        //ray.setLimit(source.distance(target) + collision);

        return getCollisions(object.getWorldId(), object.getInstanceId(), ray, intentions, selector);
    }

    /**
     * Calculates collisions of ray
     *
     * @param mapId      Map id
     * @param instanceId Instance id
     * @param ray        Ray
     * @param intentions Acceptible obstacles types
     * @param selector   Collision selector type
     * @return Collision check result
     */
    public static CollisionResults getCollisions(int mapId, int instanceId, Ray ray, byte intentions, Selector selector) {


        AionMap map = _loadedMaps.get(mapId);
        CollisionResults results = new CollisionResults(false, instanceId, selector);

        map.collideWith(ray, results);

        return results;
    }

    /**
     * Return first available collision between two objects or null if no collisions detected
     * WARNING! No terrain collisions calculated by this method
     *
     * @param source source object
     * @param target target object
     */
    public static CollisionResult getObjectsCollision(VisibleObject source, VisibleObject target, byte collisionCheck) {

        WorldPosition sourcePos = source.getPosition();
        WorldPosition targetPos = target.getPosition();

        if (sourcePos.getMapId() != targetPos.getMapId() || sourcePos.getInstanceId() != targetPos.getInstanceId())
            return null;

        return getObjectsCollision(

                sourcePos.getMapId(), sourcePos.getInstanceId(),

                new Vector3f(
                        sourcePos.getX(),
                        sourcePos.getY(),
                        sourcePos.getZ() + (source.getObjectTemplate().getBoundRadius().getUpper())),

                new Vector3f(
                        targetPos.getX(),
                        targetPos.getY(),
                        targetPos.getZ() + (target.getObjectTemplate().getBoundRadius().getUpper())),

                collisionCheck
        );
    }

    /**
     * Return first available collision or null if no collisions detected
     * WARNING! No terrain collisions calculated by this method
     *
     * @param mapId          source map id
     * @param instanceId     source instance id
     * @param source         source point
     * @param destination    destination point
     * @param collisionCheck accept only collisions marked by this flag
     */
    public static CollisionResult getObjectsCollision(int mapId, int instanceId,
                                                      Vector3f source,
                                                      Vector3f destination,
                                                      byte collisionCheck) {

        float limit = source.distance(destination);
        Vector3f direction = destination.clone().subtractLocal(source).normalizeLocal();

        Ray ray = new Ray(source, direction);
        ray.setLimit(limit);

        CollisionResults results = new CollisionResults(false, instanceId);


        AionMap map = _loadedMaps.get(mapId);

        int result = map.collideWith(ray, results);

        if (result > 0) {

            CollisionResult res = results.getClosestCollision();
            if (res.getDistance() > limit)
                return null;

            return results.getClosestCollision();
        }

        return null;
    }

    /**
     * Validate target coordinates
     *
     * @param creature Source creature
     * @param x Target x
     * @param y Target y
     */
    public static boolean isPointValid(Creature creature, float x, float y) {
        AionMap map = _loadedMaps.get(creature.getWorldId());
        return map != null && map.isValid(x, y);
    }

    /**
     * Return next available movement point for selected creature
     *
     * @param creature   Source creature
     * @param vectorX    Movement vector X
     * @param vectorY    Movement vector Y
     * @param intentions Collision intentions
     */
    public static Vector3f getAvailablePoint(Creature creature, float vectorX, float vectorY, byte intentions) {

        float x = creature.getMoveController().beginX();
        float y = creature.getMoveController().beginY();

        float targetX = x + vectorX;
        float targetY = y + vectorY;

        Vector2f tg = new Vector2f(targetX, targetY);
        Vector2f sg = new Vector2f(creature.getMoveController().beginX(), creature.getMoveController().beginY());

        CollisionResults result = getCollisions(creature,
                targetX,
                targetY,
                intentions,
                Selector.ALL
        );

        Vector3f target = new Vector3f(targetX, targetY, creature.getMoveController().beginZ() + creature.getObjectTemplate().getBoundRadius().getUpper() / 2f);

        //if (result.size() == 0) {
        //target = new Vector3f(targetX, targetY, creature.getMoveController().beginZ() + creature.getObjectTemplate().getBoundRadius().getUpper() /2f);
        //} else {

        if (result.size() > 0 && result.getClosestCollision().getContactPoint().distance(new Vector3f(x, y, creature.getMoveController().beginZ())) < tg.distance(sg) + 3f) {

            Vector3f origin = result.getClosestCollision().getContactPoint();
            Vector3f efPos = new Vector3f(
                    creature.getMoveController().beginX(),
                    creature.getMoveController().beginY(),
                    creature.getMoveController().beginZ());

            Vector3f dic = GeomUtil.getDirection3D(origin, efPos);

            target = GeomUtil.getNextPoint3D(
                    origin,
                    dic,
                    creature.getObjectTemplate().getBoundRadius().getCollision());
        }

        //}

        Vector3f ret = target;
        if (!creature.isInState(CreatureState.FLYING) && !creature.getPosition().isInstanceMap()) {

            ret = setGroundZ(creature.getWorldId(), creature.getInstanceId(), target);
            if (ret == null)
                ret = new Vector3f(
                        creature.getMoveController().beginX(),
                        creature.getMoveController().beginY(),
                        creature.getMoveController().beginZ());
        }

        return ret;
    }

    /* Return model alias by model template id */
    public static String getModelAlias(short modelId) {
        return _modelNames.get(modelId);
    }

    /* Return model alias by model chield mesh */
    public static String getModelAlias(AionMesh mesh) {
        return getModelAlias(mesh.ModelId);
    }

    public static AionModel getModel(String fileName, int mapId) {

        Log.info("Request model " + fileName);
        return _loadedMaps.get(mapId).getModel(fileName);
    }

    public static AionModel getDoorModel(String fileName, int mapId, float x, float y, float z) {
        return _loadedMaps.get(mapId).getDoorModel(fileName, x, y, z);
    }

    private static void destroyDirectByteBuffer(Buffer toBeDestroyed) {
        Cleaner cleaner = ((DirectBuffer) toBeDestroyed).cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    /* Collision check type*/
    public enum Selector {
        ALL(0f), // check collision by object collision mesh
        VERTICAL_CHECK(2f); //check collision to extract height

        public final float Infelicity;

        /**
         *
         * @param infelicity Collision infelicity
         */
        Selector(float infelicity){
            Infelicity = infelicity;
        }
    }
}
