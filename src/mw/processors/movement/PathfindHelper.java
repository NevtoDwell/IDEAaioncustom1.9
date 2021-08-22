package mw.processors.movement;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.math.FastMath;
import mw.utils.GeomUtil;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.math.Ray;
import mw.engines.geo.math.Vector2f;
import mw.engines.geo.math.Vector3f;

/**
 * Calculator of pathfinding mob's steps
 *
 * @author MetaWind
 */
public class PathfindHelper {

    /* Distance beetween onjects infelicity */
    private static final float OBJECTS_DISTANCE_INFELICITY = 0.01f;

    /* Obstacles for movements */
    private static final byte MOVEMENT_OBSTACLES_FLAGS = CollidableType.PHYSICAL.getId();

    /* Owner visible angle in degrees */
    private static final int VISIBLE_ANGLE = 220;

    /* Pathfinding single step angle to search accepible movement point */
    private static final int PATHFIND_ANGLE_STEP = 20;

    /**
     * Select next movement step to follow target point
     *
     * @param source  Source creature
     * @param target  Target creature
     * @param maxStep Maximal pathfind step length
     * @return Selected point or NULL if target can't be reached
     */
    public static Vector3f selectStep(Creature source, Vector3f target, float maxStep, float targetOffset) {

        int mapId = source.getPosition().getMapId();
        int instanceId = source.getPosition().getInstanceId();


        float zOffset = Math.max(0.6f,source.getObjectTemplate().getBoundRadius().getUpper() * 0.7f);

        Vector3f sourcePoint = source.getPosition().getPoint();
        Vector3f targetPoint = target.clone();

        //real source and target movement points for ray trace check
        sourcePoint.z += zOffset;

        if(GeoEngine.setGroundZ(source,targetPoint) == null) //if creature can't fly, Z will be corrected
            return null;

        targetPoint.z += zOffset;

        Vector3f direction = GeomUtil.getDirection3D(sourcePoint, targetPoint);

        float futureDistance = sourcePoint.distance(targetPoint) - targetOffset;

        if (futureDistance < OBJECTS_DISTANCE_INFELICITY)
            return null; //target reached

        if (futureDistance > maxStep)
            futureDistance = maxStep;

        targetPoint = GeomUtil.getNextPoint3D(sourcePoint, direction, futureDistance);

        Ray ray = new Ray(sourcePoint, targetPoint.subtract(sourcePoint).normalizeLocal());
        ray.setLimit(futureDistance);

        boolean hasCollisions = source.affectedByObstacles() && GeoEngine.collide(mapId, instanceId, ray, MOVEMENT_OBSTACLES_FLAGS);
        if (!hasCollisions && GeoEngine.setGroundZ(source, targetPoint) != null)
            return targetPoint;

        final int rounds = (VISIBLE_ANGLE / PATHFIND_ANGLE_STEP) + 1;
        final int offset = VISIBLE_ANGLE / 2;

        Vector3f closetsPoint = null;
        double minimalDistance = Short.MAX_VALUE;

        //now, rotate target point by visible angle to find available point
        for (int i = 0; i < rounds; i++) {

            double angle =
                    Math.toDegrees(
                            Math.atan2(targetPoint.y - sourcePoint.y, targetPoint.x - sourcePoint.x)
                    ) + (i * PATHFIND_ANGLE_STEP - offset);

            Vector2f rotated2D =
                    GeomUtil.getNextPoint2D(new Vector2f(sourcePoint.x, sourcePoint.y), (float) angle, futureDistance);

            if (!GeoEngine.isPointValid(source, rotated2D.x, rotated2D.y))
                continue;

            float distanceToTarget = FastMath.sqrt(rotated2D.distanceSquared(target.x, target.y));

            //if current distance greater that was on previous step - last point was closest available
            if (distanceToTarget > minimalDistance)
                break;

            Vector3f rotated = new Vector3f(
                    rotated2D.x,
                    rotated2D.y,
                    targetPoint.z);

            if(GeoEngine.setGroundZ(source, rotated) == null)
                continue;

            rotated.z += zOffset;

            Vector3f directionToRotated = GeomUtil.getDirection3D(sourcePoint, rotated);

            ray = new Ray(sourcePoint, directionToRotated);
            ray.setLimit(rotated.distance(sourcePoint));

            hasCollisions = source.affectedByObstacles() && GeoEngine.collide(mapId, instanceId, ray, MOVEMENT_OBSTACLES_FLAGS);
            if (!hasCollisions) {
                closetsPoint = rotated;
                minimalDistance = distanceToTarget;
            }

            rotated.z -= zOffset;
        }


        return closetsPoint;
    }

    /**
     * Select next movement point to follow target
     *
     * @param source  Source creature
     * @param target  Target creature
     * @param maxStep Maximal pathfind step length
     * @return Selected point or NULL if target can't be reached
     */
    public static Vector3f selectFollowStep(Creature source, VisibleObject target, float maxStep) {

        int mapId = source.getPosition().getMapId();
        int instanceId = source.getPosition().getInstanceId();

        if (target.getPosition().getMapId() != mapId || target.getPosition().getInstanceId() != instanceId)
            return null;

        float offset = source.getObjectTemplate().getBoundRadius().getCollision()
                + target.getObjectTemplate().getBoundRadius().getCollision();

        Vector3f point = target.getPosition().getPoint();

        assert point.x != 0 && point.y != 0;

        return selectStep(source, point, maxStep, offset);
    }


    /**
     * Select next random point for creature movement
     *
     * @param source Source creature
     * @param minRange Minimal walk range
     * @param maxRange Maximal walk range
     * @return
     */
    public static Vector3f getRandomPoint(Creature source, float minRange, float maxRange) {

        Vector3f origin = source.getPosition().getPoint();

        if (!GeoEngine.isPointValid(source, origin.x, origin.y))
            return null;

        assert minRange > 0 && maxRange > minRange;

        final int SearchAngle = 360;
        final int AngleStep = 60;

        int randDist = (int) (Math.random() * maxRange + minRange);
        int randAngle = (int) (Math.random() * SearchAngle);

        for (int i = 0; i < SearchAngle; i+= AngleStep) {

            Vector2f rotated2D =
                    GeomUtil.getNextPoint2D(new Vector2f(origin.x, origin.y), (float) randAngle + i, randDist);

            if (!GeoEngine.isPointValid(source, rotated2D.x, rotated2D.y))
                continue;

            Vector3f rotated = new Vector3f(
                    rotated2D.x,
                    rotated2D.y,
                    origin.z);

            if(GeoEngine.setGroundZ(source, rotated) == null)
                continue;

            if(rotated.distance(origin) < minRange)
                continue;

            return rotated;
        }

        return null;
    }
}
