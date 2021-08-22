package com.ne.gs.modules.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.utils.xml.ElementList;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "NpcSpawnList")
public class NpcSpawnList extends ElementList<NpcSpawn> {

    @XmlElement(name = "spawn")
    public List<NpcSpawn> getTimes() {
        return getElements();
    }

}
