/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import javolution.util.FastList;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.templates.survey.SurveyItem;

/**
 * @author KID
 */
public abstract class SurveyControllerDAO implements DAO {

    @Override
    public final String getClassName() {
        return SurveyControllerDAO.class.getName();
    }

    public abstract boolean useItem(int id);

    public abstract FastList<SurveyItem> getAllNew();
}
