/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.broker.filter;

import org.apache.commons.lang3.ArrayUtils;

import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerContainsExtraFilter extends BrokerFilter {

    private final int[] masks;

    /**
     * @param masks
     */
    public BrokerContainsExtraFilter(int... masks) {
        this.masks = masks;
    }

    @Override
    public boolean accept(ItemTemplate template) {
        return ArrayUtils.contains(masks, template.getTemplateId() / 10000);
    }

}
