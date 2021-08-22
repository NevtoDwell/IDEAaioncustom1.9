package com.ne.gs.dataholders;

import com.ne.gs.model.drop.DropGroup;
import com.ne.gs.model.drop.NpcDrop;
import com.ne.gs.skillengine.model.MotionTime;
import gnu.trove.map.hash.THashMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author MetaWind
 */
@XmlRootElement(name = "droplists")
@XmlAccessorType(XmlAccessType.FIELD)
public class DroplistsData {

    /* Available droplists */
    @XmlElement(name = "droplist")
    private List<DropGroup> dropLists;

    /* Droplists sorted by id */
    @XmlTransient
    private final THashMap<Integer, DropGroup> byId = new THashMap<>();

    /**
     * JAXB unmarshall callback
     */
    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (DropGroup droplist : dropLists) {
            byId.put(droplist.getId(), droplist);
        }
        dropLists = null;
    }

    /**
     * @param id Droplist id
     * @return droplist by droplist template id
     */
    public DropGroup getDroplist(int id) {
        return byId.get(id);
    }

    /**
     * @param ids Droplist ids
     * @return droplists collection
     */
    public List<DropGroup> getDroplists(int[] ids) {

        List<DropGroup> result = new ArrayList<>(ids.length);
        for (int id : ids)
            result.add(byId.get(id));

        return result;
    }
    /**
     * @return Loaded droplists counter
     */
    public int size() {
        return byId.size();
    }
}
