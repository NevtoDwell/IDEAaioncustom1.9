/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ne.commons.DateUtil;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Time")
public class Time {

    @XmlElement(name = "from", required = true)
    @XmlJavaTypeAdapter(CronExpressionAdapter.class)
    protected DateUtil.CronExpr _from;

    @XmlJavaTypeAdapter(CronExpressionAdapter.class)
    @XmlElement(name = "to", required = true)
    protected DateUtil.CronExpr _to;

    public DateUtil.CronExpr getFrom() {
        return _from;
    }

    public void setFrom(DateUtil.CronExpr value) {
        _from = value;
    }

    public DateUtil.CronExpr getTo() {
        return _to;
    }

    public void setTo(DateUtil.CronExpr value) {
        _to = value;
    }

    public static class CronExpressionAdapter extends XmlAdapter<String, DateUtil.CronExpr> {

        @Override
        public DateUtil.CronExpr unmarshal(String v) throws Exception {
            return new DateUtil.CronExpr(v);
        }

        @Override
        public String marshal(DateUtil.CronExpr v) throws Exception {
            return v.toString();
        }

    }

}
