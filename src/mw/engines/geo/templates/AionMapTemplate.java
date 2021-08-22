package mw.engines.geo.templates;

import mw.engines.geo.math.Matrix3f;
import mw.engines.geo.math.Vector3f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Aion level map template
 * @author MetaWind
 */
public class AionMapTemplate {

    /* Map id */
    public final int Id;

    /* Map heightmap */
    public final short[] Heightmap;

    /* Map heightmap size*/
    public final int HeightmapSize;

    /* Static object spots */
    public final List<MapSpot> Spots;

    /**
     * Hidden constructor to ensure custom instancing
     *
     * @param id        Map id
     * @param heightmap Level heightmap
     * @param spots     Static object spots
     */
    private AionMapTemplate(int id, short[] heightmap, List<MapSpot> spots) {

        Id = id;
        Heightmap = heightmap;
        HeightmapSize = (int) Math.sqrt(heightmap.length);
        Spots = spots;
    }

    /**
     * Reads map template from byte buffer
     *
     * @param buffer Source buffer
     * @return Readed map template
     */
    public static AionMapTemplate Read(ByteBuffer buffer) {

        int mapId = buffer.getInt();

        int pointsCounter = buffer.getInt();
        short[] heightmap = new short[pointsCounter];
        for (int in = 0; in < pointsCounter; in++)
            heightmap[in] = buffer.getShort();

        List<MapSpot> spots = new ArrayList<>();

        int objectsEndPosition = buffer.getInt() + buffer.position();
        while (buffer.position() < objectsEndPosition) {

            short id = buffer.getShort();

            Vector3f position = new Vector3f(
                    buffer.getFloat(),
                    buffer.getFloat(),
                    buffer.getFloat());

            boolean isIdenity = buffer.get() > 0;

            Matrix3f matrix = new Matrix3f();

            if (!isIdenity) {

                matrix.set(0, 0, buffer.getFloat());
                matrix.set(0, 1, buffer.getFloat());
                matrix.set(0, 2, buffer.getFloat());

                matrix.set(1, 0, buffer.getFloat());
                matrix.set(1, 1, buffer.getFloat());
                matrix.set(1, 2, buffer.getFloat());

                matrix.set(2, 0, buffer.getFloat());
                matrix.set(2, 1, buffer.getFloat());
                matrix.set(2, 2, buffer.getFloat());

            } else {
                matrix.loadIdentity();
            }

            float scale = buffer.getFloat();

            MapSpot spot = new MapSpot(id, position, matrix, scale);
            spots.add(spot);
        }

        return new AionMapTemplate(mapId, heightmap, spots);
    }

    public static class MapSpot {
        public final short ModelId;

        public final Vector3f Position;

        public final Matrix3f Transform;

        public final float Scale;

        public MapSpot(short id, Vector3f position, Matrix3f transform, float scale) {
            ModelId = id;
            Position = position;
            Transform = transform;
            Scale = scale;
        }
    }
}
