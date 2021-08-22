/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.service;

import com.google.common.base.Preconditions;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TeamMember;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.alliance.events.AssignViceCaptainEvent.AssignType;
import com.ne.gs.model.team2.common.events.TeamCommand;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.model.team2.league.LeagueMember;
import com.ne.gs.model.team2.league.LeagueService;
import com.ne.gs.utils.PacketSendUtility;


/**
 * @author ATracer
 */
public final class PlayerTeamCommandService {

    public static void executeCommand(Player player, TeamCommand command, int playerObjId) {
        Player teamSubjective = getTeamSubjective(player, playerObjId);
        // if playerObjId is not 0 - subjective should not be active player
        Preconditions.checkArgument(playerObjId == 0 || teamSubjective.getObjectId().equals(playerObjId) || command == TeamCommand.LEAGUE_EXPEL,
            "Wrong command detected " + command);
        execute(player, command, teamSubjective);
    }

    private static void execute(Player player, TeamCommand eventCode, Player teamSubjective) {
        switch (eventCode) {
            case GROUP_BAN_MEMBER:
                PlayerGroupService.banPlayer(teamSubjective, player);
                break;
            case GROUP_SET_LEADER:
                PlayerGroupService.changeLeader(teamSubjective);
                break;
            case GROUP_REMOVE_MEMBER:
                PlayerGroupService.removePlayer(teamSubjective);
                break;
            case GROUP_START_MENTORING:
                //PacketSendUtility.sendYellowMessageOnCenter(player, "Наставничество недоступно в данной версии. | Mentoring is not available in this release.");
                PlayerGroupService.startMentoring(player);
                break;
            case GROUP_END_MENTORING:
                PlayerGroupService.stopMentoring(player);
                break;
            case ALLIANCE_LEAVE:
                PlayerAllianceService.removePlayer(player);
                break;
            case ALLIANCE_BAN_MEMBER:
                PlayerAllianceService.banPlayer(teamSubjective, player);
                break;
            case ALLIANCE_SET_CAPTAIN:
                PlayerAllianceService.changeLeader(teamSubjective);
                break;
            case ALLIANCE_CHECKREADY_CANCEL:
            case ALLIANCE_CHECKREADY_START:
            case ALLIANCE_CHECKREADY_AUTOCANCEL:
            case ALLIANCE_CHECKREADY_NOTREADY:
            case ALLIANCE_CHECKREADY_READY:
                PlayerAllianceService.checkReady(player, eventCode);
                break;
            case ALLIANCE_SET_VICECAPTAIN:
                PlayerAllianceService.changeViceCaptain(teamSubjective, AssignType.PROMOTE);
                break;
            case ALLIANCE_UNSET_VICECAPTAIN:
                PlayerAllianceService.changeViceCaptain(teamSubjective, AssignType.DEMOTE);
                break;
            case LEAGUE_LEAVE:
                LeagueService.removeAlliance(player.getPlayerAlliance2());
                break;
            case LEAGUE_EXPEL:
                LeagueService.expelAlliance(teamSubjective, player);
                break;
        }
    }

    private static Player getTeamSubjective(Player player, int playerObjId) {
        if (playerObjId == 0) {
            return player;
        }
        if (player.isInTeam()) {
            TeamMember<Player> member = player.getCurrentTeam().getMember(playerObjId);
            if (member != null) {
                return member.getObject();
            }
            if (player.isInLeague()) {
                LeagueMember subjective = player.getPlayerAlliance2().getLeague().getMember(playerObjId);
                if (subjective != null) {
                    return subjective.getObject().getLeaderObject();
                }
            }
        }
        return player;
    }
}
