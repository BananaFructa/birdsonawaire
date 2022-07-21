package BananaFructa.bow;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import net.minecraft.util.math.Vec3i;

import java.io.Serializable;

public class SimplifiedConnection {

    public Vec3i p1;
    public Vec3i p2;
    public WireType type;

    public SimplifiedConnection(Vec3i p1, Vec3i p2, WireType type) {
        this.p1 = p1;
        this.p2 = p2;
        this.type = type;
    }

    public SimplifiedConnection(ImmersiveNetHandler.Connection con) {
        p1 = new Vec3i(con.start.getX(),con.start.getY(),con.start.getZ());
        p2 = new Vec3i(con.end.getX(),con.end.getY(),con.end.getZ());
        type = con.cableType;
    }

    public boolean equals(SimplifiedConnection o) {
        return type == o.type && ((p1.equals(o.p1) && p2.equals(o.p2)) || (p1.equals(o.p2) && p2.equals(o.p1)));
    }

    public boolean equals(ImmersiveNetHandler.Connection o) {
        return type == o.cableType && ((p1.equals(o.start) && p2.equals(o.end)) || (p1.equals(o.end) && p2.equals(o.start)));
    }

}
