/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.assemblednpc;

import com.ne.gs.model.templates.assemblednpc.AssembledNpcTemplate.AssembledNpcPartTemplate;

/**
 * @author xTz
 */
public class AssembledNpcPart {

    private final Integer object;
    private final AssembledNpcPartTemplate template;

    public AssembledNpcPart(Integer object, AssembledNpcPartTemplate template) {
        this.object = object;
        this.template = template;
    }

    public Integer getObject() {
        return object;
    }

    public AssembledNpcPartTemplate getAssembledNpcPartTemplate() {
        return template;
    }

    public int getNpcId() {
        return template.getNpcId();
    }

    public int getStaticId() {
        return template.getStaticId();
    }
}
