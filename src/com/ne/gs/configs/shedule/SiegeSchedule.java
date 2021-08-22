/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.shedule;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;

import com.ne.commons.utils.xml.JAXBUtil;

@XmlRootElement(name = "siege_schedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeSchedule {

    @XmlElement(name = "fortress", required = true)
    private List<Fortress> fortressesList;

    @XmlElement(name = "source", required = true)
    private List<Source> sourcesList;

    public List<Fortress> getFortressesList() {
        return fortressesList;
    }

    public void setFortressesList(List<Fortress> fortressList) {
        fortressesList = fortressList;
    }

    public List<Source> getSourcesList() {
        return sourcesList;
    }

    public void setSourcesList(List<Source> sourceList) {
        sourcesList = sourceList;
    }

    public static SiegeSchedule load() {
        SiegeSchedule ss;
        try {
            String xml = FileUtils.readFileToString(new File("./config/shedule/siege_schedule.xml"));
            ss = JAXBUtil.deserialize(xml, SiegeSchedule.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sieges", e);
        }
        return ss;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "source")
    public static class Source {

        @XmlAttribute(required = true)
        private int id;

        @XmlElement(name = "siegeTime", required = true)
        private List<String> siegeTime;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<String> getSiegeTimes() {
            return siegeTime;
        }

        public void setSiegeTimes(List<String> siegeTime) {
            this.siegeTime = siegeTime;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "fortress")
    public static class Fortress {

        @XmlAttribute(required = true)
        private int id;

        @XmlElement(name = "siegeTime", required = true)
        private List<String> siegeTimes;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<String> getSiegeTimes() {
            return siegeTimes;
        }

        public void setSiegeTimes(List<String> siegeTimes) {
            this.siegeTimes = siegeTimes;
        }
    }

}
