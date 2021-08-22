package mw.engines.geo;


import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.npc.NpcRating;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResult;
import mw.engines.geo.math.Ray;
import mw.engines.geo.math.Vector3f;
import mw.utils.GeomUtil;

/**
 * @author MetaWind
 */
public class GeoHelper {

    /* Maximal creature see distance */
    private static final float MAX_SEE_DISTANCE = 50f;

    @SuppressWarnings( "deprecation" )
    public static float getZ(VisibleObject object) {
        return GeoEngine.getZ(object.getWorldId(), object.getInstanceId(), object.getX(), object.getY(), object.getZ());
    }


    /**
     * Determines, can visible object see another one
     *
     * @param v1 first object
     * @param v2 second object
     * @return TRUE if no collisions detected
     */
    public static boolean canSee(VisibleObject v1, VisibleObject v2) {

        if (!v1.affectedByObstacles())
            return true;

        Vector3f sp = v1.getPosition().getPoint();
        Vector3f tp = v2.getPosition().getPoint();

        //raycast from center of source object to center of target
        sp.z += v1.getObjectTemplate().getBoundRadius().getUpper();
        tp.z += v2.getObjectTemplate().getBoundRadius().getUpper();

        float limit = (sp.distance(tp) - v2.getObjectTemplate().getBoundRadius().getCollision());
        if (limit <= 0)
            return true;

        if(limit > MAX_SEE_DISTANCE)
            return false;

        Vector3f dir = GeomUtil.getDirection3D(sp, tp);
        Ray ray = new Ray(sp, dir);
        ray.setLimit(limit);

        return !GeoEngine.collide(
                v1.getWorldId(),
                v1.getInstanceId(),
                ray,
                (byte)(CollidableType.PHYSICAL.getId() | CollidableType.DOOR.getId() | CollidableType.SKILL.getId()));
    }
}
