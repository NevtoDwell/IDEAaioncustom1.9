/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author alexa026
 * @author ATracer
 * @author kecimis
 */
public class SM_ATTACK_STATUS extends AionServerPacket {

    private final Creature creature;
    private final TYPE type;
    private final int skillId;
    private final int value;
    private final int logId;

    public static enum TYPE {
        NATURAL_HP(3),
        USED_HP(4),
        // when skill uses hp as cost parameter
        REGULAR(5),
        ABSORBED_HP(6),
        DAMAGE(7),
        HP(7),
        PROTECTDMG(8),
        DELAYDAMAGE(10),
        FALL_DAMAGE(17),
        HEAL_MP(19),
        ABSORBED_MP(20),
        MP(21),
        NATURAL_MP(22),
        FP_RINGS(23),
        FP(25),
        NATURAL_FP(26);

        private final int value;

        private TYPE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static enum LOG {
        SPELLATK(1),
        HEAL(3),
        MPHEAL(4),
        SKILLLATKDRAININSTANT(23),
        SPELLATKDRAININSTANT(24),
        POISON(25),
        BLEED(26),
        PROCATKINSTANT(92),
        DELAYEDSPELLATKINSTANT(95),
        SPELLATKDRAIN(130),
        FPHEAL(133),
        REGULARHEAL(170),
        REGULAR(171);

        private final int value;

        private LOG(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public SM_ATTACK_STATUS(Creature creature, TYPE type, int skillId, int value, LOG log) {
        this.creature = creature;
        this.type = type;
        this.skillId = skillId;
        this.value = value;
        logId = log.getValue();
    }

    public SM_ATTACK_STATUS(Creature creature, TYPE type, int skillId, int value) {
        this(creature, type, skillId, value, LOG.REGULAR);
    }

    public SM_ATTACK_STATUS(Creature creature, int value) {
        this(creature, TYPE.REGULAR, 0, value, LOG.REGULAR);
    }

    /**
     * {@inheritDoc} ddchcc
     */

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(creature.getObjectId());
        switch (type) {
            case DAMAGE:
            case DELAYDAMAGE:
                writeD(-value);
                break;
            default:
                writeD(value);
        }
        writeC(type.getValue());
        writeC(creature.getLifeStats().getHpPercentage());
        writeH(skillId);
        writeH(logId);
    }

    // logId
    // depends on effecttemplate
    // effecttemplate (TYPE) LOG.getValue()
    // spellattack(hp) 1
    // poison(hp) 25
    // delaydamage(hp) 95
    // bleed(hp) 26
    // mp regen(natural_mp) 171
    // hp regen(natural_hp) 171
    // fp regen(natural_fp) 171
    // fp pot(fp) 171
    // prochp(hp) 171
    // procmp(mp) 171
    // heal_instant (regular) 171
    // SpellAtkDrainInstantEffect(absorbed_mp) 24(refactoring shard)
    // mpheal(mp) 4
    // heal(hp) 3
    // fpheal(fp) 133
    // spellatkdrain(hp) 130
    // falldmg (17) 170
    // mpheal (19) 171
    // hp as cost parameter(4) logId 170
    // procatkinstant - (7) 92
    // protecteffect on protector - (8) 171

    // TODO find rest of logIds
}
