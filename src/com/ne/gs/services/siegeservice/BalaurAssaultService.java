/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.configs.main.SiegeConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.assemblednpc.AssembledNpc;
import com.ne.gs.model.assemblednpc.AssembledNpcPart;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.siege.ArtifactLocation;
import com.ne.gs.model.siege.FortressLocation;
import com.ne.gs.model.siege.Influence;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.assemblednpc.AssembledNpcTemplate;
import com.ne.gs.model.templates.assemblednpc.AssembledNpcTemplate.AssembledNpcPartTemplate;
import com.ne.gs.network.aion.serverpackets.SM_NPC_ASSEMBLER;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.SiegeService;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;

public class BalaurAssaultService {

    private static final BalaurAssaultService instance = new BalaurAssaultService();
    private final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
    private final Map<Integer, FortressAssault> fortressAssaults = new FastMap<Integer, FortressAssault>().shared();
    private final List<Integer> excludedLocations = new ArrayList<>();

    public static BalaurAssaultService getInstance() {
        return instance;
    }
    
    private BalaurAssaultService() {
    	if(SiegeConfig.LOCATIONS_WITH_DISABLED_ASSAULT != null) {
    		String[] locations = SiegeConfig.LOCATIONS_WITH_DISABLED_ASSAULT.split(",");
        	if(locations.length == 0)
        		return;
        	for(int i = 0; i < locations.length; i++) {
        		int excludedLocId = Integer.parseInt(locations[i]);
        		excludedLocations.add(excludedLocId);
        	}
    	}
    }

    public void onSiegeStart(Siege<?> siege) {
        if (siege instanceof FortressSiege) {
            if (!calculateFortressAssault(((FortressSiege) siege).getSiegeLocation())) {
                return;
            }
        } else if (siege instanceof ArtifactSiege) {
            if (!calculateArtifactAssault(((ArtifactSiege) siege).getSiegeLocation())) {
                return;
            }
        } else {
            return;
        }
        if(isExcludedLocation(siege.getSiegeLocationId())) {
        	return;
        }
        newAssault(siege, Rnd.get(1, 600));
        if (LoggingConfig.LOG_SIEGE) {
            log.info("[SIEGE] Balaur Assault scheduled on Siege ID: " + siege.getSiegeLocationId() + "!");
        }
    }

    public void onSiegeFinish(Siege<?> siege) {
        int locId = siege.getSiegeLocationId();
        if (fortressAssaults.containsKey(locId)) {
            Boolean bossIsKilled = siege.isBossKilled();
            fortressAssaults.get(locId).finishAssault(bossIsKilled);
            if (bossIsKilled && siege.getSiegeLocation().getRace().equals(SiegeRace.BALAUR)) {
                log.info("[SIEGE] > [FORTRESS:" + siege.getSiegeLocationId() + "] has been captured by Balaur Assault!");
            } else {
                log.info("[SIEGE] > [FORTRESS:" + siege.getSiegeLocationId() + "] Balaur Assault finished without capture!");
            }
            fortressAssaults.remove(locId);
        }
    }

    private boolean calculateFortressAssault(FortressLocation fortress) {
        boolean isBalaurea = fortress.getWorldId() != 400010000;
        int locationId = fortress.getLocationId();

        if (fortressAssaults.containsKey(locationId)) {
            return false;
        }
        if (!calcFortressInfluence(isBalaurea, fortress)) {
            return false;
        }
        int count = 0;
        for (FortressAssault fa : fortressAssaults.values()) {
            if (fa.getWorldId() == fortress.getWorldId()) {
                count++;
            }
        }

        return count < (isBalaurea ? 1 : 2);
    }

    private boolean calculateArtifactAssault(ArtifactLocation artifact) {
        return false;
    }

    public void startAssault(Player player, int location, int delay) {
        if (fortressAssaults.containsKey(location)) {
            player.sendMsg("Assault on " + location + " was already started");
            return;
        }

        newAssault(SiegeService.getInstance().getSiege(location), delay);
    }

    private void newAssault(Siege<?> siege, int delay) {
        if (siege instanceof FortressSiege) {
            FortressAssault assault = new FortressAssault((FortressSiege) siege);
            assault.startAssault(delay);
            fortressAssaults.put(siege.getSiegeLocationId(), assault);
        } else if (siege instanceof ArtifactSiege) {
            ArtifactAssault assault = new ArtifactAssault((ArtifactSiege) siege);
            assault.startAssault(delay);
        }
    }

    private boolean calcFortressInfluence(boolean isBalaurea, FortressLocation fortress) {
        SiegeRace locationRace = fortress.getRace();

        if (locationRace.equals(SiegeRace.BALAUR) || !fortress.isVulnerable()) {
            return false;
        }
        int ownedForts = 0;
        float influence;
        if (isBalaurea) {
            for (FortressLocation fl : SiegeService.getInstance().getFortresses().values()) {
                if (fl.getWorldId() != 400010000 && !fortressAssaults.containsKey(fl.getLocationId()) && fl.getRace().equals(locationRace)) {
                    ownedForts++;
                }
            }
            influence = ownedForts >= 2 ? 0.25F : 0.1F;
        } else {
            influence = locationRace.equals(SiegeRace.ASMODIANS) ? Influence.getInstance().getGlobalAsmodiansInfluence() : Influence.getInstance()
                .getGlobalElyosInfluence();
        }

        influence *= 100.0F;
        influence *= SiegeConfig.BALAUR_ASSAULT_RATE;

        return Rnd.chance(influence);
    }

    public void spawnDredgion(int spawnId) {
        AssembledNpcTemplate template = DataManager.ASSEMBLED_NPC_DATA.getAssembledNpcTemplate(spawnId);
        FastList<AssembledNpcPart> assembledPatrs = new FastList<>();
        for (AssembledNpcPartTemplate npcPart : template.getAssembledNpcPartTemplates()) {
            assembledPatrs.add(new AssembledNpcPart(IDFactory.getInstance().nextId(), npcPart));
        }

        AssembledNpc npc = new AssembledNpc(template.getRouteId(), template.getMapId(), template.getLiveTime(), assembledPatrs);
        Iterator<Player> iter = World.getInstance().getPlayersIterator();

        while (iter.hasNext()) {
            Player findedPlayer = iter.next();
            findedPlayer.sendPck(new SM_NPC_ASSEMBLER(npc));
            findedPlayer.sendPck(SM_SYSTEM_MESSAGE.STR_ABYSS_CARRIER_SPAWN);
        }
    }
    
    /**
     * @param locationId
     * @return true if balaurs assault is disabled for this location.
     */
    public boolean isExcludedLocation(int locationId) {
    	try {
        	if(this.excludedLocations.contains(Integer.valueOf(locationId)))
        		return true;
        	else
        		return false;
    	}
    	catch(Exception e) {
    		log.error("Error while checking locations which excluded from balaurs assault.", e);
    		return false;
    	}
    }
}
