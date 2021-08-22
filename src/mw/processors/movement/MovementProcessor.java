package mw.processors.movement;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.walker.WalkerTemplate;
import mw.engines.geo.math.Vector3f;
import mw.processors.AGameProcessor;
import mw.processors.movement.motor.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processor that handle all npc's movements
 *
 * @author MetaWind
 */
public class MovementProcessor extends AGameProcessor {

    /*movement processor threads counter*/
    private static final int THREADS_COUNTER = 12;

    /* Creature, that registered in this processor */
    private final ConcurrentHashMap<Creature, AMovementMotor> _registeredCreatures = new ConcurrentHashMap<>();

    /**
     * Default constructor
     */
    public MovementProcessor() {
        super(THREADS_COUNTER);
    }

    /**
     * Apply movement motor
     * Moves npc to selected coordinates
     *
     * @param creature Creature to move
     * @param x Target X coordinate
     * @param y Target Y coordinate
     * @param z Target Z coordinate
     * @return NULL if movement wasn't applied for some reasons
     */
    public AMovementMotor applyMove(Npc creature, float x, float y, float z) {

        AMovementMotor motor = new PointMotor(creature, new Vector3f(x, y, z), this);
        if (applyMotor(creature, motor))
            return motor;

        return null;
    }

    /**
     * Apply movement motor
     * Moves npc to selected last registered spot coordinates
     *
     * @param creature Creature to return
     * @return NULL if movement wasn't applied for some reasons
     */
    public AMovementMotor applyReturn(Npc creature, Vector3f spot){

        AMovementMotor motor = new ReturnMotor(creature, spot, this);
        if (applyMotor(creature, motor))
            return motor;

        return null;
    }

    /**
     * Apply follow motor
     * Npc's begins follow selected target
     *
     * @param creature Source creature
     * @param target Target creature
     * @return NULL if motor wasn't applied for some reasons
     */
    public AMovementMotor applyFollow(Npc creature, VisibleObject target) {

        AMovementMotor motor = new FollowMotor(this, creature, target);
        if (applyMotor(creature, motor))
            return motor;

        return null;
    }

    /**
     * Apply walk motor
     * Npc's begin walk by selected routes
     *
     * @param creature Source creature
     * @param template Walker template
     * @return NULL if motor wasn't applied for some reasons
     */
    public AMovementMotor applyWalk(Npc creature, WalkerTemplate template, int nextRouteIndex) {

        AMovementMotor motor = new WalkMotor(creature, this, template, nextRouteIndex);
        if (applyMotor(creature, motor))
            return motor;

        return null;
    }

    /**
     * Cancel current movement motor applied to creature
     *
     * @param creature Source creature
     * @return FALSE if creature no have any motor to cancel
     */
    public boolean cancelMotor(Creature creature) {

        AMovementMotor motor = _registeredCreatures.remove(creature);
        if (motor == null)
            return false;

        motor.stop();
        return true;
    }

    /**
     * Applies motor to creature and cancel previous one if exsist
     * @param creature Source creature
     * @param newMotor Motor to apply
     * @return
     */
    private boolean applyMotor(Creature creature, AMovementMotor newMotor) {

        AMovementMotor oldMotor = _registeredCreatures.put(creature, newMotor);

        if (oldMotor == newMotor)
            throw new Error("Attempt to replace same movement motors");

        if (oldMotor != null)
            oldMotor.stop();

        newMotor.start();

        return true;
    }
}
