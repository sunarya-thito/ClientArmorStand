package thito.clientarmorstand.viewers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import thito.clientarmorstand.ViewerList;

public class OnlinePlayerList extends ViewerList implements Listener {
    @Override
    protected void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player); // this will ignore if the player is already a viewer
        }
        super.tick(); // this does nothing actually
    }
}
