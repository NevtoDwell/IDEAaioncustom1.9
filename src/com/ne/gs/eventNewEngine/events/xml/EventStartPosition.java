/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author userd
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventStartPosition")
public class EventStartPosition {

    @XmlAttribute(name = "x")
    protected int x;
    @XmlAttribute(name = "y")
    protected int y;
    @XmlAttribute(name = "z")
    protected int z;
    @XmlAttribute(name = "h")
    protected byte h;
    @XmlAttribute(name = "group")
    protected int group = 0;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public byte getH() {
        return h;
    }

    public int getGroup() {
        return group;
    }
}
