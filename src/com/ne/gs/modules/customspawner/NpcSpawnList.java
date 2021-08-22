package com.ne.gs.modules.customspawner;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.utils.xml.ElementList;
import com.ne.gs.modules.common.NpcSpawn;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "NpcSpawnList")
@XmlRootElement(name = "spawns")
public class NpcSpawnList extends ElementList<NpcSpawn> {
    @XmlElement(name = "spawn")
    public List<NpcSpawn> getSpawns() {
        return getElements();
    }
}
