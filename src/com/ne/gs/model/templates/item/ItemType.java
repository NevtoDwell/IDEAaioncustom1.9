/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Wakizashi
 */
@XmlType(name = "item_type")
@XmlEnum
public enum ItemType {
    NORMAL,
    ABYSS,
    DRACONIC,
    DEVANION,
    LEGEND;
}
