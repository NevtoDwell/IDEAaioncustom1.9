package mw.processors.movement.motor;

import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.controllers.movement.MovementMask;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.stats.calc.Stat2;
import mw.engines.geo.math.Vector3f;
import mw.processors.movement.MovementProcessor;

/**
 * Base abstraction of all movement motors
 * @author MetaWind
 */
public abstract class AMovementMotor {

    /* Motor owner */
    final Creature _owner;

    /* Parent movement processor */
    final MovementProcessor _processor;

    /* Current movement target position */
    Vector3f _targetPosition;

    /* Current movement heading */
    byte _targetHeading;

    /* Current movement mask */
    byte _targetMask;

    /**
     * Base constructor
     * @param owner Creature owner
     * @param processor Parent movement processor
     */
    AMovementMotor(Creature owner, MovementProcessor processor){
        _owner = owner;
        _processor = processor;
    }

    /* Start this motor */
    public abstract void start();

    /* Stop this motor */
    public abstract void stop();

    /**
     * @return Current movement target coordinates
     */
    public Vector3f getCurrentTarget() {
        return _targetPosition;
    }

    /**
     * @return Current movement mask
     */
    public byte getMovementMask(){
        return  _targetMask;
    }

    /**
     * Recalculate current movement heading and movement mask
     * @return
     */
    void recalculateMovementParams(){

        byte oldHeading = (byte) _owner.getHeading();
        _targetHeading = (byte) (Math.toDegrees(Math.atan2(
                _targetPosition.getY() - _owner.getY(),
                _targetPosition.getX() - _owner.getX())) / 3);

        _targetMask = MovementMask.IMMEDIATE;
        if (oldHeading != _targetHeading)
            _targetMask |= MovementMask.NPC_STARTMOVE;

        final Stat2 stat = _owner.getGameStats().getMovementSpeed();
        if (_owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
            _targetMask |= stat.getBonus() < 0 ? MovementMask.NPC_RUN_FAST : MovementMask.NPC_RUN_SLOW;
        } else if (_owner.isInState(CreatureState.WALKING) || _owner.isInState(CreatureState.ACTIVE)) {
            _targetMask |= stat.getBonus() < 0 ? MovementMask.NPC_WALK_FAST : MovementMask.NPC_WALK_SLOW;
        }
        if (_owner.isFlying())
            _targetMask |= MovementMask.GLIDE;
        if (_owner.getAi2().getState() == AIState.RETURNING) {
            _targetMask |= MovementMask.NPC_RUN_FAST;
        }
    }
}
