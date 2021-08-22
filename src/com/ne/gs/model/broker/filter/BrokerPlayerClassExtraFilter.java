/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.broker.filter;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class BrokerPlayerClassExtraFilter extends BrokerPlayerClassFilter {

    private final int mask;

    /**
     * @param playerClass
     */
    public BrokerPlayerClassExtraFilter(int mask, PlayerClass playerClass) {
        super(playerClass);
        this.mask = mask;
    }

    @Override
    public boolean accept(ItemTemplate template) {
        return super.accept(template) && mask == template.getTemplateId() / 100000;
    }

}
