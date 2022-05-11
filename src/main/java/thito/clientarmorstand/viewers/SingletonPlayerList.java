package thito.clientarmorstand.viewers;

import org.bukkit.entity.Player;
import thito.clientarmorstand.ViewerList;

public class SingletonPlayerList extends ViewerList {
    private final Player player;

    public SingletonPlayerList(Player player) {
        this.player = player;
        addPlayer(player);
    }

    public Player getPlayer() {
        return player;
    }
}
