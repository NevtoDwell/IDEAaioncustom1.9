package com.ne.gs.modules.pvpevent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.commons.annotations.NotNull;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PvpRewardList")
public class PvpRewardList {

    @XmlAttribute(name = "group", required = true)
    private String _group;

    @XmlAttribute(name = "rank")
    private int _rank = 1;

    @XmlAttribute(name = "ap")
    private int _ap;

    @XmlAttribute(name = "lvl")
    private int _lvl;

    @XmlAttribute(name = "gp")
    private int _gp;

    @XmlElement(name = "random_rewards")
    private PvpItemList _randomRewardList = new PvpItemList();

    @XmlElement(name = "selective_rewards")
    private PvpItemList _selectiveRewardList = new PvpItemList();

    public String getGroup() {
        return _group;
    }

    public void setGroup(String group) {
        _group = group;
    }

    public int getAp() {
        return _ap;
    }

    public void setAp(int ap) {
        _ap = ap;
    }

    public int getGp() {
        return _gp;
    }

    public void setGp(int gp) {
        _gp = gp;
    }

    public int getLvl() {
        return _lvl;
    }

    public void setLvl(int lvl) {
        _lvl = lvl;
    }

    public int getRank() {
        return _rank;
    }

    public void setRank(int rank) {
        _rank = rank;
    }

    @NotNull
    public PvpItemList getRandomRewardList() {
        return _randomRewardList;
    }

    public void setRandomRewardList(@NotNull PvpItemList randomRewardList) {
        _randomRewardList = randomRewardList;
    }

    @NotNull
    public PvpItemList getSelectiveRewardList() {
        return _selectiveRewardList;
    }

    public void setSelectiveRewardList(@NotNull PvpItemList selectiveRewardList) {
        _selectiveRewardList = selectiveRewardList;
    }

}
