package com.ne.gs.taskmanager.tasks;

import com.ne.gs.ShutdownHook;
import com.ne.gs.ShutdownHook.ShutdownMode;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.tasks.TaskFromDBHandler;
import com.ne.gs.services.SiegeService;
import com.ne.gs.services.SiegeService.SiegeBoss;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jenelli
 * @date 26.05.13
 * @time 0:30
 */
public class SpawnTask extends TaskFromDBHandler {
    private static final Logger log = LoggerFactory.getLogger(SpawnTask.class);

    private int npcId;
    private float x;
    private float y;
    private float z;
    private byte h;
    private int worldId;
    private int instanceIndex;

    @Override
    public String getTaskName() {
        return "spawn";
    }

    @Override
    public boolean isValid() {
        return params.length == 1 || params.length == 7;

    }

    @Override
    public void run() {
        String logString = "Task[" + id + "]. ";
        log.info(logString + "Spawn npc started.");
        setLastActivation();

        try {
            npcId = Integer.parseInt(params[0]);
        } catch (Exception e){
            log.error(logString + "Can't parsing parameters!", e);
            return;
        }

        if (SiegeBoss.GOVERNOR_SUNAYAKA_218553 == npcId) {
            SiegeService.getInstance().spawnTiamarantaBoss();
            log.info(logString + "Spawned npc 218553.");
            return;
        }

        if (params.length != 7) {
            log.warn(logString + "Can't spawn npc " + npcId);
        }

        try {
            worldId = Integer.parseInt(params[1]);
            x = Float.parseFloat(params[2]);
            y = Float.parseFloat(params[3]);
            z = Float.parseFloat(params[4]);
            h = Byte.parseByte(params[5]);
            instanceIndex = Integer.parseInt(params[6]);
        } catch (Exception e){
            log.error(logString + "Can't parsing parameters!", e);
            return;
        }

        SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, h), instanceIndex);
        log.info(logString + "Spawned npc " + npcId);
    }
}
