package mw.processors.movement.motor;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.templates.walker.RouteStep;
import com.ne.gs.model.templates.walker.WalkerTemplate;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import mw.engines.geo.GeoEngine;
import mw.utils.GeomUtil;
import mw.engines.geo.math.Vector3f;
import mw.processors.movement.MovementProcessor;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Motor, that move owner object by walking routes
 * This motor NOT use pathfinding for movements validation
 *
 * @author MetaWind
 */
public class WalkMotor extends AMovementMotor {

    private static final float MINIMAL_DISTANCE = 0.1f;

    /* Walker template to walk to */
    private final WalkerTemplate _walkerTemplate;
    private final int _firstRouteIndex;

    /* Last movement milliseconds*/
    private long _lastMoveMs;

    private Vector3f _lastMovePoint;

    /* Current route step */
    private RouteStep _current;

    /* Current movement task */
    private ScheduledFuture<?> _task;

    /* Current movement task */
    private ScheduledFuture<?> _positionUpdateTask;

    public WalkMotor(Creature owner, MovementProcessor processor, WalkerTemplate template, int nextRouteIndex) {
        super(owner, processor);
        _walkerTemplate = template;
        _firstRouteIndex = nextRouteIndex;
    }

    @Override
    public void start() {

        RouteStep closest = null;

        if(_firstRouteIndex < 0) {
            List<RouteStep> steps = _walkerTemplate.getRouteSteps();

            double minDistance = Short.MAX_VALUE;
            Vector3f ownerPoint = _owner.getPosition().getPoint();

            for (RouteStep step : steps) {

                float distance = GeomUtil.getDistance3D(ownerPoint, step.getX(), step.getY(), step.getZ());
                if (distance > minDistance)
                    break; //last spot was closest

                minDistance = distance;
                closest = step;
            }
        }
        else{
            closest =_walkerTemplate.getRouteStep(_firstRouteIndex);
        }

        if (closest == null)
            throw new Error("No available point was found for index " + _firstRouteIndex);

        update(closest);
        _positionUpdateTask = _processor.scheduleAtFixedRate(this::setActualPosition, 0, 2000);

    }

    @Override
    public void stop() {
        if (_task != null)
            _task.cancel(false);


        if(_positionUpdateTask != null)
        _positionUpdateTask.cancel(false);

        setActualPosition();
    }

    public RouteStep getCurrentStep(){
        return _current;
    }

    public boolean update(RouteStep beginStep) {

        if(beginStep == null)
            return false;

        Vector3f ownerPoint = _owner.getPosition().getPoint();
        float distance = GeomUtil.getDistance3D(
                ownerPoint,
                beginStep.getX(),
                beginStep.getY(),
                beginStep.getZ());

        if (distance < MINIMAL_DISTANCE) {
            beginStep = beginStep.getNextStep();
            distance = GeomUtil.getDistance3D(
                    ownerPoint,
                    beginStep.getX(),
                    beginStep.getY(),
                    beginStep.getZ());
        }

        _lastMovePoint = _owner.getPosition().getPoint();
        _lastMoveMs = System.currentTimeMillis();

        _current = beginStep;
        _targetPosition = new Vector3f(beginStep.getX(), beginStep.getY(), beginStep.getZ());

        recalculateMovementParams();

        final float speed = _owner.getGameStats().getMovementSpeedFloat();
        long movementTime = (long) ((distance / speed) * 1000);

        PacketSendUtility.broadcastPacket(_owner, new SM_MOVE(
                _owner.getObjectId(),
                _owner.getX(), _owner.getY(), _owner.getZ(),
                _targetPosition.x, _targetPosition.y, _targetPosition.z,
                _targetHeading,
                _targetMask
        ));

        final RouteStep next = beginStep.getNextStep();

        _task = _processor.schedule(() -> {

            if (_targetPosition != null) {
                //_positionUpdateTask.cancel(false);
                World.getInstance().updatePosition(_owner,
                        _targetPosition.x,
                        _targetPosition.y,
                        _targetPosition.z, _targetHeading, true);
            }

            //_owner.getAi2().onGeneralEvent(AIEventType.TARGET_REACHED);
            _processor.schedule(() -> update(next), 0);


        }, movementTime);

        return true;
    }

    private void setActualPosition() {
        Vector3f lastMove = _lastMovePoint;
        Vector3f targetMove = new Vector3f(_current.getX(), _current.getY(), _current.getZ());

        float speed = _owner.getGameStats().getMovementSpeedFloat();
        long time = System.currentTimeMillis() - _lastMoveMs;
        float distPassed = speed * (time / 1000f);
        float maxDist = lastMove.distance(targetMove);
        if (distPassed <= 0)
            return;

        if(distPassed > maxDist)
            distPassed = maxDist;

        Vector3f dir = GeomUtil.getDirection3D(lastMove, targetMove);
        Vector3f position = GeomUtil.getNextPoint3D(lastMove, dir, distPassed);
        position.z = GeoEngine.getZ(_owner.getWorldId(), _owner.getInstanceId(), position.x, position.y, position.z);

        World.getInstance().updatePosition(_owner,
                position.x,
                position.y,
                position.z, _targetHeading, false);

        //_owner.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
        _owner.getAi2().onGeneralEvent(AIEventType.MOVE_ARRIVED);
    }
}
