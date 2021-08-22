/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrates;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.google.common.collect.Lists;
import gnu.trove.map.hash.THashMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.DateUtil;
import com.ne.commons.configuration.ConfigurableProcessor;
import com.ne.commons.services.CronService;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.commons.utils.Callback;
import com.ne.commons.utils.PropertiesUtils;
import com.ne.commons.utils.xml.XmlUtil;
import com.ne.gs.configs.main.RateConfig;

/**
 * @author Jenelli
 * @date 26.04.13
 * @time 22:31
 */
public class CustomRateManager extends Actor {

    private static final ActorRef _instance = ActorRef.of(new CustomRateManager());

    private static final Logger _log = LoggerFactory.getLogger(CustomRateManager.class);

    private final Map<Integer, RateLoad> _loadRates = new THashMap<>();
    //private final Set<Integer> _loadRateIds = new THashSet<>();
    private final List<JobDetail> _jobs = Lists.newArrayList();

    private CustomRateManager() {
    }

    public static ActorRef getInstance() {
        return _instance;
    }

    private void init() throws Exception {
        RateLoadList list;
        String loadFile = "./config/custom_data/custom_rates/rate_config_loads.xml";
        try {
            list = XmlUtil.loadXmlJAXB(RateLoadList.class, loadFile);
        } catch (FileNotFoundException e) {
            _log.info("File for loading not found: " + loadFile);
            return;
        }

        _loadRates.clear();
        //_loadRateIds.clear();
        try {
            loadDefaultRate();
        } catch (Exception e) {
            _log.error("Can't load default configuration: ", e);
            return;
        }

        for (JobDetail jobDetail : _jobs) {
            CronService.getInstance().cancel(jobDetail);
        }

        for (RateLoad rateLoad : list) {
            _loadRates.put(rateLoad.getId(), rateLoad);
        }

        for (final RateLoad rateLoad : list) {
            if (rateLoad.getTime() == null) {
                _log.info(rateLoad.getId() + "Rate load time is null");
            } else {
                DateUtil.CronExpr from = rateLoad.getTime().getFrom();
                DateUtil.CronExpr to = rateLoad.getTime().getTo();

                if (DateUtil.cronBetween(from, to)) {
                    loadRate(rateLoad);
                } else {
                    CronService.ScheduleResult sr = CronService.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            getInstance().tell(new Msg() {
                                @Override
                                public void run() {
                                    actor().loadRate(rateLoad.getId());
                                }
                            });
                        }
                    }, from.toString());

                    _jobs.add(sr.getJobDetail());
                }
            }
        }
    }

    private void loadRate(Integer uid) {
        RateLoad rateLoad = _loadRates.get(uid);
        if (rateLoad != null) {
            loadRate(rateLoad);
        }
    }

    private void loadRate(final RateLoad rateLoad) {
        try {
            //_loadRateIds.add(rateLoad.getId());
            _log.info("Load rates from " + rateLoad.getId());
            loadRates("./config/custom_data/custom_rates/" + rateLoad.getFile());

            if (rateLoad.getTime() != null) {
                CronService.ScheduleResult sr = CronService.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        getInstance().tell(new Msg() {
                            @Override
                            public void run() {
                                try {
                                    actor().loadDefaultRate(rateLoad.getId());
                                } catch (Exception e) {
                                    _log.error("Can't load default configuration: " + rateLoad.getId(), e);
                                }
                            }
                        });
                    }
                }, rateLoad.getTime().getTo().toString());

                 _jobs.add(sr.getJobDetail());
            }
        } catch (Exception e) {
            _log.error("Can't load configuration: " + rateLoad.getId(), e);
        }
    }

    private void loadRates(String fileName) throws IOException {
        Properties adminProps = PropertiesUtils.load(fileName);
        ConfigurableProcessor.process(RateConfig.class, adminProps);
        _log.info("Load rates from file " + fileName);
    }

    private void loadDefaultRate(Integer id) throws IOException {
        //_loadRateIds.remove(id);
        _log.info("Cancel rate " + id);
        loadDefaultRate();
    }

    private void loadDefaultRate() throws IOException {
        String fileName = "./config/main/rates.properties";
        loadRates(fileName);
    }

    private static abstract class Msg extends Message<CustomRateManager> {}

    public static class Init extends Msg {

        private static final String LOADING = "CustomRateManager: Loading custom rates";
        private static final String SUCCESS = "CustomRateManager: Custom rates loaded successfully";
        private static final String FAILURE = "CustomRateManager: Error while loading custom rates";

        private final Callback _callback;

        public Init() {
            this(Callback.DUMMY);
        }

        public Init(Callback callback) {
            _callback = callback;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            _log.info(LOADING);
            _callback.onEvent(LOADING);
            try {
                actor().init();
                _log.info(SUCCESS);
                _callback.onEvent(SUCCESS);
            } catch (Exception e) {
                _log.error(FAILURE, e);
                _callback.onEvent(FAILURE + " " + e.getMessage());
            }
        }
    }
}
