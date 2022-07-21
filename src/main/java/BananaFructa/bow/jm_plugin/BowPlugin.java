package BananaFructa.bow.jm_plugin;

import BananaFructa.bow.Bow;
import BananaFructa.bow.SimplifiedConnection;
import BananaFructa.bow.Utils;
import BananaFructa.bow.events.NewConnection;
import BananaFructa.bow.events.OldConnection;
import antibluequirk.alternatingflux.wire.AFWireType;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.ImageOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.DisplayUpdateEvent;
import journeymap.client.api.model.MapImage;
import journeymap.client.io.ThemeLoader;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.component.OnOffButton;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.theme.ThemeButton;
import journeymap.client.ui.theme.ThemeToggle;
import journeymap.client.ui.theme.ThemeToolbar;
import journeymap.common.Journeymap;
import journeymap.common.properties.Category;
import journeymap.common.properties.config.BooleanField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;

@ParametersAreNonnullByDefault
@journeymap.client.api.ClientPlugin
public class BowPlugin implements IClientPlugin {

    private static IClientAPI api;
    final HashMap<String,ImageOverlay> overlays = new HashMap<>();
    public boolean displayNetwork = false;

    @Override
    public void initialize(IClientAPI iClientAPI) {
        MinecraftForge.EVENT_BUS.register(this);
        api = iClientAPI;
        api.subscribe(Bow.modid, EnumSet.of(ClientEvent.Type.MAPPING_STARTED, ClientEvent.Type.MAPPING_STOPPED, ClientEvent.Type.DISPLAY_UPDATE));
    }

    @Override
    public String getModId() {
        return Bow.modid;
    }

    private String idFromVecs(Vec3i p1,Vec3i p2) {
        return "CON-" + p1.getX() + "-" + p1.getY() + "-" + p1.getZ() + "-" + p2.getX() + "-" + p2.getY() + "-" + p2.getZ();
    }

    @SubscribeEvent
    public void onNewCon (NewConnection event) {

        System.out.println("NEW CON");

        SimplifiedConnection con = event.con;
        int z1 = con.p1.getZ();
        int z2 = con.p2.getZ();
        int x1 = con.p1.getX();
        int x2 = con.p2.getX();
        double dist = Math.sqrt(Math.pow(z1-z2,2)+Math.pow(x1-x2,2));
        final int imageHeight = 20;
        final int imageWidth = (int)dist;
        if (imageWidth <= 0) return;
        BufferedImage buf = new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = buf.createGraphics();
        g.setColor(new Color(255, 255, 255,255));
        if (con.type == WireType.COPPER || con.type == WireType.COPPER_INSULATED) g.setColor(new Color(190, 132, 102,255));
        if (con.type == WireType.ELECTRUM || con.type == WireType.ELECTRUM_INSULATED) g.setColor(new Color(226, 164, 91,255));
        if (con.type == WireType.STEEL)  g.setColor(new Color(131, 129, 128,255));
        if (con.type == WireType.REDSTONE) g.setColor(new Color(181, 33, 50,255));
        if (Bow.AlternatingFluxLoaded) if (con.type instanceof AFWireType) g.setColor(new Color(240, 131, 101,255));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(0,imageHeight/2,imageWidth,imageHeight/2);
        g.dispose();

        MapImage img = new MapImage(buf);

        double angle = 0;

        if (z1 == z2) angle = 90;
        else {
            angle = Math.toDegrees(Math.atan((float)(x1-x2)/(float)(z1-z2)));
        }

        img.setRotation(270-(int)angle);

        float x_mid = (float)(x1 + x2)/2;
        float z_mid = (float)(z1 + z2)/2;

        BlockPos se = new BlockPos(x_mid + (float)imageWidth/2,255,z_mid + (float)imageHeight/2 + 1);
        BlockPos nw = new BlockPos(x_mid - (float)imageWidth/2,255,z_mid - (float)imageHeight/2 + 1);

        String id = idFromVecs(con.p1,con.p2);

        ImageOverlay overlay = new ImageOverlay(Bow.modid,id,nw,se,img);

        try {
            overlays.put(id,overlay);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    @SubscribeEvent
    public void onOldCon(OldConnection event) {
        try {
            synchronized (overlays) {
                String id = idFromVecs(event.con.p1, event.con.p2);
                ImageOverlay overlay = overlays.get(id);
                api.remove(overlay);
                overlays.remove(id);
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    void refreshState(Fullscreen f) {
        // I give up
        int zoom = Journeymap.getClient().getFullMapProperties().zoomLevel.get();
        if (zoom > 0) {
            f.zoomOut();
            f.zoomIn();
        } else {
            f.zoomIn();
            f.zoomOut();
        }
    }

    void showAll() {
        synchronized (overlays) {
            for (ImageOverlay overlay : overlays.values()) {
                try {
                    api.show(overlay);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
    }

    void hideAll() {
        synchronized (overlays) {
            for (ImageOverlay overlay : overlays.values()) {
                try {
                    api.remove(overlay);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onEvent(ClientEvent clientEvent) {
    }

    public boolean canAddButtons(GuiScreen gui) {
        return Utils.readDeclaredField(Fullscreen.class,gui,"optionsToolbar") != null;
    }

    public void addButtons(GuiScreen gui,ThemeButton... buttons) {
        ThemeToolbar optionToolbar = Utils.readDeclaredField(Fullscreen.class,gui,"optionsToolbar");
        // Remove all already loaded option tool bar buttons
        ((JmUI)gui).getButtonList().removeAll(Utils.readDeclaredField(ThemeToolbar.class,optionToolbar,"buttonList"));
        // Add our buttons in the option toolbar
        optionToolbar.add(buttons);
        // Readd the tool bar buttons
        optionToolbar.addAllButtons((JmUI)gui);

    }

    boolean wait = false;
    boolean inFullScreen = false;
    boolean wasInFullScreen = false;

    // Waiting loop since the gui event is fired before the gui init is fired.
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        inFullScreen = gui instanceof Fullscreen;

        // Do not let the network to be displayed on the minimap
        if (wasInFullScreen && !inFullScreen && displayNetwork) hideAll();

        if (wait && inFullScreen) {
            if (canAddButtons(gui)) {


                ThemeButton electricNetwork = new ThemeToggle(ThemeLoader.getCurrentTheme(),"Electric Network","electric_network", new BooleanField(Category.Inherit,displayNetwork));
                electricNetwork.setTooltip("Shown the Immersive Engineering electric network.");
                electricNetwork.addToggleListener(new OnOffButton.ToggleListener() {
                    @Override
                    public boolean onToggle(OnOffButton onOffButton, boolean b) {
                        displayNetwork = b;
                        if (displayNetwork) {
                            refreshState((Fullscreen)gui);
                            showAll();
                        } else {
                            hideAll();
                        }
                        return true;
                    }
                });

                addButtons(gui,electricNetwork);

                if (displayNetwork) {
                    refreshState((Fullscreen)gui);
                    showAll();
                }

                wait = false;
            }
        } else if (!inFullScreen && wait) {
            wait = false;
        }

        wasInFullScreen = gui instanceof Fullscreen;
    }

    @SubscribeEvent
    public void onGui(GuiOpenEvent event) {
        if (event.getGui() instanceof Fullscreen) {
            wait = true;
        }
    }
}
