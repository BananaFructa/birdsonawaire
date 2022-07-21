package BananaFructa.bow.events;

import BananaFructa.bow.SimplifiedConnection;
import net.minecraftforge.fml.common.eventhandler.Event;

public class NewConnection extends Event {

    public SimplifiedConnection con;

    public NewConnection(SimplifiedConnection con) {
        this.con = con;
    }

}
