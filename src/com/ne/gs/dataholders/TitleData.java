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

import com.ne.gs.model.templates.TitleTemplate;

/**
 * @author xavier
 */
@XmlRootElement(name = "player_titles")
@XmlAccessorType(XmlAccessType.FIELD)
public class TitleData {

    @XmlElement(name = "title")
    private List<TitleTemplate> tts;

    private TIntObjectHashMap<TitleTemplate> titles;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        titles = new TIntObjectHashMap<>();
        for (TitleTemplate tt : tts) {
            titles.put(tt.getTitleId(), tt);
        }
        tts = null;
    }

    public TitleTemplate getTitleTemplate(int titleId) {
        return titles.get(titleId);
    }

    /**
     * @return titles.size()
     */
    public int size() {
        return titles.size();
    }
}
