package com.ne.gs.model.gameobjects.player;

import mw.engines.geo.math.Vector3f;

/**
 *
 * @author Alex
 */
public class Coordinates {

  private int worldId;
  private int instanceId;
  private float x;
  private float y;
  private float z;
  private float x2;
  private float y2;
  private float z2;
  private float direction;
  private byte heading;
  private Vector3f vector;

  public Coordinates(Vector3f vector) {
    this.vector = vector;
  }

  public Coordinates(Vector3f vector, float x, float y, float z) {
    this.vector = vector;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Coordinates(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Coordinates(int worldId, int instanceId, float x, float y, float z) {
    this.worldId = worldId;
    this.instanceId = instanceId;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Coordinates(int worldId, int instanceId, float x, float y, float z, float direction) {
    this.worldId = worldId;
    this.instanceId = instanceId;
    this.x = x;
    this.y = y;
    this.z = z;
    this.direction = direction;
  }

  public Coordinates(int worldId, int instanceId, float x, float y, float z, float direction, byte heading) {
    this.worldId = worldId;
    this.instanceId = instanceId;
    this.x = x;
    this.y = y;
    this.z = z;
    this.direction = direction;
    this.heading = heading;
  }

  public Coordinates(int worldId, int instanceId, float x, float y, float z, float direction, byte heading, Vector3f vector) {
    this.worldId = worldId;
    this.instanceId = instanceId;
    this.x = x;
    this.y = y;
    this.z = z;
    this.direction = direction;
    this.heading = heading;
    this.vector = vector;
  }

  public int getWorldId() {
    return worldId;
  }

  public int getInstanceId() {
    return instanceId;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getZ() {
    return z;
  }

  public void setWorldId(int worldId) {
    this.worldId = worldId;
  }

  public void setInstanceId(int instanceId) {
    this.instanceId = instanceId;
  }

  public void setX(float x) {
    this.x = x;
  }

  public void setY(float y) {
    this.y = y;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public float getDirection() {
    return direction;
  }

  public void setDirection(float direction) {
    this.direction = direction;
  }

  public byte getHeading() {
    return heading;
  }

  public void setHeading(byte heading) {
    this.heading = heading;
  }

  public Vector3f getVector() {
    return vector;
  }

  public void setVector(Vector3f vector) {
    this.vector = vector;
  }

  public void setX2(float x2) {
    this.x2 = x2;
  }

  public void setY2(float y2) {
    this.y2 = y2;
  }

  public void setZ2(float z2) {
    this.z2 = z2;
  }

  public float getX2() {
    return x2;
  }

  public float getY2() {
    return y2;
  }

  public float getZ2() {
    return z2;
  }
}
