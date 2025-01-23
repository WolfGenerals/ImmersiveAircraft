package immersive_aircraft.physics;

import net.minecraft.world.phys.Vec3;

public class PlanePhysicsEngine {
    // control surface, -1 to 1
    protected float pitchControl = 0.0f;
    protected float rollControl = 0.0f;
    protected float yawControl = 0.0f;

    protected AirplaneParameter state;

    public PlanePhysicsEngine(AirplaneParameter state) {
        this.state = state;
    }

    public void input(float pitch, float roll, float yaw) {
        pitchControl = pitch;
        rollControl = roll;
        yawControl = yaw;
        // check
        pitchControl = Math.max(-1.0f, Math.min(1.0f, pitchControl));
        rollControl = Math.max(-1.0f, Math.min(1.0f, rollControl));
        yawControl = Math.max(-1.0f, Math.min(1.0f, yawControl));
    }

    public static class Result {
        public float xRot;
        public float yRot;
        public float zRot;
        public Vec3 deltaPosition;
    }

    public Result emulator() {
        Result result = new Result();

        rudderSurface(result);

        result.deltaPosition = state.getDirection().scale(state.getThrust());
//        // 飞机参考系三轴方向
//        Vec3 planeXAxis = new Vec3(
//                Math.cos(r)*Math.cos(y),
//                Math.sin(r)*Math.cos(y),
//                -Math.sin(y)
//        );
//        Vec3 planeYAxis = new Vec3(
//                Math.cos(r)*Math.sin(y)*Math.sin(p) - Math.sin(r)*Math.cos(p),
//                Math.sin(r)*Math.sin(y)*Math.sin(p) + Math.cos(r)*Math.cos(p),
//                Math.cos(y)*Math.sin(p)
//        );
//        Vec3 planeZAxis = new Vec3(
//                Math.cos(r)*Math.sin(y)*Math.cos(p) + Math.sin(r)*Math.sin(p),
//                Math.sin(r)*Math.sin(y)*Math.cos(p) - Math.cos(r)*Math.sin(p),
//                Math.cos(y)*Math.cos(p)
//        );

        return result;
    }

    private void rudderSurface(Result result) {
        // 弧度制rpy
        double r = Math.toRadians(state.getRoll());
        double p = Math.toRadians(state.getPitch());
        double y = Math.toRadians(state.getYaw()+90);
        // control surface
        result.zRot -= state.getRollEffect() * rollControl;
        result.xRot += state.getPitchEffect() * pitchControl *
                (float) (Math.cos(r));
        result.yRot -= state.getPitchEffect() * pitchControl *
                (float) (Math.cos(p) * Math.sin(r));
        result.xRot -= state.getYawEffect() * yawControl *
                (float) (Math.sin(r));
        result.yRot -= state.getYawEffect() * yawControl *
                (float) (Math.cos(p) * Math.cos(r));
    }
}
