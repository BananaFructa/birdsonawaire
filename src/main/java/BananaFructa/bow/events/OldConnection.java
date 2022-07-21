package BananaFructa.bow.events;

import BananaFructa.bow.SimplifiedConnection;
import net.minecraftforge.fml.common.eventhandler.Event;

public class OldConnection extends Event {

    public SimplifiedConnection con;

    public OldConnection(SimplifiedConnection con) {
        this.con = con;
    }

}
