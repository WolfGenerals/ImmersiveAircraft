package immersive_aircraft.entity;

import immersive_aircraft.physics.AirplaneParameter;
import immersive_aircraft.physics.PlanePhysicsEngine;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/**
 * Implements airplane like physics properties and accelerated towards
 */
public abstract class AirplaneEntity extends AircraftEntity {
    public AirplaneEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    protected boolean useAirplaneControls() {
        return true;
    }

    @Override
    protected float getGravity() {
        Vector3f direction = getForwardDirection();
        float speed = (float) getDeltaMovement().length() * (1.0f - Math.abs(direction.y));
        return Math.max(0.0f, 1.0f - speed * 1.5f) * super.getGravity();
    }

    protected float getBrakeFactor() {
        return 0.95f;
    }


    protected PlanePhysicsEngine physicsEngine = new PlanePhysicsEngine(new AirplaneParameter(this));
    @Override
    protected void updateController() {
        physicsEngine.input(pressingInterpolatedZ.getSmooth(), pressingInterpolatedX.getSmooth(),0);

        // engine control
        if (movementY != 0) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + 0.1f * movementY)));
            if (movementY < 0) {
                setDeltaMovement(getDeltaMovement().scale(getBrakeFactor()));
            }
        }
    }

    @Override
    public void tick() {
        if (Double.isNaN(getDeltaMovement().x) || Double.isNaN(getDeltaMovement().y) || Double.isNaN(getDeltaMovement().z)) {
            setDeltaMovement(0, 0, 0);
        }

        super.tick();
    }

    @Override
    protected void convertPower(@NotNull Vec3 direction) {
        PlanePhysicsEngine.Result result = physicsEngine.emulator();

        prevRoll = getRoll();
        setXRot(getXRot()+result.xRot);
        setYRot(getYRot()+result.yRot);
        setZRot(getRoll()+result.zRot);
        setDeltaMovement(result.deltaPosition);
    }
}
