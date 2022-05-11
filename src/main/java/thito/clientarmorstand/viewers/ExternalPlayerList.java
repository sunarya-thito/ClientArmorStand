package thito.clientarmorstand.viewers;

import org.bukkit.entity.Player;
import thito.clientarmorstand.Viewer;
import thito.clientarmorstand.ViewerList;

import java.util.ArrayList;
import java.util.List;

public class ExternalPlayerList extends ViewerList {
    private final List<Player> playerList;

    public ExternalPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    @Override
    protected void tick() {
        for (int i = playerList.size() - 1; i >= 0; i--) {
            if (i >= playerList.size()) continue;
            addPlayer(playerList.get(i));
        }
        List<Player> remove = new ArrayList<>();
        for (Viewer viewer : getViewers()) {
            boolean found = false;
            for (int i = playerList.size() - 1; i >= 0; i--) {
                if (i >= playerList.size()) continue;
                if (playerList.get(i).getPlayer() == viewer.getPlayer()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                remove.add(viewer.getPlayer());
            }
        }
        for (Player player : remove) removePlayer(player);
        super.tick();
    }
}
