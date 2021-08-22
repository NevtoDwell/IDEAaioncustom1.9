/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.Set;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.Announcement;

/**
 * DAO that manages Announcements
 *
 * @author Divinity
 */
public abstract class AnnouncementsDAO implements DAO {

    public abstract Set<Announcement> getAnnouncements();

    public abstract void addAnnouncement(Announcement announce);

    public abstract boolean delAnnouncement(int idAnnounce);

    /**
     * Returns class name that will be uses as unique identifier for all DAO classes
     *
     * @return class name
     */
    @Override
    public final String getClassName() {
        return AnnouncementsDAO.class.getName();
    }
}
