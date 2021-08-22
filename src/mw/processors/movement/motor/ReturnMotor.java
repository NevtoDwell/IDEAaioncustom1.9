package mw.processors.movement.motor;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import mw.engines.geo.math.Vector3f;
import mw.processors.movement.MovementProcessor;

import java.util.concurrent.ScheduledFuture;

/**
 * Motor, that move owner object to target spot
 * This motor not use any pathfinding or geo calculations
 *
 * @author MetaWind
 */
public class ReturnMotor extends AMovementMotor {

    /* Current movement task */
    private ScheduledFuture<?> _task;

    /**
     * Base constructor
     *
     * @param owner     Creature owner
     * @param processor Parent movement processor
     */
    public ReturnMotor(Creature owner, Vector3f spot, MovementProcessor processor) {
        super(owner, processor);
        _targetPosition = spot;
    }

    @Override
    public void start() {

        assert _task == null;

        recalculateMovementParams();

        final float distance = _owner.getPosition().getDistance(_targetPosition);
        final float speed = _owner.getGameStats().getMovementSpeedFloat();
        long movementTime = (long) ((distance / speed) * 1000);

        PacketSendUtility.broadcastPacket(_owner, new SM_MOVE(
                _owner.getObjectId(),
                _owner.getX(), _owner.getY(), _owner.getZ(),
                _targetPosition.x, _targetPosition.y, _targetPosition.z,
                _targetHeading,
                _targetMask
        ));

        _task = _processor.schedule(() -> {

            World.getInstance().updatePosition(_owner,
                    _targetPosition.x,
                    _targetPosition.y,
                    _targetPosition.z, _targetHeading, false);

            _owner.getAi2().onGeneralEvent(AIEventType.MOVE_ARRIVED);
            _owner.getAi2().onGeneralEvent(AIEventType.BACK_HOME);

        }, movementTime);
    }

    @Override
    public void stop() {
        //this motor is unstoppable
    }
}
