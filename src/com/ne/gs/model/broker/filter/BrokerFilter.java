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
public abstract class BrokerFilter {

    /**
     * @param template
     *
     * @return
     */
    public abstract boolean accept(ItemTemplate template);
}
