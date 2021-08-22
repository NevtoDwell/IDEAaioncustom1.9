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

import com.ne.gs.model.ai.Ai;
import com.ne.gs.model.templates.ai.AITemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "ai_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class AIData {

    @XmlElement(name = "ai", type = Ai.class)
    private List<Ai> templates;
    private final TIntObjectHashMap<AITemplate> aiTemplate = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        aiTemplate.clear();
        for (Ai template : templates) {
            aiTemplate.put(template.getNpcId(), new AITemplate(template));
        }

        templates = null;
    }

    public int size() {
        return aiTemplate.size();
    }

    public TIntObjectHashMap<AITemplate> getAiTemplate() {
        return aiTemplate;
    }
}
