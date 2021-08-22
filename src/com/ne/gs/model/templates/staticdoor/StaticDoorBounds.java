package com.ne.gs.model.templates.staticdoor;

import mw.engines.geo.bounding.BoundingBox;
import mw.engines.geo.math.Vector3f;

import javax.xml.bind.annotation.*;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaticDoorBounds")
public class StaticDoorBounds {

  @XmlAttribute
  private float x1;

  @XmlAttribute
  private float y1;

  @XmlAttribute
  private float z1;

  @XmlAttribute
  private float x2;

  @XmlAttribute
  private float y2;

  @XmlAttribute
  private float z2;

  @XmlTransient
  private BoundingBox boundingBox;

  public BoundingBox getBoundingBox() {
    if (boundingBox == null) {
      boundingBox = new BoundingBox(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
    }
    return boundingBox;
  }
}
