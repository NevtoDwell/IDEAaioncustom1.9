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
package mw.engines.geo.collision;

import mw.engines.geo.math.Vector3f;
import mw.engines.geo.scene.AionMesh;

/**
 * A <code>CollisionResult</code> represents a single collision instance
 * between two {@link Collidable}. A collision check can result in many
 * collision instances (places where collision has occured).
 *
 * @author Kirill Vainer
 */
public class CollisionResult implements Comparable<CollisionResult> {

    private AionMesh aionMesh;
    private Vector3f contactPoint;
    private Vector3f contactNormal;
    private float distance;
    private int triangleIndex;

    public CollisionResult(AionMesh geometry, Vector3f contactPoint, float distance, int triangleIndex) {
        this.aionMesh = geometry;
        this.contactPoint = contactPoint;
        this.distance = distance;
        this.triangleIndex = triangleIndex;
    }

    public CollisionResult(Vector3f contactPoint, float distance) {
        this.contactPoint = contactPoint;
        this.distance = distance;
    }

    public CollisionResult(){
    }

    public void setAionMesh(AionMesh geom){
        this.aionMesh = geom;
    }

    public void setContactNormal(Vector3f norm){
        this.contactNormal = norm;
    }

    public void setContactPoint(Vector3f point){
        this.contactPoint = point;
    }

    public void setDistance(float dist){
        this.distance = dist;
    }

    public void setTriangleIndex(int index){
        this.triangleIndex = index;
    }

    public int compareTo(CollisionResult other) {
        return Float.compare(distance, other.distance);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CollisionResult){
            return ((CollisionResult)obj).compareTo(this) == 0;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(distance);
    }

    public Vector3f getContactPoint() {
        return contactPoint;
    }

    public Vector3f getContactNormal() {
        return contactNormal;
    }

    public float getDistance() {
        return distance;
    }

    public AionMesh getAionMesh() {
        return aionMesh;
    }

    public int getTriangleIndex() {
        return triangleIndex;
    }

    public String toString() {
        return "CollisionResult[geometry=" + aionMesh
                                + ", contactPoint=" + contactPoint
                                + ", contactNormal=" + contactNormal
                                + ", distance=" + distance
                                + ", triangleIndex=" + triangleIndex
                                + "]";
    }
}
