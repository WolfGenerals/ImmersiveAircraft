package immersive_aircraft.entity;

import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public abstract class Rotorcraft extends AircraftEntity {
    public Rotorcraft(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    public Vector3f getForwardDirection() {
        return new Vector3f(
                Mth.sin(-getYRot() * ((float) Math.PI / 180)),
                0.0f,
                Mth.cos(getYRot() * ((float) Math.PI / 180))
        ).normalize();
    }

    @Override
    public Vector3f getRightDirection() {
        return new Vector3f(
                Mth.cos(-getYRot() * ((float) Math.PI / 180)),
                0.0f,
                Mth.sin(getYRot() * ((float) Math.PI / 180))
        ).normalize();
    }

    @Override
    protected void convertPower(Vec3 direction) {
        Vec3 velocity = getDeltaMovement().multiply(1.0f, 0.0f, 1.0f);
        double drag = Math.abs(direction.dot(velocity.normalize()));
        Vec3 newVelocity = velocity.normalize()
                .lerp(direction, getProperties().get(VehicleStat.LIFT))
                .scale(velocity.length() * (drag * getProperties().get(VehicleStat.FRICTION) + (1.0 - getProperties().get(VehicleStat.FRICTION))));
        setDeltaMovement(
                newVelocity.x,
                getDeltaMovement().y,
                newVelocity.z
        );
    }

    @Override
    public void tick() {
        // rolling interpolation
        prevRoll = roll;
        if (onGround()) {
            setZRot(roll * 0.9f);
        } else {
            setZRot(-pressingInterpolatedX.getSmooth() * getProperties().get(VehicleStat.ROLL_FACTOR));
        }

        if (Double.isNaN(getDeltaMovement().x) || Double.isNaN(getDeltaMovement().y) || Double.isNaN(getDeltaMovement().z)) {
            setDeltaMovement(0, 0, 0);
        }

        super.tick();
    }

    @Override
    protected void updateController() {
        // left-right
        setYRot(getYRot() - getProperties().get(VehicleStat.YAW_SPEED) * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (!onGround()) {
            setXRot(getXRot() + getProperties().get(VehicleStat.PITCH_SPEED) * pressingInterpolatedZ.getSmooth());
        }
        setXRot(getXRot() * (1.0f - getProperties().getAdditive(VehicleStat.STABILIZER)));
    }
}
