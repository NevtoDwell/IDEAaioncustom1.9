package com.ne.gs.modules.pvpevent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.commons.annotations.NotNull;
import com.ne.gs.modules.common.CustomLocTemplate;
import com.ne.gs.modules.common.PosList;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "PvpLocTemplate")
public class PvpLocTemplate extends CustomLocTemplate {

    @XmlElement(name = "player_cmd", required = true)
    protected PlayerCmd _playerCmd = new PlayerCmd();

    @XmlElement(name = "start_positions", required = true)
    protected List<PosList> _startPositions = new ArrayList<>(0);

    @XmlElement(name = "rewards")
    protected List<PvpRewardList> _rewards = new ArrayList<>(0);

    @XmlElement(name = "penalties")
    protected PenaltyList _penalties = new PenaltyList();

    public PlayerCmd getPlayerCmd() {
        return _playerCmd;
    }

    public void setPlayerCmd(PlayerCmd playerCmd) {
        _playerCmd = playerCmd;
    }

    @NotNull
    public List<PosList> getStartPositions() {
        return _startPositions;
    }

    public void setStartPositions(@NotNull List<PosList> startPositions) {
        _startPositions = startPositions;
    }

    public List<PvpRewardList> getRewards() {
        return _rewards;
    }

    public void setRewards(List<PvpRewardList> rewards) {
        _rewards = rewards;
    }

    public PenaltyList getPenalties() {
        return _penalties;
    }

    public void setPenalties(PenaltyList penalties) {
        _penalties = penalties;
    }
}
