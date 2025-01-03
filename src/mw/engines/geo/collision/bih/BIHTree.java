/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package mw.engines.geo.collision.bih;

import mw.engines.geo.bounding.BoundingBox;
import mw.engines.geo.bounding.BoundingSphere;
import mw.engines.geo.bounding.BoundingVolume;
import mw.engines.geo.collision.Collidable;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.collision.UnsupportedCollisionException;
import mw.engines.geo.math.FastMath;
import mw.engines.geo.math.Matrix4f;
import mw.engines.geo.math.Ray;
import mw.engines.geo.math.Vector3f;
import mw.engines.geo.util.TempVars;

import static java.lang.Math.max;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BIHTree {

    public static final int MAX_TREE_DEPTH = 100;
    public static final int MAX_TRIS_PER_NODE = 21;
    private BIHNode root;
    private int maxTrisPerNode;
    private int numTris;
    private float[] pointData;
    private int[] triIndices;

    private transient float[] bihSwapTmp;

    private void initTriList(FloatBuffer vb, ShortBuffer ib) {
        pointData = new float[numTris * 3 * 3];
        int p = 0;
        for (int i = 0; i < numTris * 3; i += 3) {
            int vert = ib.get(i) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);

            vert = ib.get(i + 1) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);

            vert = ib.get(i + 2) * 3;
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert++);
            pointData[p++] = vb.get(vert);
        }

        triIndices = new int[numTris];
        for (int i = 0; i < numTris; i++) {
            triIndices[i] = i;
        }
    }

    public BIHTree(FloatBuffer vb, ShortBuffer ib){
        this(vb,ib, MAX_TRIS_PER_NODE);
    }

    public BIHTree(FloatBuffer vb, ShortBuffer ib, int maxTrisPerNode) {

        this.maxTrisPerNode = maxTrisPerNode;

        bihSwapTmp = new float[9];

        numTris = ib.capacity() / 3;
        initTriList(vb, ib);
    }

    public BIHTree() {
    }

    public void construct() {
        BoundingBox sceneBbox = createBox(0, numTris - 1);
        root = createNode(0, numTris - 1, sceneBbox, 0);
    }

    private BoundingBox createBox(int l, int r) {
        TempVars vars = TempVars.get();

        Vector3f min = vars.vect1.set(new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY));
        Vector3f max = vars.vect2.set(new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));

        Vector3f v1 = vars.vect3,
                v2 = vars.vect4,
                v3 = vars.vect5;

        for (int i = l; i <= r; i++) {
            getTriangle(i, v1, v2, v3);
            BoundingBox.checkMinMax(min, max, v1);
            BoundingBox.checkMinMax(min, max, v2);
            BoundingBox.checkMinMax(min, max, v3);
        }

        BoundingBox bbox = new BoundingBox(min, max);
        vars.release();
        return bbox;
    }

    int getTriangleIndex(int triIndex) {
        return triIndices[triIndex];
    }

    private int sortTriangles(int l, int r, float split, int axis) {
        int pivot = l;
        int j = r;

        TempVars vars = TempVars.get();

        Vector3f v1 = vars.vect1,
                v2 = vars.vect2,
                v3 = vars.vect3;

        while (pivot <= j) {
            getTriangle(pivot, v1, v2, v3);
            v1.addLocal(v2).addLocal(v3).multLocal(FastMath.ONE_THIRD);
            if (v1.get(axis) > split) {
                swapTriangles(pivot, j);
                --j;
            } else {
                ++pivot;
            }
        }

        vars.release();
        pivot = (pivot == l && j < pivot) ? j : pivot;
        return pivot;
    }

    private void setMinMax(BoundingBox bbox, boolean doMin, int axis, float value) {
        Vector3f min = bbox.getMin(null);
        Vector3f max = bbox.getMax(null);

        if (doMin) {
            min.set(axis, value);
        } else {
            max.set(axis, value);
        }

        bbox.setMinMax(min, max);
    }

    private float getMinMax(BoundingBox bbox, boolean doMin, int axis) {
        if (doMin) {
            return bbox.getMin(null).get(axis);
        } else {
            return bbox.getMax(null).get(axis);
        }
    }

    private BIHNode createNode(int l, int r, BoundingBox nodeBbox, int depth) {
        if ((r - l) < maxTrisPerNode || depth > MAX_TREE_DEPTH) {
            return new BIHNode(l, r);
        }

        BoundingBox currentBox = createBox(l, r);

        Vector3f exteriorExt = nodeBbox.getExtent(null);
        Vector3f interiorExt = currentBox.getExtent(null);
        exteriorExt.subtractLocal(interiorExt);

        int axis = 0;
        if (exteriorExt.x > exteriorExt.y) {
            if (exteriorExt.x > exteriorExt.z) {
                axis = 0;
            } else {
                axis = 2;
            }
        } else {
            if (exteriorExt.y > exteriorExt.z) {
                axis = 1;
            } else {
                axis = 2;
            }
        }
        if (exteriorExt.equals(Vector3f.ZERO)) {
            axis = 0;
        }

//        Arrays.sort(tris, l, r, comparators[axis]);
        float split = currentBox.getCenter().get(axis);
        int pivot = sortTriangles(l, r, split, axis);
        if (pivot == l || pivot == r) {
            pivot = (r + l) / 2;
        }

        //If one of the partitions is empty, continue with recursion: same level but different bbox
        if (pivot < l) {
            //Only right
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);
            return createNode(l, r, rbbox, depth + 1);
        } else if (pivot > r) {
            //Only left
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);
            return createNode(l, r, lbbox, depth + 1);
        } else {
            //Build the node
            BIHNode node = new BIHNode(axis);

            //Left child
            BoundingBox lbbox = new BoundingBox(currentBox);
            setMinMax(lbbox, false, axis, split);

            //The left node right border is the plane most right
            node.setLeftPlane(getMinMax(createBox(l, max(l, pivot - 1)), false, axis));
            node.setLeftChild(createNode(l, max(l, pivot - 1), lbbox, depth + 1)); //Recursive call

            //Right Child
            BoundingBox rbbox = new BoundingBox(currentBox);
            setMinMax(rbbox, true, axis, split);
            //The right node left border is the plane most left
            node.setRightPlane(getMinMax(createBox(pivot, r), true, axis));
            node.setRightChild(createNode(pivot, r, rbbox, depth + 1)); //Recursive call

            return node;
        }
    }

    public void getTriangle(int index, Vector3f v1, Vector3f v2, Vector3f v3) {
        int pointIndex = index * 9;

        v1.x = pointData[pointIndex++];
        v1.y = pointData[pointIndex++];
        v1.z = pointData[pointIndex++];

        v2.x = pointData[pointIndex++];
        v2.y = pointData[pointIndex++];
        v2.z = pointData[pointIndex++];

        v3.x = pointData[pointIndex++];
        v3.y = pointData[pointIndex++];
        v3.z = pointData[pointIndex++];
    }

    public void swapTriangles(int index1, int index2) {
        int p1 = index1 * 9;
        int p2 = index2 * 9;

        // store p1 in tmp
        System.arraycopy(pointData, p1, bihSwapTmp, 0, 9);

        // copy p2 to p1
        System.arraycopy(pointData, p2, pointData, p1, 9);

        // copy tmp to p2
        System.arraycopy(bihSwapTmp, 0, pointData, p2, 9);

        // swap indices
        int tmp2 = triIndices[index1];
        triIndices[index1] = triIndices[index2];
        triIndices[index2] = tmp2;
    }

    private int collideWithRay(Ray r,
            Matrix4f worldMatrix,
            BoundingVolume worldBound,
            CollisionResults results) {

        TempVars vars = TempVars.get();
        try {
            CollisionResults boundResults = vars.collisionResults;
            boundResults.clear();
            worldBound.collideWith(r, boundResults);
            if (boundResults.size() > 0) {
                float tMin = boundResults.getClosestCollision().getDistance();
                float tMax = boundResults.getFarthestCollision().getDistance();

                if (tMax <= 0) {
                    tMax = Float.POSITIVE_INFINITY;
                } else if (tMin == tMax) {
                    tMin = 0;
                }

                if (tMin <= 0) {
                    tMin = 0;
                }

                if (r.getLimit() < Float.POSITIVE_INFINITY) {
                    tMax = Math.min(tMax, r.getLimit());
                    if (tMin > tMax){
                        return 0;
                    }
                }

    //            return root.intersectBrute(r, worldMatrix, this, tMin, tMax, results);
                return root.intersectWhere(r, worldMatrix, this, tMin, tMax, results);
            }
            return 0;
        } finally {
            vars.release();
        }
    }

    private int collideWithBoundingVolume(BoundingVolume bv,
            Matrix4f worldMatrix,
            CollisionResults results) {
        BoundingBox bbox;
        if (bv instanceof BoundingSphere) {
            BoundingSphere sphere = (BoundingSphere) bv;
            bbox = new BoundingBox(bv.getCenter().clone(), sphere.getRadius(),
                    sphere.getRadius(),
                    sphere.getRadius());
        } else if (bv instanceof BoundingBox) {
            bbox = new BoundingBox((BoundingBox) bv);
        } else {
            throw new UnsupportedCollisionException("BoundingVolume:" + bv);
        }

        bbox.transform(worldMatrix.invert(), bbox);
        return root.intersectWhere(bv, bbox, worldMatrix, this, results);
    }

    public int collideWith(Collidable other,
            Matrix4f worldMatrix,
            BoundingVolume worldBound,
            CollisionResults results) {

        if (other instanceof Ray) {
            Ray ray = (Ray) other;
            return collideWithRay(ray, worldMatrix, worldBound, results);
        } else if (other instanceof BoundingVolume) {
            BoundingVolume bv = (BoundingVolume) other;
            return collideWithBoundingVolume(bv, worldMatrix, results);
        } else {
            throw new UnsupportedCollisionException("Collidable:" + other);
        }
    }
}
