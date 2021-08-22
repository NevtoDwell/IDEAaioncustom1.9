package mw.engines.geo.scene;

import mw.engines.geo.bounding.BoundingVolume;
import mw.engines.geo.collision.Collidable;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.collision.UnsupportedCollisionException;
import mw.engines.geo.math.Matrix3f;
import mw.engines.geo.math.Matrix4f;
import mw.engines.geo.math.Ray;
import mw.engines.geo.math.Vector3f;
import mw.engines.geo.templates.AionMeshTemplate;

/**
 * Aion model mesh object
 *
 * @author MetaWind
 */
public class AionMesh implements Cloneable, Collidable {

    /* Model template id */
    public final short ModelId;

    /* World matrix are never changes in our case, but i leave it here */
    private final Matrix4f worldMatrix = new Matrix4f();

    /* Object template */
    private final AionMeshTemplate _template;

    /* Object bounding box */
    private final BoundingVolume boundingBox;

    /**
     * Default constructor
     */
    public AionMesh(short modelId, AionMeshTemplate template, Matrix3f rotation, Vector3f loc, float scale) {

        ModelId = modelId;

        _template = template;

        worldMatrix.loadIdentity();
        worldMatrix.setRotationMatrix(rotation);
        worldMatrix.scale(scale);
        worldMatrix.setTranslation(loc);

        boundingBox = template.InitialBounding.transform(worldMatrix, null);
    }

    @Override
    public int collideWith(Collidable other, CollisionResults results) throws UnsupportedCollisionException {

        /*TODO
        if ((_template.CollisionIntention & CollidableType.MOVEABLE.getId()) != 0) {
            return 0;
        }
        */

        if(_template.CollisionIntention == 0)
            return 0;
        /*if ((_template.CollisionIntention & results._intentions) == 0) {
            return 0;
        }*/

        if (other instanceof Ray && !boundingBox.intersects(((Ray) other)))
            return 0;

        int initial = results.size();

        int collisions = _template.CollisionTree.collideWith(other, worldMatrix, boundingBox, results);
        if (collisions > 0) {

            int result = results.size();
            for (int i = initial; i < result; i++)
                results.getCollisionDirect(i).setAionMesh(this);

        }
        return collisions;
    }

    public BoundingVolume getBoundingBox() {
        return boundingBox;
    }

    public AionMeshTemplate getTemplate(){
        return _template;
    }
}
