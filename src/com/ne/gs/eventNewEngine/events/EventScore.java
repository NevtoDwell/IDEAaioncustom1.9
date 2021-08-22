/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events;

/**
 *
 * @author userd
 */
public class EventScore {

    public final int PlayerObjectId;
    public int Kills;
    public int Death;
    public int Wins;
    public int Loses;
    public boolean isWinner = false;

    public EventScore(int id) {
        this.PlayerObjectId = id;
        Kills = Death = Wins = Loses = 0;
    }
}
