package mw.processors.movement.motor;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import mw.processors.movement.MovementProcessor;
import mw.processors.movement.PathfindHelper;

import java.util.concurrent.ScheduledFuture;

/**
 * Motor, that move owner object to other target object
 * This motor use pathfinding for movements validation
 *
 * @author MetaWind
 */
public class FollowMotor extends AMovementMotor {

    /* Maximal pathfind step length*/
    private static final float PATHFIND_MAX_STEP = 6f;

    /* Revalidate target position time in milliseconds */
    private static final int TARGET_REVALIDATE_TIME = 400;

    /* Object to follow to */
    public VisibleObject _target;

    /* Current movement task */
    private ScheduledFuture<?> _task;

    @Override
    public void start() {
        assert _task == null;
        update();
    }

    @Override
    public void stop() {
        if (_task != null)
            _task.cancel(false);

        _target = null;
    }

    /**
     * Default constructor
     *
     * @param parentProcessor Parent movement processor
     * @param owner           Motor owner
     * @param target
     */
    public FollowMotor(
            MovementProcessor parentProcessor,
            Creature owner,
            VisibleObject target) {
        super(owner, parentProcessor);

        _target = target;
    }

    /**
     * Select new movement point and begin move
     *
     * @return
     */
    public boolean update() {

        VisibleObject target = _target;

        if (target == null || _task != null && _task.isCancelled() || _owner.getLifeStats().isAlreadyDead())
            return false;

        if(canProcess()) {
            _targetPosition = PathfindHelper.selectFollowStep(
                    _owner,
                    target,
                    Math.max(PATHFIND_MAX_STEP, PATHFIND_MAX_STEP * _owner.getObjectTemplate().getBoundRadius().getCollision()));
        }
        else
            _targetPosition = null;

        long movementTime = TARGET_REVALIDATE_TIME; //default time

        if (_targetPosition != null) {

            recalculateMovementParams();

            final float distance = _owner.getPosition().getDistance(_targetPosition);
            final float speed = _owner.getGameStats().getMovementSpeedFloat();
            movementTime = (long) ((distance / speed) * 1000);

            if(movementTime == 0)
                movementTime = TARGET_REVALIDATE_TIME;

            PacketSendUtility.broadcastPacket(_owner, new SM_MOVE(
                    _owner.getObjectId(),
                    _owner.getX(), _owner.getY(), _owner.getZ(),
                    _targetPosition.x, _targetPosition.y, _targetPosition.z,
                    _targetHeading,
                    _targetMask
            ));

        }

        _task = _processor.schedule(() -> {

            if (_targetPosition != null) {
                World.getInstance().updatePosition(_owner,
                        _targetPosition.x,
                        _targetPosition.y,
                        _targetPosition.z,
                        _targetHeading,
                        false);
            }

            _processor.schedule(()->update(), 0);

        }, movementTime);

        return true;
    }

    private boolean canProcess(){
        return !_owner.getEffectController().isUnderFear() && _owner.canPerformMove() && _target.getX() != 0 && _target.getY() != 0 && _target != null;
    }
}
