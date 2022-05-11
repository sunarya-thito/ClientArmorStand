package thito.clientarmorstand;

import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class Viewer {
    private final WeakReference<Player> playerReference;
    private final List<ViewerData> viewerDataList = new ArrayList<>();
    public Viewer(Player player) {
        playerReference = new WeakReference<>(player);
    }

    void tick() {
        for (ViewerData viewerData : viewerDataList) {
            viewerData.computeCanSee();
        }
    }

    public ViewerData getData(ClientArmorStand clientArmorStand) {
        for (int i = viewerDataList.size() - 1; i >= 0; i--) {
            ViewerData viewerData = this.viewerDataList.get(i);
            if (viewerData.getClientArmorStand() == clientArmorStand) {
                return viewerData;
            }
        }
        return null;
    }

    public ViewerData register(ClientArmorStand clientArmorStand) {
        for (int i = viewerDataList.size() - 1; i >= 0; i--) {
            ViewerData viewerData = this.viewerDataList.get(i);
            if (viewerData.getClientArmorStand() == clientArmorStand) {
                return viewerData;
            }
        }
        ViewerData viewerData = new ViewerData(this, clientArmorStand);
        viewerDataList.add(viewerData);
        return viewerData;
    }

    public void unregisterAll() {
        for (ViewerData viewerData  : viewerDataList) {
            viewerData.getCanSee().set(false);
        }
        viewerDataList.clear();
    }

    public void unregister(ClientArmorStand clientArmorStand) {
        for (int i = viewerDataList.size() - 1; i >= 0; i--) {
            ViewerData viewerData = viewerDataList.get(i);
            if (viewerData.getClientArmorStand() == clientArmorStand) {
                viewerDataList.remove(i);
                viewerData.getCanSee().set(false);
                return;
            }
        }
    }

    public Player getPlayer() {
        Player player = playerReference.get();
        return player != null && !player.isOnline() ? null : player;
    }
}
