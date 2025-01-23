package immersive_aircraft.physics;

import immersive_aircraft.entity.AirplaneEntity;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class AirplaneParameter {
    private static final float NORMAL_SPEED = 1f;// m/t
    @NotNull
    protected AirplaneEntity airplane;

    public AirplaneParameter(@NotNull AirplaneEntity airplane) {
        this.airplane = airplane;
    }

    public float getPitch() {
        return airplane.getXRot();
    }

    public float getYaw() {
        return airplane.getYRot();
    }

    public float getRoll() {
        return airplane.getRoll();
    }

    public Vec3 getVelocity() {
        return airplane.getDeltaMovement();
    }

    public Vec3 getDirection() {
        return airplane.toVec3d(airplane.getForwardDirection()).normalize();
    }

    public float getThrust() {
        return airplane.getEnginePower();
    }

    public float getPitchEffect() {
        return airplane.getProperties().get(VehicleStat.PITCH_SPEED)
                * ((float) getVelocity().length()) / NORMAL_SPEED;
    }

    public boolean onGround() {
        return airplane.onGround();
    }

    public float getYawEffect() {
        return airplane.getProperties().get(VehicleStat.PITCH_SPEED)
                * ((float) getVelocity().length()) / NORMAL_SPEED
                * 0.5f;
    }

    public float getRollEffect() {
        return airplane.getProperties().get(VehicleStat.YAW_SPEED)
                * ((float) getVelocity().length()) / NORMAL_SPEED
                *1.5f;
    }
}
