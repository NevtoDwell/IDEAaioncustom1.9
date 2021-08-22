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
import java.util.ArrayList;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.templates.Guides.GuideTemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "guides")
@XmlAccessorType(XmlAccessType.FIELD)
public class GuideHtmlData {

    @XmlElement(name = "guide", type = GuideTemplate.class)
    private List<GuideTemplate> guideTemplates;
    private final TIntObjectHashMap<ArrayList<GuideTemplate>> templates = new TIntObjectHashMap<>();
    private final int CLASS_ALL = 255;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (GuideTemplate template : guideTemplates) {
            addTemplate(template);
        }
        guideTemplates = null;
    }

    private void addTemplate(GuideTemplate template) {
        Race race = template.getRace();
        if (race == null) {
            race = Race.PC_ALL;
        }
        int classId = template.getPlayerClass() == null ? CLASS_ALL : template.getPlayerClass().ordinal();

        int hash = makeHash(classId, race.ordinal(), template.getLevel());
        ArrayList<GuideTemplate> value = templates.get(hash);
        if (value == null) {
            value = new ArrayList<>();
            templates.put(hash, value);
        }
        value.add(template);
    }

    public int size() {
        return templates.size();
    }

    public TIntObjectHashMap<ArrayList<GuideTemplate>> getTemplates() {
        return templates;
    }

    public GuideTemplate getTemplateByTitle(String title) {
        for (int templateHash : templates.keys()) {
            for (GuideTemplate template : templates.get(templateHash)) {
                if (template.getTitle().equals(title)) {
                    return template;
                }
            }
        }
        return null;
    }

    public List<GuideTemplate> getTemplatesFor(PlayerClass playerClass, Race race, int level) {
        List<GuideTemplate> classRaceSpecificTemplates = templates.get(makeHash(playerClass.ordinal(), race.ordinal(),
            level));
        List<GuideTemplate> classSpecificTemplates = templates
            .get(makeHash(playerClass.ordinal(), Race.PC_ALL.ordinal(),
                level));
        List<GuideTemplate> raceSpecificTemplates = templates.get(makeHash(CLASS_ALL, race.ordinal(), level));
        List<GuideTemplate> generalTemplates = templates.get(makeHash(CLASS_ALL, Race.PC_ALL.ordinal(), level));

        List<GuideTemplate> guideTemplate = new ArrayList<>();

        if (classRaceSpecificTemplates != null) {
            guideTemplate.addAll(classRaceSpecificTemplates);
        }
        if (classSpecificTemplates != null) {
            guideTemplate.addAll(classSpecificTemplates);
        }
        if (raceSpecificTemplates != null) {
            guideTemplate.addAll(raceSpecificTemplates);
        }
        if (generalTemplates != null) {
            guideTemplate.addAll(generalTemplates);
        }

        return guideTemplate;
    }

    private static int makeHash(int classType, int race, int level) {
        int result = classType << 8;
        result = (result | race) << 8;
        return result | level;
    }
}
