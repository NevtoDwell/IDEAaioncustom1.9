package mw.engines.geo.scene;

import com.ne.gs.utils.MathUtil;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.collision.Collidable;
import mw.engines.geo.collision.CollisionResult;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.collision.UnsupportedCollisionException;
import mw.engines.geo.math.Ray;
import mw.engines.geo.math.Triangle;
import mw.engines.geo.math.Vector3f;
import mw.engines.geo.templates.AionMapTemplate;
import mw.utils.GeomUtil;

import java.util.List;

/**
 * Aion level map object
 *
 * @author MetaWind
 */
public class AionMap implements Collidable {

    /* Terrain height scale factor */
    private static final float HEIGHT_SCALE =  32f;

    /* Map template */
    private final AionMapTemplate _mapTemplate;

    /* Static map objects */
    private final List<AionModel> _staticObjects;


    /**
     * Default constructor
     *
     * @param mapTemplate Aion map template
     * @param objects Map static objects
     */
    public AionMap(AionMapTemplate mapTemplate, List<AionModel> objects){
        _mapTemplate = mapTemplate;
        _staticObjects = objects;
    }

    public boolean isValid(float x, float y){
        return x > 0 && y > 0 && !Float.isNaN(x) && !Float.isNaN(y);
    }


    /**
     * Return model by mesh name
     * @param meshFile
     * @return
     */
    public AionModel getModel(String meshFile){

        for (AionModel model : _staticObjects){
            String alias = GeoEngine.getModelAlias(model.Id);
            if(alias.equalsIgnoreCase(meshFile))
                return model;
        }

        return null;
    }

    /**
     * Return model by mesh name
     * @param meshFile
     * @return
     */
    public AionModel getDoorModel(String meshFile, float x, float y, float z){

        AionModel last = null;
        double mindist = Short.MAX_VALUE;

        for (AionModel model : _staticObjects){
            String alias = GeoEngine.getModelAlias(model.Id);
            if(alias.endsWith(meshFile)){

                double dist = MathUtil.getDistance(x,y,z, model.X,model.Y,model.Z);
                if(dist < mindist){
                    last = model;
                    mindist = dist;
                }
            }
        }

        return last;
    }

    @Override
    public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {

        int res = 0;
        if(other instanceof Ray) {

            Ray ray = (Ray)other;
            res += collideWithTerrain((Ray) other, results);

            //vertical terrain ray trace check
            if( results.Selector == GeoEngine.Selector.VERTICAL_CHECK && res > 0){

                //ray origin z equals origin z + 2, just check distance with 0.1f infelicity
                if(results.getClosestCollision().getDistance() < GeoEngine.Selector.VERTICAL_CHECK.Infelicity) //no need to calculate any other collisions
                    return res;
            }
        }

        if(results.is_onlyFirst() && res > 0)
            return res;


        for(AionModel aionModel : _staticObjects){
            res += aionModel.collideWith(other, results);
            if(results.is_onlyFirst() && res > 0)
                break;
        }

        return res;
    }

    /**
     * Calculate collisions between ray and terrain
     * @param ray Source ray
     * @param collisionResults Collision results collection
     * @return Collisions counter
     */
    private int collideWithTerrain(Ray ray, CollisionResults collisionResults){

        assert ray.limit > 0;

        Vector3f current = ray.origin.clone();

        float limit = ray.limit + (2 - ray.limit % 2);

        float distance = 0;
        int result = 0;

        int xdev = 0, ydev = 0;
        float p1 = 0, p2 = 0, p3 = 0, p4 = 0;

        while (distance <= limit){

            int cellX = (int)Math.floor(current.x) / 2;
            int cellY = (int)Math.floor(current.y) / 2;

            if(xdev != cellX || ydev != cellY) { //skip if cell is same as before

                xdev = cellX;
                ydev = cellY;

                if (xdev + 2 > _mapTemplate.HeightmapSize || ydev + 2 > _mapTemplate.HeightmapSize)
                    return result;

                if (_mapTemplate.Heightmap.length < 4) {
                    p1 = p2 = p3 = p4 = _mapTemplate.Heightmap[0] / HEIGHT_SCALE;
                } else {

                    p1 = _mapTemplate.Heightmap[(ydev + (xdev * _mapTemplate.HeightmapSize))] / HEIGHT_SCALE;
                    p2 = _mapTemplate.Heightmap[((ydev + 1) + (xdev * _mapTemplate.HeightmapSize))] / HEIGHT_SCALE;
                    p3 = _mapTemplate.Heightmap[((ydev) + ((xdev + 1) * _mapTemplate.HeightmapSize))] / HEIGHT_SCALE;
                    p4 = _mapTemplate.Heightmap[((ydev + 1) + ((xdev + 1) * _mapTemplate.HeightmapSize))] / HEIGHT_SCALE;
                }


                float resX = xdev * 2;
                float resY = ydev * 2;

                Triangle t1 = new Triangle(
                        new Vector3f(resX, resY, p1),
                        new Vector3f(resX, resY + 2, p2),
                        new Vector3f(resX + 2, resY, p3));

                Vector3f out1 = new Vector3f();
                if (ray.intersectWhere(t1, out1)) {

                    float dist = ray.origin.distance(out1);
                    if(dist <= ray.limit) {
                        collisionResults.addCollision(new CollisionResult(out1, dist));
                        result++;
                    }
                    return result;
                }


                Triangle t2 = new Triangle(
                        new Vector3f(resX + 2, resY + 2, p4),
                        new Vector3f(resX, resY + 2, p2),
                        new Vector3f(resX + 2, resY, p3));

                Vector3f out2 = new Vector3f();
                if (ray.intersectWhere(t2, out2)) {

                    float dist = ray.origin.distance(out2);
                    if(dist <= ray.limit) {
                        collisionResults.addCollision(new CollisionResult(out2, dist));
                        result++;
                    }
                    return result;
                }
            }

            current = GeomUtil.getNextPoint3D(current, ray.direction, 1);
            distance+= 1;
        }

        return result;
    }
    /**
     * Dummy aion level map implimentation
     */
    public static class DummyMap extends AionMap{

        /**
         * Default constructor
         */
        public DummyMap() {
            super(null, null);
        }

        @Override
        public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {
            return 0;
        }
    }
}
