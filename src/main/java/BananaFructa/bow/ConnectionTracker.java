package BananaFructa.bow;

import BananaFructa.bow.events.NewConnection;
import BananaFructa.bow.events.OldConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jdk.nashorn.internal.parser.TokenStream;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.MinecraftForge;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class ConnectionTracker {

    Minecraft mc;

    public ConnectionTracker () {
        mc = Minecraft.getMinecraft();
    }

    public final Object lock = new Object();

    HashMap<Vec3i,HashMap<Vec3i,SimplifiedConnection>> connections = new HashMap<>();
    HashMap<Vec3i,HashMap<Vec3i,Boolean>> checked = new HashMap<>();

    public void addCon(ImmersiveNetHandler.Connection con) {
        if (!connections.containsKey(con.start)) {
            connections.put(con.start, new HashMap<>());
            checked.put(con.start, new HashMap<>());
        }
        SimplifiedConnection s = new SimplifiedConnection(con);
        connections.get(con.start).put(con.end, s);
        checked.get(con.start).put(con.end, true);
        MinecraftForge.EVENT_BUS.post(new NewConnection(s));
    }

    public void addCon(SimplifiedConnection con) {
        if (!connections.containsKey(con.p1)) {
            connections.put(con.p1, new HashMap<>());
            checked.put(con.p1, new HashMap<>());
        }
        connections.get(con.p1).put(con.p2, con);
        checked.get(con.p1).put(con.p2, true);
        MinecraftForge.EVENT_BUS.post(new NewConnection(con));
    }

    public void removeCon(Vec3i v1, Vec3i v2) {
        SimplifiedConnection con = connections.get(v1).get(v2);
        connections.get(v1).remove(v2);
        if (connections.get(v1).isEmpty()) connections.remove(v1);
        checked.get(v1).remove(v2);
        if (checked.get(v1).isEmpty())checked.remove(v1);
        MinecraftForge.EVENT_BUS.post(new OldConnection(con));
    }

    public Tuple<Vec3i,Vec3i> order(Vec3i a, Vec3i b) {
        if (connections.containsKey(a)) {
            if (connections.get(a).containsKey(b)) return new Tuple<>(a,b);
        }
        if (connections.containsKey(b)) {
            if (connections.get(b).containsKey(a)) return new Tuple<>(b,a);
        }
        return null;
    }

    public synchronized void evaluateConnections(Collection<ImmersiveNetHandler.Connection> conns) {
        synchronized (lock) {
            for (ImmersiveNetHandler.Connection con : conns) {

                Tuple<Vec3i, Vec3i> o = order(con.start, con.end);

                if (o == null) {
                    addCon(con);
                } else if (con.cableType == connections.get(o.getFirst()).get(o.getSecond()).type) {
                    checked.get(o.getFirst()).put(o.getSecond(), true);
                }
            }
            checkForNonExistent();
        }
    }

    public void checkForNonExistent() {
        List<Tuple<Vec3i,Vec3i>> toRemove = new ArrayList<>();
        for (Vec3i p1 : connections.keySet()) {
            for (Vec3i p2 : connections.get(p1).keySet()) {
                if (inPlayerRadius(p1.getX(), p1.getZ()) || inPlayerRadius(p2.getX(), p2.getZ())) {
                    if (!checked.get(p1).get(p2)) toRemove.add(new Tuple<>(p1,p2));
                    else {
                        checked.get(p1).put(p2,false);
                    }
                } else {
                    checked.get(p1).put(p2,false);
                }
            }
        }

        for (Tuple<Vec3i,Vec3i> t : toRemove) {
            removeCon(t.getFirst(),t.getSecond());
        }
    }

    public void removeAll() {
        synchronized (lock) {
            List<Tuple<Vec3i, Vec3i>> toRemove = new ArrayList<>();
            for (Vec3i p1 : connections.keySet()) {
                for (Vec3i p2 : connections.get(p1).keySet()) {
                    toRemove.add(new Tuple<>(p1, p2));
                }
            }

            for (Tuple<Vec3i, Vec3i> t : toRemove) {
                removeCon(t.getFirst(), t.getSecond());
            }
        }
    }

    public boolean inPlayerRadius(int x,int z) {
        return Math.abs(mc.player.posX - x) < mc.gameSettings.renderDistanceChunks * 16 && Math.abs(mc.player.posZ - z) < mc.gameSettings.renderDistanceChunks * 16;
    }

    public void save(String path) {
            File file = new File(path);
            file.mkdirs();
            try {

                Gson gson = new Gson();

                List<SerializableSC> serializableSCS = new ArrayList<>();

                synchronized (lock) {
                    for (Vec3i p1 : connections.keySet()) {
                        for (Vec3i p2 : connections.get(p1).keySet()) {
                            serializableSCS.add(new SerializableSC(connections.get(p1).get(p2)));
                        }
                    }
                }

                BufferedWriter stream = new BufferedWriter(new FileWriter(new File(file,"electric_network.bow")));
                stream.write(gson.toJson(serializableSCS));
                stream.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
    }

    public void load(String path) {
        File f = new File(path,"electric_network.bow");
        if (f.exists()) {
            try {

                Gson gson = new Gson();

                FileInputStream inputStream = new FileInputStream(f);
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                List<SerializableSC> list = gson.fromJson(new String(bytes, StandardCharsets.UTF_8),new TypeToken<List<SerializableSC>>(){}.getType());

                removeAll();

                for (SerializableSC serializableSC: list) {
                    addCon(serializableSC.toSC());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
