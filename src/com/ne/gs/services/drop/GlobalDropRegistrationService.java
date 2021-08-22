/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.drop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import gnu.trove.procedure.TObjectProcedure;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.GlobalDropData;
import com.ne.gs.model.drop.Drop;
import com.ne.gs.model.drop.DropGroup;
import com.ne.gs.model.drop.GlobalDrop;
import com.ne.gs.model.drop.NpcDrop;
import com.ne.gs.model.templates.npc.NpcTemplate;

/**
 * @author Kolobrodik
 */
public class GlobalDropRegistrationService {

    public GlobalDropRegistrationService() {
        init();
    }

    public final void init() {
        GlobalDropData globalDrop = DataManager.GLOBAL_DROP_DATA;
        for (final GlobalDrop drop : globalDrop.getGlobalDrop()) {
            DataManager.NPC_DATA.getNpcData().forEachValue(new TObjectProcedure<NpcTemplate>() {
                @Override
                public boolean execute(NpcTemplate npcTemplate) {
                    if (drop.getLevel() != npcTemplate.getLevel()) {
                        return true;
                    }
                    if (drop.getNpcRace() != npcTemplate.getRace()) {
                        return true;
                    }
                    if (drop.getNpcRating() != npcTemplate.getRating()) {
                        return true;
                    }

                    if (npcTemplate.getNpcDrop() != null) {
                        NpcDrop currentDrop = npcTemplate.getNpcDrop();
                        for (DropGroup dg : currentDrop.getDropGroup()) {
                            Iterator<Drop> iterator = dg.getDrop().iterator();
                            while (iterator.hasNext()) {
                                Drop d = iterator.next();
                                for (DropGroup dg2 : drop.getDropGroup()) {
                                    if (dg2.getRace() != dg.getRace()) {
                                        continue;
                                    }
                                    if (dg2.isUseCategory() != dg.isUseCategory()) {
                                        continue;
                                    }
                                    for (Drop d2 : dg2.getDrop()) {
                                        if (d.getItemId() == d2.getItemId()) {
                                            iterator.remove();
                                        }
                                    }
                                }
                            }
                        }
                        List<DropGroup> list = new ArrayList<>();
                        for (DropGroup dg : drop.getDropGroup()) {
                            boolean added = false;
                            for (DropGroup dg2 : currentDrop.getDropGroup()) {
                                if (dg2.getGroupName().equals(dg.getGroupName())) {
                                    dg2.getDrop().addAll(dg.getDrop());
                                    added = true;
                                }
                            }
                            if (!added) {
                                list.add(dg);
                            }
                        }
                        if (!list.isEmpty()) {
                            currentDrop.getDropGroup().addAll(list);
                        }
                    } else {
                        NpcDrop npcDrop = new NpcDrop();
                        npcDrop.setDropGroup(drop.getDropGroup());
                        npcDrop.setNpcId(npcTemplate.getTemplateId());
                        npcTemplate.setNpcDrop(npcDrop);
                    }
                    return true;
                }
            });
        }
    }

    public static GlobalDropRegistrationService getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final GlobalDropRegistrationService instance = new GlobalDropRegistrationService();
    }
}
