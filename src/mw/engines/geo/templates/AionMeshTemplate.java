package mw.engines.geo.templates;

import mw.engines.geo.bounding.BoundingBox;
import mw.engines.geo.collision.bih.BIHTree;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;

/**
 * Aion model mesh template
 * @author MetaWind
 */
public class AionMeshTemplate {

    /* Mesh alias */
    public final String Alias;

    /* Collision intention */
    public final byte CollisionIntention;

    /* Collision intention */
    public final byte MaterialId;

    /* Collision BIH tree */
    public final BIHTree CollisionTree;

    /* Mesh bounding box */
    public final BoundingBox InitialBounding;

    /**
     * Hidden constructor to prevent custom instancing
     * @param alias Mesh alias
     * @param vb Vertices buffer
     * @param ib Indexes buffer
     * @param collisionIntention Collision intntion
     */
    private AionMeshTemplate(String alias, FloatBuffer vb, ShortBuffer ib, short collisionIntention){

        Alias = alias;

        MaterialId = (byte) 0;
        CollisionIntention = (byte) collisionIntention;

        InitialBounding = new BoundingBox();
        InitialBounding.computeFromPoints(vb);

        CollisionTree = new BIHTree(vb, ib);
        CollisionTree.construct();
    }

    /**
     * Reads besh from byte buffer
     * @param buffer Source buffer
     * @return Readed mesh template
     */
    public static AionMeshTemplate Read(ByteBuffer buffer){

        short nameLen = buffer.getShort();
        byte[] nameArr = new byte[nameLen];
        buffer.get(nameArr);

        String nameAlias = new String(nameArr);

        int vectorCount = buffer.getInt();
        vectorCount *= 3;
        FloatBuffer vertices = MappedByteBuffer.allocateDirect(vectorCount * 4).asFloatBuffer();
        for (int i = 0; i < vectorCount; i++)
            vertices.put(buffer.getFloat());

        int indexesCounter = buffer.getInt();
        ShortBuffer indexes = MappedByteBuffer.allocateDirect(indexesCounter * 2).asShortBuffer();
        for (int i = 0; i < indexesCounter; i++)
            indexes.put(buffer.getShort());

        byte collisionIntention = buffer.get();

        byte materialsCount = buffer.get();
        for (int i = 0; i < materialsCount; i++){
            buffer.get();
        }

        return new AionMeshTemplate(nameAlias, vertices, indexes, collisionIntention);
    }
}
