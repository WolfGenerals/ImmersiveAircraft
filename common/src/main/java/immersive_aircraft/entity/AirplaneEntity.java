package immersive_aircraft.entity;

import immersive_aircraft.item.upgrade.VehicleStat;
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

    @Override
    protected void updateController() {
        if (!isVehicle()) {
            return;
        }

        // left-right
        setYRot(getYRot() - getProperties().get(VehicleStat.YAW_SPEED) * pressingInterpolatedX.getSmooth());

        // forwards-backwards
        if (!onGround()) {
            setXRot(getXRot() + getProperties().get(VehicleStat.PITCH_SPEED) * pressingInterpolatedZ.getSmooth());
        }
        setXRot(getXRot() * (1.0f - getProperties().getAdditive(VehicleStat.STABILIZER)));

        // engine control
        if (movementY != 0) {
            setEngineTarget(Math.max(0.0f, Math.min(1.0f, getEngineTarget() + 0.1f * movementY)));
            if (movementY < 0) {
                setDeltaMovement(getDeltaMovement().scale(getBrakeFactor()));
            }
        }

        // get direction
        Vector3f direction = getForwardDirection();

        // speed
        float thrust = (float) (Math.pow(getEnginePower(), 2.0) * getProperties().get(VehicleStat.ENGINE_SPEED));
        if (onGround() && getEngineTarget() < 1.0) {
            thrust = getProperties().get(VehicleStat.PUSH_SPEED) / (1.0f + (float) getDeltaMovement().length() * 5.0f) * pressingInterpolatedZ.getSmooth() * (1.0f - getEnginePower());
        }

        // accelerate
        setDeltaMovement(getDeltaMovement().add(toVec3d(direction.mul(thrust))));
    }

    @Override
    public void tick() {
        // rolling interpolation
        prevRoll = roll;
        if (onGround()) {
            setZRot(roll * 0.9f);
        } else {
            setZRot(-pressingInterpolatedX.getSmooth() * getProperties().get(VehicleStat.ROLL_FACTOR) * 4);
        }

        if (Double.isNaN(getDeltaMovement().x) || Double.isNaN(getDeltaMovement().y) || Double.isNaN(getDeltaMovement().z)) {
            setDeltaMovement(0, 0, 0);
        }

        super.tick();
    }

    @Override
    protected void convertPower(@NotNull Vec3 direction) {
        // 获取当前速度
        Vec3 velocity = getDeltaMovement();

        // 计算方向与速度之间的夹角（拖曳力）
        double drag = Math.abs(direction.dot(velocity.normalize()));

        // 根据升力线性插值调整方向
        Vec3 adjustedDirection = velocity.normalize().lerp(direction, getProperties().get(VehicleStat.LIFT));

        // 根据摩擦力和拖曳力缩放速度
        double friction = getProperties().get(VehicleStat.FRICTION);
        double scaledSpeed = velocity.length() * (drag * friction + (1.0 - friction));
        Vec3 newVelocity = adjustedDirection.scale(scaledSpeed);

        // 更新飞机速度
        setDeltaMovement(newVelocity);
    }
}
