package thito.clientarmorstand;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import thito.clientarmorstand.property.NonNullObservable;
import thito.clientarmorstand.property.Observable;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class ClientArmorStand {
    static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public static void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        Validate.notNull(scheduledExecutorService, "scheduledExecutorService");
        ClientArmorStand.scheduledExecutorService = scheduledExecutorService;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static void submit(Runnable runnable) {
        scheduledExecutorService.submit(runnable);
    }

    private static int availableIds = Integer.MAX_VALUE / 2;
    static final ArmorStandVersion version = new ArmorStandVersion();

    private final int id = availableIds--;
    private final Observable<ArmorStandMeta> meta = new NonNullObservable<>(new ArmorStandMeta());
    private final Observable<Double> viewDistance = new Observable<>();
    private final Observable<Position> location = new Observable<>();
    private final UUID uniqueId = UUID.randomUUID();
    private final Observable<ViewerList> viewerSet = new Observable<>();

    public ClientArmorStand() {
        location.addListener((obs, o, v) -> {
            if (o != null && v != null) {
                broadcastPacket(createTeleportPacket(v));
            }
        });
        viewerSet.addListener((obs, o, v) -> {
            if (o != null) {
                o.unregister(this);
            }
            if (v != null) {
                v.register(this);
            }
        });
        meta.addListener((obs, o, v) -> {
            if (o != null) {
                o.unregister(this);
            }
            if (v != null) {
                v.register(this);
                PacketContainer metadataPacket = v.createPacket(this);
                broadcastPacket(metadataPacket);
                PacketContainer equipmentPacket = v.createEquipmentPacket(this, ArmorStandVersion.Slot.HELMET, v.getHelmet().get());
                broadcastPacket(equipmentPacket);
                equipmentPacket = v.createEquipmentPacket(this, ArmorStandVersion.Slot.CHESTPLATE, v.getChestplate().get());
                broadcastPacket(equipmentPacket);
                equipmentPacket = v.createEquipmentPacket(this, ArmorStandVersion.Slot.LEGGINGS, v.getLeggings().get());
                broadcastPacket(equipmentPacket);
                equipmentPacket = v.createEquipmentPacket(this, ArmorStandVersion.Slot.BOOTS, v.getBoots().get());
                broadcastPacket(equipmentPacket);
            }
        });
    }

    PacketContainer createTeleportPacket(Position location) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        packetContainer.getIntegers().write(0, id);
        version.writeTeleportLocation(packetContainer, location);
        return packetContainer;
    }

    public void setViewDistance(Double viewDistance) {
        this.viewDistance.set(viewDistance != null && viewDistance < 0 ? null : viewDistance);
    }

    public Observable<ViewerList> getViewerSet() {
        return viewerSet;
    }

    public Observable<ArmorStandMeta> getMeta() {
        return meta;
    }

    public Observable<Position> getLocation() {
        return location;
    }

    PacketContainer createSpawnPacket() {
        Position location = this.location.get();
        if (location == null) throw new IllegalStateException("location does not exist");
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packetContainer.getIntegers().write(0, id);
        version.writeSpawnArmorStandType(packetContainer);
        version.writeSpawnUniqueId(packetContainer, uniqueId);
        version.writeSpawnLocation(packetContainer, location);
        return packetContainer;
    }

    PacketContainer createDestroyPacket() {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        version.writeDestroyEntityId(packetContainer, id);
        return packetContainer;
    }

    public int getId() {
        return id;
    }

    public double getViewDistance() {
        return viewDistance.getOptionally().orElse(Bukkit.getViewDistance() * 16d);
    }

    void broadcastPacket(PacketContainer packetContainer) {
        ViewerList viewerList = this.viewerSet.get();
        if (viewerList == null) return;
        for (Viewer viewer : viewerList.getViewers()) {
            Player player = viewer.getPlayer();
            if (player == null) continue;
            ViewerData viewerData = viewer.getData(this);
            if (viewerData == null) continue;
            if (viewerData.getCanSee().get()) {
                sendPacket(player, packetContainer);
            }
        }
    }

    static void sendPacket(Player player, PacketContainer packet) {
        if (player.isOnline()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
