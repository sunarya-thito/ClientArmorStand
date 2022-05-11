package thito.clientarmorstand;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import thito.clientarmorstand.property.NonNullObservable;
import thito.clientarmorstand.property.Observable;

import static thito.clientarmorstand.ClientArmorStand.sendPacket;

public final class ViewerData {
    private final Viewer viewer;
    private final ClientArmorStand clientArmorStand;
    private final Observable<Boolean> canSee = new NonNullObservable<>(false);
    public ViewerData(Viewer viewer, ClientArmorStand clientArmorStand) {
        this.viewer = viewer;
        this.clientArmorStand = clientArmorStand;
        canSee.addListener((obs, o, v) -> {
            Player player = viewer.getPlayer();
            if (player == null) return;
            if (v) {
                sendPacket(player, clientArmorStand.createSpawnPacket());
                ArmorStandMeta armorStandMeta = clientArmorStand.getMeta().get();
                PacketContainer metadataPacket = armorStandMeta.createPacket(clientArmorStand);
                sendPacket(player, metadataPacket);
                PacketContainer equipmentPacket = armorStandMeta.createEquipmentPacket(clientArmorStand, ArmorStandVersion.Slot.HELMET, armorStandMeta.getHelmet().get());
                sendPacket(player, equipmentPacket);
                equipmentPacket = armorStandMeta.createEquipmentPacket(clientArmorStand, ArmorStandVersion.Slot.CHESTPLATE, armorStandMeta.getChestplate().get());
                sendPacket(player, equipmentPacket);
                equipmentPacket = armorStandMeta.createEquipmentPacket(clientArmorStand, ArmorStandVersion.Slot.LEGGINGS, armorStandMeta.getLeggings().get());
                sendPacket(player, equipmentPacket);
                equipmentPacket = armorStandMeta.createEquipmentPacket(clientArmorStand, ArmorStandVersion.Slot.BOOTS, armorStandMeta.getBoots().get());
                sendPacket(player, equipmentPacket);
            } else {
                sendPacket(player, clientArmorStand.createDestroyPacket());
            }
        });
    }

    public Observable<Boolean> getCanSee() {
        return canSee;
    }

    public ClientArmorStand getClientArmorStand() {
        return clientArmorStand;
    }

    public Viewer getViewer() {
        return viewer;
    }

    void computeCanSee() {
        canSee.set(clientArmorStand.getLocation().getOptionally()
                .map(loc -> {
                    Player player = viewer.getPlayer();
                    if (player == null) return false;
                    Location location = player.getLocation();
                    return !player.getWorld().getUID().equals(loc.getWorldId()) && loc.distance(Position.fromLocation(location)) <= clientArmorStand.getViewDistance();
                }).orElse(false));
    }
}
