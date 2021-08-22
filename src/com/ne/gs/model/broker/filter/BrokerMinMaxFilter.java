/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.broker.filter;

import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerMinMaxFilter extends BrokerFilter {

    private final int min;
    private final int max;

    /**
     * @param min
     * @param max
     */
    public BrokerMinMaxFilter(int min, int max) {
        this.min = min * 100000;
        this.max = max * 100000;
    }

    @Override
    public boolean accept(ItemTemplate template) {
        return template.getTemplateId() >= min && template.getTemplateId() < max;
    }

}
