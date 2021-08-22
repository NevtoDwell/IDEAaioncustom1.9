package mw.processors.movement.motor;

import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.controllers.movement.MovementMask;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import mw.engines.geo.math.Vector3f;
import mw.processors.movement.MovementProcessor;
import mw.processors.movement.PathfindHelper;

import java.util.concurrent.ScheduledFuture;

/**
 * Motor, that move owner object to target coordinates
 * This motor use pathfinding for movements validation
 *
 * @author MetaWind
 */
public class PointMotor extends AMovementMotor {

    /* Maximal pathfind step length*/
    private static final float PATHFIND_MAX_STEP = 3f;

    /* Movement target point */
    private final Vector3f _point;

    /* Maximal pathfinding step len for motor owner */
    private final float _maximalStepLen;

    /* Current movement task */
    private ScheduledFuture<?> _task;

    public PointMotor(Creature owner, Vector3f point, MovementProcessor processor) {
        super(owner, processor);

        float collision = _owner.getObjectTemplate().getBoundRadius().getCollision();

        _point = point;
        _maximalStepLen = PATHFIND_MAX_STEP * collision > 0 ? collision : 1f;
    }

    @Override
    public void start() {
        assert _task == null;
        update();
    }

    @Override
    public void stop() {
        if (_task != null)
            _task.cancel(false);
    }

    /**
     * Select new movement step and begin move
     *
     * @return
     */
    public boolean update() {

        if (_task != null && _task.isCancelled())
            return false;

        _targetPosition = PathfindHelper.selectStep(
                _owner,
                _point,
                _maximalStepLen,
                0);

        if (_targetPosition == null)
            return false; //path not found or target reached

        recalculateMovementParams();

        final float distance = _owner.getPosition().getDistance(_targetPosition);
        final float speed = _owner.getGameStats().getMovementSpeedFloat();
        long movementTime = (long) ((distance / speed) * 1000);

        if(movementTime == 0)
            return false;

        PacketSendUtility.broadcastPacket(_owner, new SM_MOVE(
                _owner.getObjectId(),
                _owner.getX(), _owner.getY(), _owner.getZ(),
                _targetPosition.x, _targetPosition.y, _targetPosition.z,
                _targetHeading,
                _targetMask
        ));

        _task = _processor.schedule(() -> {

            if (_targetPosition != null && _owner.canPerformMove()) {
                World.getInstance().updatePosition(_owner,
                        _targetPosition.x,
                        _targetPosition.y,
                        _targetPosition.z, _targetHeading, false);

                _owner.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
                _processor.schedule(this::update, 0);

            }else {
                _owner.getAi2().onGeneralEvent(AIEventType.MOVE_ARRIVED);
                //_owner.getAi2().think();
            }

        }, movementTime);

        return true;
    }
}
