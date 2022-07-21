package BananaFructa.bow.events;

import BananaFructa.bow.SimplifiedConnection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.http.impl.conn.Wire;

import java.io.Serializable;

public class SerializableSC implements Serializable {

    int x1;
    int y1;
    int z1;
    int x2;
    int y2;
    int z2;
    int type = 0;

    public SerializableSC(SimplifiedConnection connection) {
        x1 = connection.p1.getX();
        y1 = connection.p1.getY();
        z1 = connection.p1.getZ();
        x2 = connection.p2.getX();
        y2 = connection.p2.getY();
        z2 = connection.p2.getZ();
        for (int i = 0;i < WireType.uniqueNames.length;i++) {
            if (WireType.uniqueNames[i].equals(connection.type.getUniqueName())) {
                type = i;
                break;
            }
        }
    }

    public SimplifiedConnection toSC() {
        return new SimplifiedConnection(new Vec3i(x1,y1,z1),new Vec3i(x2,y2,z2),WireType.getValue(WireType.uniqueNames[type]));
    }


}
