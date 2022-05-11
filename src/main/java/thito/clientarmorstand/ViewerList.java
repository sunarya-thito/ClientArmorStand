package thito.clientarmorstand;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ViewerList {
    private static final Set<ViewerList> VIEWER_LISTS = Collections.newSetFromMap(new WeakHashMap<>());
    private static ScheduledFuture<?> task;
    private static void triggerTask() {
        if (task != null) return;
        task = ClientArmorStand.getScheduledExecutorService().scheduleAtFixedRate(() -> {
            if (VIEWER_LISTS.isEmpty()) {
                task.cancel(false);
                task = null;
                return;
            }
            for (ViewerList viewerList : VIEWER_LISTS) {
                viewerList.internalTick();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }
    private final Set<ClientArmorStand> clientArmorStandSet = new HashSet<>();
    private final List<Viewer> viewers = new ArrayList<>();

    public ViewerList() {
        VIEWER_LISTS.add(this);
        triggerTask();
    }

    void register(ClientArmorStand clientArmorStand) {
        if (clientArmorStandSet.add(clientArmorStand)) {
            for (Viewer viewer : viewers) {
                viewer.register(clientArmorStand);
            }
        }
    }
    void unregister(ClientArmorStand clientArmorStand) {
        if (clientArmorStandSet.remove(clientArmorStand)) {
            for (Viewer viewer : viewers) {
                viewer.unregister(clientArmorStand);
            }
        }
    }
    private void internalTick() {
        try {
            tick();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        for (int i = viewers.size() - 1; i >= 0; i--) {
            Viewer viewer = viewers.get(i);
            Player player = viewer.getPlayer();
            if (player == null) {
                viewers.remove(i);
                continue;
            }
            viewer.tick();
        }
    }
    protected void tick() {
    }
    protected boolean addPlayer(Player player) {
        for (Viewer viewer : viewers) {
            if (viewer.getPlayer() == player) return false;
        }
        viewers.add(new Viewer(player));
        return true;
    }
    protected boolean removePlayer(Player player) {
        for (int i = viewers.size() - 1; i >= 0; i--) {
            Viewer viewer = viewers.get(i);
            if (viewer.getPlayer() == player) {
                viewers.remove(i);
                viewer.unregisterAll();
                return true;
            }
        }
        return false;
    }
    public final List<Viewer> getViewers() {
        return Collections.unmodifiableList(viewers);
    }
}
