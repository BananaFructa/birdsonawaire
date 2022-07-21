package BananaFructa.bow;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import journeymap.client.data.WorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import javax.rmi.CORBA.Util;
import java.util.Collection;

public class ClientProxy extends CommonProxy {

    ConnectionTracker tracker = new ConnectionTracker();

    int tickCounter = 0;
    int lastDim = -2;

    boolean joined = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().player != null && tickCounter >= 10) {

            if (joined) {
                joined = false;
                try {
                    tracker.load(Utils.getSaveDirectory());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            if (lastDim == -2) lastDim = Minecraft.getMinecraft().player.dimension;

            if (lastDim != Minecraft.getMinecraft().player.dimension) {
                try {
                    // save the current state as last dimension
                    tracker.save(Utils.getSaveDirectory(lastDim));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            lastDim = Minecraft.getMinecraft().player.dimension;

            Collection<ImmersiveNetHandler.Connection> cons = getAllIEWireConnections(Minecraft.getMinecraft().player.dimension);
            tracker.evaluateConnections(cons);
            tickCounter = 0;
        }

        tickCounter++;
    }

    @SubscribeEvent
    public void onJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        joined = true;
    }

    @SubscribeEvent
    public void onLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        try {
            tracker.save(Utils.getSaveDirectory());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Collection<ImmersiveNetHandler.Connection> getAllIEWireConnections(int dim) {
        return ImmersiveNetHandler.INSTANCE.getAllConnections(dim);
    }

}
