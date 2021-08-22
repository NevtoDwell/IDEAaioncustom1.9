/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.assemblednpc.AssembledNpcTemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "assembled_npcs")
@XmlAccessorType(XmlAccessType.FIELD)
public class AssembledNpcsData {

    @XmlElement(name = "assembled_npc", type = AssembledNpcTemplate.class)
    private List<AssembledNpcTemplate> templates;
    private final TIntObjectHashMap<AssembledNpcTemplate> assembledNpcsTemplates = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (AssembledNpcTemplate template : templates) {
            assembledNpcsTemplates.put(template.getNr(), template);
        }

        templates = null;
    }

    public int size() {
        return assembledNpcsTemplates.size();
    }

    public AssembledNpcTemplate getAssembledNpcTemplate(int i) {
        return assembledNpcsTemplates.get(i);
    }

}
