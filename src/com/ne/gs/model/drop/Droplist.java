package com.ne.gs.model.drop;

import com.ne.gs.model.Race;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author MetaWind
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Droplist {

    @XmlAttribute(name = "race")
    protected Race race = Race.PC_ALL;

    @XmlAttribute(name = "use_category")
    protected Boolean useCategory = true;

    @XmlElement(name = "drop")
    protected List<Drop> drop;

    /**
     * Return creature race if templae is race restricted
     * @return Creature race or Race.PC_ALL if template are not race restricted
     */
    public Race getRace(){
        return race;
    }


    /**
     * Will be removed later, unbelivable shit from previous drop system
     */
    @Deprecated
    public boolean isUseCategory() {
        return useCategory;
    }
}
