/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.mapskillrestrictor;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.Chainer;
import com.ne.commons.utils.EventNotifier;
import com.ne.commons.utils.xml.XmlUtil;
import com.ne.gs.configs.modules.MapSkillRestrictorConfig;
import com.ne.gs.controllers.effect.PlayerEffectController;
import com.ne.gs.model.conds.SkillUseCond;
import com.ne.gs.model.events.PlayerEnteredMap;
import com.ne.gs.model.events.PlayerLeftMap;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.handlers.CmdReloadHandler;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author hex1r0
 */
public final class MapSkillRestrictor {
    private static final TIntObjectHashMap<TIntSet> _maps = new TIntObjectHashMap<>(0);

    public static void init() {
        if (!MapSkillRestrictorConfig.MAP_SKILL_RESTRICTOR_ENABLED) {
            return;
        }

        reload();

        EventNotifier.GLOBAL.attach(new Enter());
        EventNotifier.GLOBAL.attach(new Leave());

        Chainer.GLOBAL.attach(new Reload());

    }

    private static void reload() {
        Document doc = XmlUtil.loadXmlSAX("./config/custom_data/map_skill_restrictor.xml");
        if (doc == null) { return; }

        _maps.clear();
        for (Node maps : XmlUtil.nodesByName(doc, "maps")) {
            for (Node map : XmlUtil.nodesByName(maps, "map")) {
                int mapId = Integer.parseInt(XmlUtil.getAttribute(map, "id"));
                for (Node skill : XmlUtil.nodesByName(map, "skill")) {
                    int skillId = Integer.parseInt(XmlUtil.getAttribute(skill, "id"));
                    TIntSet set = _maps.get(mapId);
                    if (set == null) {
                        set = new TIntHashSet(0);
                        _maps.put(mapId, set);
                    }
                    set.add(skillId);
                }
            }
        }
    }

    private static class Enter extends PlayerEnteredMap {
        @Override
        public Object onEvent(@NotNull Tuple2<Player, Integer> e) {
            if (!MapSkillRestrictorConfig.MAP_SKILL_RESTRICTOR_ENABLED) { return null; }

            TIntSet set = _maps.get(e._2);
            if (set != null) {
                e._1.getConditioner().attach(Cond.STATIC);
                final PlayerEffectController ec = e._1.getEffectController();
                set.forEach(new TIntProcedure() {
                    @Override
                    public boolean execute(int skillId) {
                        ec.removeEffect(skillId);
                        return true;
                    }
                });
            }

            return null;
        }
    }

    private static class Leave extends PlayerLeftMap {
        @Override
        public Object onEvent(@NotNull Tuple2<Player, Integer> e) {
            TIntSet set = _maps.get(e._2);
            if (set != null) {
                e._1.getConditioner().detach(Cond.STATIC);
            }
            return null;
        }
    }

    private static class Cond extends SkillUseCond {
        public static final Cond STATIC = new Cond();

        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Skill> e) {
            if (!MapSkillRestrictorConfig.MAP_SKILL_RESTRICTOR_ENABLED) {
                return SkillUseCond.STATIC.onEvent(e); // perform default checks
            }

            TIntSet set = _maps.get(e._1.getWorldId());
            if (set == null) {
                return SkillUseCond.STATIC.onEvent(e); // perform default checks
            }

            boolean denied = set.contains(e._2.getSkillId());
            if (denied) {
                PacketSendUtility.sendYellowMessageOnCenter(e._1, "Это умение нельзя использовать в данной локации!");
                return false;
            }

            return SkillUseCond.STATIC.onEvent(e); // perform default checks
        }
    }

    private static class Reload extends CmdReloadHandler {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, String[]> e) {
            if (!MapSkillRestrictorConfig.MAP_SKILL_RESTRICTOR_ENABLED) {
                return false; // continue
            }

            if (e._2.length > 0 && e._2[0].equalsIgnoreCase("mapskillrestrictor")) {
                MapSkillRestrictor.reload();
                e._1.sendMsg("MapSkillRestrictor: reloaded");
                return true; // break
            }
            return false; // continue
        }
    }
}
