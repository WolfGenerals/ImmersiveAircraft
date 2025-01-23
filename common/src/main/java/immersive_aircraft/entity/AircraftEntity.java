package immersive_aircraft.entity;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;

/**
 * Abstract aircraft, which performs basic physics
 */
public abstract class AircraftEntity extends EngineVehicle {
    protected double lastY;

    public AircraftEntity(EntityType<? extends AircraftEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    private static final List<Trail> TRAILS = Collections.emptyList();

    public List<Trail> getTrails() {
        return TRAILS;
    }

    protected abstract void convertPower(Vec3 direction);

    @Override
    protected float getGroundDecay() {
        float gravity = Math.min(1.0f, Math.max(0.0f, getGravity() / (-0.04f)));
        float upgrade = Math.min(1.0f, getProperties().get(VehicleStat.ACCELERATION) * 0.5f);
        return (super.getGroundDecay() * gravity + (1.0f - gravity)) * (1.0f - upgrade) + upgrade;
    }

    @Override
    protected void updateVelocity() {
        // get direction
        Vector3f direction = getForwardDirection();

        // convert power
        convertPower(toVec3d(direction));

        // friction
        applyFriction();

        if (onGround()) {
            // Landing
            setXRot((getXRot() + getProperties().get(VehicleStat.GROUND_PITCH)) * 0.9f - getProperties().get(VehicleStat.GROUND_PITCH));
        } else {
            // Wind
            Vector3f effect = getWindEffect();
            setXRot(getXRot() + effect.x);
            setYRot(getYRot() + effect.z);

            float offsetStrength = 0.005f;
            setDeltaMovement(getDeltaMovement().add(effect.x * offsetStrength, 0.0f, effect.z * offsetStrength));
        }
    }

    protected void applyFriction() {
        // Decay is the basic factor of friction, basically the density of the material slowing down the vehicle
        float decay = 1.0f - getProperties().get(VehicleStat.FRICTION);
        float gravity = getGravity();
        if (wasTouchingWater) {
            gravity *= 0.25f;
            decay = 0.9f;
        } else if (onGround()) {
            if (isVehicle()) {
                decay = getGroundDecay();
            } else {
                decay = 0.75f;
            }
        }

        // Velocity decay
        Vec3 velocity = getDeltaMovement();
        float hd = getProperties().get(VehicleStat.HORIZONTAL_DECAY);
        float vd = getProperties().get(VehicleStat.VERTICAL_DECAY);
        setDeltaMovement(velocity.x * decay * hd, velocity.y * decay * vd + gravity, velocity.z * decay * hd);

        // Rotation decay
        float rf = decay * getProperties().get(VehicleStat.ROTATION_DECAY);
        pressingInterpolatedX.decay(0.0f, 1.0f - rf);
        pressingInterpolatedZ.decay(0.0f, 1.0f - rf);
    }

    public void chill() {
        lastY = 0.0f;
    }

    public float getWindStrength() {
        float sensitivity = getProperties().get(VehicleStat.WIND);
        float thundering = level().getRainLevel(0.0f);
        float raining = level().getThunderLevel(0.0f);
        float weather = (float) ((Config.getInstance().windClearWeather + getDeltaMovement().length()) + thundering * Config.getInstance().windThunderWeather + raining * Config.getInstance().windRainWeather);
        return weather * sensitivity;
    }

    public Vector3f getWindEffect() {
        float wind = getWindStrength();
        float nx = (float) (Utils.cosNoise(tickCount / 20.0 / getProperties().get(VehicleStat.MASS)) * wind);
        float nz = (float) (Utils.cosNoise(tickCount / 21.0 / getProperties().get(VehicleStat.MASS)) * wind);
        return new Vector3f(nx, 0.0f, nz);
    }
}

