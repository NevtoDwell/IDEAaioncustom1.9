/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.services;

import com.ne.gs.model.DuelResult;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.network.aion.serverpackets.SM_DUEL;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.summons.SummonsService;
import com.ne.gs.skillengine.model.SkillTargetSlot;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.zone.ZoneInstance;
import com.ne.gs.world.zone.ZoneName;
import java.util.concurrent.Future;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author userd
 */
public class DuelService {

    private static Logger log = LoggerFactory.getLogger(DuelService.class);
    private FastMap<Integer, Integer> duels;
    private FastMap<Integer, Future<?>> drawTasks;

    public static final DuelService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * @param duels
     */
    private DuelService() {
        this.duels = new FastMap<Integer, Integer>().shared();
        this.drawTasks = new FastMap<Integer, Future<?>>().shared();
        log.info("DuelService started.");
    }

    /**
     * Send the duel request to the owner
     *
     * @param requester the player who requested the duel
     * @param responder the player who respond to duel request
     */
    public void onDuelRequest(Player requester, Player responder) {
        if (isDueling(requester.getObjectId()) || isDueling(responder.getObjectId())) {
            PacketSendUtility.sendPck(requester, SM_SYSTEM_MESSAGE.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
            return;
        }
        RequestResponseHandler rrh = new RequestResponseHandler(requester) {

            @Override
            public void denyRequest(Creature requester, Player responder) {
                rejectDuelRequest((Player) requester, responder);
            }

            @Override
            public void acceptRequest(Creature requester, Player responder) {
                Player player = (Player) requester;
                Future<?> task = drawTasks.get(player.getObjectId());
                if (task != null && !task.isDone()) {
                }
                else if (isDueling(requester.getObjectId()) || isDueling(responder.getObjectId())) {
                }
                else{
                    startDuel(player, responder);
                }                         
            }
        };
        responder.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, rrh);
        PacketSendUtility.sendPck(responder, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, 0, 0, requester.getName()));
        PacketSendUtility.sendPck(responder, SM_SYSTEM_MESSAGE.STR_DUEL_REQUESTED(requester.getName()));
        PacketSendUtility.sendPck(requester, SM_SYSTEM_MESSAGE.STR_DUEL_REQUEST_TO_PARTNER(responder.getName()));
    }

    /**
     * Asks confirmation for the duel request
     *
     * @param requester the player whose the duel was requested
     * @param responder the player whose the duel was responded
     */
    public void confirmDuelWith(Player requester, Player responder) { 
        /**
         * Check if requester isn't already in a duel and responder is same race
         */
        /*if (requester.isEnemy(responder)) {
         return;
         }*/

        /**
         * Check if requester isn't already in a duel and responder is same race
         */

        if (isDueling(requester.getObjectId()) || isDueling(responder.getObjectId())) {
            PacketSendUtility.sendPck(requester, SM_SYSTEM_MESSAGE.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
            return;
        }
        
        for (ZoneInstance zone : responder.getPosition().getMapRegion().getZones((Creature) responder)) {
            if (!zone.isOtherRaceDuelsAllowed() && !responder.getRace().equals(requester.getRace())
	      || (!zone.isSameRaceDuelsAllowed() && responder.getRace().equals(requester.getRace()))) {
                PacketSendUtility.sendPck(requester, SM_SYSTEM_MESSAGE.STR_MSG_DUEL_CANT_IN_THIS_ZONE);
                return;
            }
        }
        
        if(requester.isInsideZone(ZoneName.get("LC1_PVP_SUB_C")) ||   //фикс команды /дуэль в на дуэль арене элиз/панда
           requester.isInsideZone(ZoneName.get("DC1_PVP_ZONE")))     
        {
            PacketSendUtility.sendPck(requester, SM_SYSTEM_MESSAGE.STR_MSG_DUEL_CANT_IN_THIS_ZONE);
            return;
        }           
        

        
        

        RequestResponseHandler rrh = new RequestResponseHandler(responder) {

            @Override
            public void denyRequest(Creature requester, Player responder) {
                Player player = (Player) requester;
                Future<?> task = drawTasks.get(player.getObjectId());
                if (task != null && !task.isDone()) {
                }else{
                    onDuelRequest(responder, (Player) requester);
                }          
            }

            @Override
            public void acceptRequest(Creature requester, Player responder) {
                cancelDuelRequest(responder, (Player) requester);
            }
        };
        requester.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, rrh);
        PacketSendUtility.sendPck(requester, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, 0, 0, responder.getName()));
    }

    /**
     * Rejects the duel request
     *
     * @param requester the duel requester
     * @param responder the duel responder
     */
    private void rejectDuelRequest(Player requester, Player responder) {
        log.debug("[Duel] Player " + responder.getName() + " rejected duel request from " + requester.getName());
        PacketSendUtility.sendPck(requester, SM_SYSTEM_MESSAGE.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
        PacketSendUtility.sendPck(responder, SM_SYSTEM_MESSAGE.STR_DUEL_REJECT_DUEL(requester.getName()));
    }

    /**
     * Cancels the duel request
     *
     * @param target the duel target
     * @param requester
     */
    private void cancelDuelRequest(Player owner, Player target) {
        // log.debug("[Duel] Player " + owner.getName() + " cancelled his duel request with " + target.getName());
        PacketSendUtility.sendPck(target, SM_SYSTEM_MESSAGE.STR_DUEL_REQUESTER_WITHDRAW_REQUEST(owner.getName()));
        PacketSendUtility.sendPck(owner, SM_SYSTEM_MESSAGE.STR_DUEL_WITHDRAW_REQUEST(target.getName()));
    }

    /**
     * Starts the duel
     *
     * @param requester the player to start duel with
     * @param responder the other player
     */
    
    private void startDuel(Player requester, Player responder) {
    PacketSendUtility.sendPck(requester, SM_DUEL.SM_DUEL_STARTED(responder.getObjectId()));
    PacketSendUtility.sendPck(responder, SM_DUEL.SM_DUEL_STARTED(requester.getObjectId()));
    createTask(requester, responder);
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {              
                createDuel(requester.getObjectId(), responder.getObjectId());
            }
        }, 3000);       
    }

    /**
     * This method will make the selected player lose the duel
     *
     * @param player
     */
    public void loseDuel(Player player) {
        if (!isDueling(player.getObjectId())) {
            return;
        }
        int opponnentId = duels.get(player.getObjectId());

        // player.getAggroList().clear();
        Player opponent = World.getInstance().findPlayer(opponnentId);

        if (opponent != null) {
            /**
             * all debuffs are removed from winner, but buffs will remain Stop
             * casting or skill use
             */
            opponent.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
            opponent.getController().cancelCurrentSkill();
            // opponent.getAggroList().clear();

            /**
             * cancel attacking winner by summon
             */
            if (player.getSummon() != null) {
                //if (player.getSummon().getTarget().isTargeting(opponnentId))
                SummonsService.doMode(SummonMode.GUARD, player.getSummon(), UnsummonType.UNSPECIFIED);
            }

            /**
             * cancel attacking loser by summon
             */
            if (opponent.getSummon() != null) {
                //if (opponent.getSummon().getTarget().isTargeting(player.getObjectId()))
                SummonsService.doMode(SummonMode.GUARD, opponent.getSummon(), UnsummonType.UNSPECIFIED);
            }

            /**
             * cancel attacking winner by summoned object
             */
            if (player.getSummonedObj() != null) {
                player.getSummonedObj().getController().cancelCurrentSkill();
            }

            /**
             * cancel attacking loser by summoned object
             */
            if (opponent.getSummonedObj() != null) {
                opponent.getSummonedObj().getController().cancelCurrentSkill();
            }

            PacketSendUtility.sendPck(opponent, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_WON, player.getName()));
            PacketSendUtility.sendPck(player, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_LOST, opponent.getName()));
        } else {
            log.warn("CHECKPOINT : duel opponent is already out of world");
        }

        removeDuel(player.getObjectId(), opponnentId);
    }

    public void loseArenaDuel(Player player) {
        if (!isDueling(player.getObjectId())) {
            return;
        }

        /**
         * all debuffs are removed from loser Stop casting or skill use
         */
        player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
        player.getController().cancelCurrentSkill();

        int opponnentId = duels.get(player.getObjectId());
        Player opponent = World.getInstance().findPlayer(opponnentId);

        if (opponent != null) {
            /**
             * all debuffs are removed from winner, but buffs will remain Stop
             * casting or skill use
             */
            opponent.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
            opponent.getController().cancelCurrentSkill();
        } else {
            log.warn("CHECKPOINT : duel opponent is already out of world");
        }

        removeDuel(player.getObjectId(), opponnentId);
    }

    private void createTask(final Player requester, final Player responder) {
        // Schedule for draw
        Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (isDueling(requester.getObjectId(), responder.getObjectId())) {
                    PacketSendUtility.sendPck(requester, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_DRAW, requester.getName()));
                    PacketSendUtility.sendPck(responder, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_DRAW, responder.getName()));
                    removeDuel(requester.getObjectId(), responder.getObjectId());               
                }
            }
        }, 302900); // 5 minutes battle retail like

        drawTasks.put(requester.getObjectId(), task);
        drawTasks.put(responder.getObjectId(), task);
        
        //timer duel - 5 minutes
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {              
                PacketSendUtility.sendPck(requester, new SM_QUEST_ACTION(4, ((300 / 100) + 2) * 60));
                PacketSendUtility.sendPck(responder, new SM_QUEST_ACTION(4, ((300 / 100) + 2) * 60));
            }
        }, 2900);    
        
    }

    /**
     * @param playerObjId
     * @return true of player is dueling
     */
    public boolean isDueling(int playerObjId) {
        return (duels.containsKey(playerObjId) && duels.containsValue(playerObjId));
    }

    /**
     * @param playerObjId
     * @param targetObjId
     * @return true of player is dueling
     */
    public boolean isDueling(int playerObjId, int targetObjId) {
        return duels.containsKey(playerObjId) && duels.get(playerObjId) == targetObjId;
    }

    /**
     * @param requesterObjId
     * @param responderObjId
     */
    public void createDuel(int requesterObjId, int responderObjId) {
        duels.put(requesterObjId, responderObjId);
        duels.put(responderObjId, requesterObjId);
    }

    /**
     * @param requesterObjId
     * @param responderObjId
     */
    private void removeDuel(int requesterObjId, int responderObjId) {
        duels.remove(requesterObjId);
        duels.remove(responderObjId);
        removeTask(requesterObjId);
        removeTask(responderObjId);
        //remove timer duel
        Player requester = World.getInstance().findPlayer(requesterObjId);
        Player responder = World.getInstance().findPlayer(responderObjId);
        PacketSendUtility.sendPck(requester, new SM_QUEST_ACTION(4, (0)));
        PacketSendUtility.sendPck(responder, new SM_QUEST_ACTION(4, (0)));
    }

    private void removeTask(int playerId) {
        Future<?> task = drawTasks.get(playerId);
        if (task != null && !task.isDone()) {
            task.cancel(true);
            drawTasks.remove(playerId);
        }
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final DuelService instance = new DuelService();
    }

}

