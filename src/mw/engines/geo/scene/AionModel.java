package mw.engines.geo.scene;

import mw.engines.geo.GeoEngine;
import mw.engines.geo.collision.Collidable;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.collision.UnsupportedCollisionException;
import mw.engines.geo.math.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Aion 3d model object
 *
 * @author MetaWind
 */
public class AionModel implements Collidable {

    /* Object id */
    public final short Id;

    /* Object meshes */
    private final List<AionMesh> _meshes;

    /* Active regions */
    public final Set<Integer> ActiveRegions;

    public final float X,Y,Z;

    private boolean _isStaticObject = false;

    /**
     * Default constructor
     * @param id Object id
     * @param meshes Object meshes collection
     */
    public AionModel(short id, Vector3f position, List<AionMesh> meshes){
        Id = id;

        X = position.x;
        Y = position.y;
        Z = position.z;

        _meshes = meshes;

        ActiveRegions = new HashSet<>();
    }

    public void activateAt(int instanceId){

        _isStaticObject = true;
        ActiveRegions.add(instanceId);
    }

    public void deactivateAt(int instanceId){

        _isStaticObject = true;
        ActiveRegions.remove(instanceId);
    }

    @Override
    public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {

        if(_isStaticObject && !ActiveRegions.contains(results._instanceId))
            return 0;

        int result = 0;
        for (int i = 0; i < _meshes.size(); i++) {

            result += _meshes.get(i).collideWith(other, results);
            if(results.is_onlyFirst() && result > 0)
                break;
        }

        return result;
    }
}
