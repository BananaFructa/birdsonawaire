package BananaFructa.bow;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.libraries.ModList;

@Mod(modid = Bow.modid,version = Bow.version,name = Bow.name)
public class Bow {

    public static final String modid = "bow";
    public static final String name = "Birds On A Wire";
    public static final String version = "0.1";
    public static boolean AlternatingFluxLoaded = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(proxy);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        AlternatingFluxLoaded = Loader.isModLoaded("alternatingflux");
    }

    @SidedProxy(modId = Bow.modid,clientSide = "BananaFructa.bow.ClientProxy",serverSide = "BananaFructa.bow.CommonProxy")
    public static CommonProxy proxy;

}
