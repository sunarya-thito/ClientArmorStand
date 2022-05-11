package thito.clientarmorstand;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public final class ArmorStandVersion {
    public interface SpawnLocationWriter {
        void write(PacketContainer packetContainer, Position location);
    }
    public interface SpawnUniqueIdWriter {
        void write(PacketContainer packetContainer, UUID id);
    }
    public interface SpawnArmorStandTypeWriter {
        void write(PacketContainer packetContainer);
    }
    public interface TeleportLocationWriter {
        void write(PacketContainer packetContainer, Position location);
    }
    public interface RegistryWriter {
        <T> void write(WrappedDataWatcher watcher, Class<T> type, Meta meta, T value);
    }
    public interface VectorWriter {
        void write(WrappedDataWatcher watcher, Meta meta, Pose pose);
    }
    public interface DestroyEntityIdWriter {
        void write(PacketContainer packetContainer, int entityId);
    }
    public interface CustomNameWriter {
        void write(WrappedDataWatcher watcher, BaseComponent baseComponent);
    }
    public interface ItemSlotWriter {
        void write(PacketContainer packetContainer, Slot slot, ItemStack itemStack);
    }
    public interface MetaOrdinalDeterminer {
        int ordinal(Meta meta);
    }

    private SpawnLocationWriter spawnLocationWriter;
    private SpawnUniqueIdWriter spawnUniqueIdWriter;
    private SpawnArmorStandTypeWriter spawnArmorStandTypeWriter;
    private TeleportLocationWriter teleportLocationWriter;
    private RegistryWriter registryWriter;
    private VectorWriter vectorWriter;
    private DestroyEntityIdWriter destroyEntityIdWriter;
    private CustomNameWriter customNameWriter;
    private ItemSlotWriter itemSlotWriter;
    private MetaOrdinalDeterminer metaOrdinalDeterminer;

    static int floor(float f) {
        int i = (int) f;
        return f < (float) i ? i - 1 : i;
    }

    static int floor(double d) {
        int i = (int) d;
        return d < (double) i ? i - 1 : i;
    }

    public ArmorStandVersion() {
        StructureModifier<Object> spawnStructure = StructureCache.getStructure(PacketType.Play.Server.SPAWN_ENTITY);
        StructureModifier<Double> doubleSpawnStructure = spawnStructure.withType(Double.TYPE);
        if (doubleSpawnStructure.size() > 0) {
            spawnLocationWriter = (packetContainer, location) -> {
                packetContainer.getDoubles().write(0, location.getX())
                        .write(1, location.getY())
                        .write(2, location.getZ());
                packetContainer.getIntegers().write(4, floor(location.getPitch() * 256f / 360f))
                        .write(5, floor(location.getYaw() * 256f / 360f));
            };
        } else {
            spawnLocationWriter = (packetContainer, location) -> {
                packetContainer.getIntegers()
                        .write(1, floor(location.getX() * 32d))
                        .write(2, floor(location.getY() * 32d))
                        .write(3, floor(location.getZ() * 32d))
                        .write(7, floor(location.getPitch() * 256f / 360f))
                        .write(8, floor(location.getYaw() * 256f / 360f));
            };
        }
        if (spawnStructure.withType(UUID.class).size() > 0) {
            spawnUniqueIdWriter = (packetContainer, id) -> {
                packetContainer.getUUIDs().write(0, id);
            };
        } else {
            spawnUniqueIdWriter = (packetContainer, id) -> {};
        }
        if (spawnStructure.withType(MinecraftReflection.getEntityTypes(), BukkitConverters.getEntityTypeConverter()).size() > 0) {
            spawnArmorStandTypeWriter = packetContainer -> {
                packetContainer.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
            };
        } else {
            spawnArmorStandTypeWriter = packetContainer -> {
                packetContainer.getIntegers().write(packetContainer.getIntegers().size() - 2, 78);
            };
        }
        StructureModifier<Object> teleportStructure = StructureCache.getStructure(PacketType.Play.Server.ENTITY_TELEPORT);
        StructureModifier<Double> doubleTeleportStructure = teleportStructure.withType(Double.TYPE);
        if (doubleTeleportStructure.size() > 0) {
            teleportLocationWriter = (packetContainer, location) -> {
                packetContainer.getDoubles().write(0, location.getX())
                        .write(1, location.getY())
                        .write(2, location.getZ());
                packetContainer.getBytes().write(0, (byte) floor(location.getPitch() * 256f / 360f))
                        .write(1, (byte) floor(location.getYaw() * 256f / 360f));
            };
        } else {
            teleportLocationWriter = (packetContainer, location) -> {
                packetContainer.getIntegers()
                        .write(1, floor(location.getX() * 32d))
                        .write(2, floor(location.getY() * 32d))
                        .write(3, floor(location.getZ() * 32d));
                packetContainer.getBytes().write(0, (byte) floor(location.getPitch() * 256f / 360f))
                        .write(1, (byte) floor(location.getYaw() * 256f / 360f));
            };
        }
        Class<?> DataWatcherObject = MinecraftReflection.getDataWatcherObjectClass();
        boolean usesIChatBaseComponent = false;
        boolean usesOptional = false;
        if (DataWatcherObject != null) {
            int index = 0;
            for (Field field : MinecraftReflection.getEntityClass().getDeclaredFields()) {
                if (field.getType() == DataWatcherObject) {
                    if (index == 2) {
                        field.setAccessible(true);
                        Type handleType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        if (handleType instanceof ParameterizedType) {
                            handleType = ((ParameterizedType) handleType).getActualTypeArguments()[0];
                            usesOptional = true;
                        }
                        if (handleType instanceof Class) {
                            usesIChatBaseComponent = handleType == MinecraftReflection.getIChatBaseComponentClass();
                        }
                    } else if (index > 2) break;
                    index++;
                }
            }
        }
        if (MinecraftReflection.watcherObjectExists()) {
            if (usesOptional) {
                if (usesIChatBaseComponent) {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)),
                                Optional.ofNullable(baseComponent == null ? null :
                                        WrappedChatComponent.fromLegacyText(TextComponent.toLegacyText(baseComponent)))
                        );
                    };
                } else {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(String.class, true)),
                                Optional.ofNullable(baseComponent == null ? null : TextComponent.toLegacyText(baseComponent))
                        );
                    };
                }
            } else {
                if (usesIChatBaseComponent) {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer()),
                                WrappedChatComponent.fromLegacyText(baseComponent == null ? "" : TextComponent.toLegacyText(baseComponent))
                        );
                    };
                } else {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(String.class)),
                                baseComponent == null ? "" : TextComponent.toLegacyText(baseComponent)
                        );
                    };
                }
            }
            registryWriter = new RegistryWriter() {
                @Override
                public <T> void write(WrappedDataWatcher watcher, Class<T> type, Meta meta, T value) {
                    watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                            metaOrdinalDeterminer.ordinal(meta),
                            WrappedDataWatcher.Registry.get(type)
                    ), value);
                }
            };
            vectorWriter = (watcher, meta, pose) -> {
                watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                        metaOrdinalDeterminer.ordinal(meta),
                        WrappedDataWatcher.Registry.getVectorSerializer()
                ), new Vector3F(
                        (float) Math.toDegrees(pose.getX()),
                        (float) Math.toDegrees(pose.getY()),
                        (float) Math.toDegrees(pose.getZ())
                ));
            };
        } else {
            if (usesOptional) {
                if (usesIChatBaseComponent) {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                2,
                                Optional.ofNullable(baseComponent == null ? null :
                                        WrappedChatComponent.fromLegacyText(TextComponent.toLegacyText(baseComponent)))
                        );
                    };
                } else {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                2,
                                Optional.ofNullable(baseComponent == null ? null : TextComponent.toLegacyText(baseComponent))
                        );
                    };
                }
            } else {
                if (usesIChatBaseComponent) {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                2,
                                WrappedChatComponent.fromLegacyText(baseComponent == null ? "" : TextComponent.toLegacyText(baseComponent))
                        );
                    };
                } else {
                    customNameWriter = (watcher, baseComponent) -> {
                        watcher.setObject(
                                2,
                                baseComponent == null ? "" : TextComponent.toLegacyText(baseComponent)
                        );
                    };
                }
            }
            registryWriter = new RegistryWriter() {
                @Override
                public <T> void write(WrappedDataWatcher watcher, Class<T> type, Meta meta, T value) {
                    if (value instanceof Boolean) {
                        watcher.setObject(metaOrdinalDeterminer.ordinal(meta), (Boolean) value ? (byte) 1 : (byte) 0);
                    } else {
                        watcher.setObject(metaOrdinalDeterminer.ordinal(meta), value);
                    }
                }
            };
            vectorWriter = (watcher, meta, pose) -> {
                watcher.setObject(metaOrdinalDeterminer.ordinal(meta), new Vector3F(
                        (float) Math.toDegrees(pose.getX()),
                        (float) Math.toDegrees(pose.getY()),
                        (float) Math.toDegrees(pose.getZ())
                ));
            };
        }
        StructureModifier<Object> entityDestroyStructure = StructureCache.getStructure(PacketType.Play.Server.ENTITY_DESTROY);
        if (entityDestroyStructure.withType(int[].class).size() > 0) {
            destroyEntityIdWriter = (packetContainer, entityId) -> {
                packetContainer.getIntegerArrays().write(0, new int[] {entityId});
            };
        } else {
            destroyEntityIdWriter = (packetContainer, entityId) -> {
                packetContainer.getIntLists().write(0, Collections.singletonList(entityId));
            };
        }
        StructureModifier<Object> entityEquipmentStructure = StructureCache.getStructure(PacketType.Play.Server.ENTITY_EQUIPMENT);
        if (entityEquipmentStructure.withType(Integer.TYPE).size() == 2) {
            itemSlotWriter = (packetContainer, slot, itemStack) -> {
                packetContainer.getIntegers().write(1, slot.ordinal());
                packetContainer.getItemModifier().write(0, itemStack);
            };
        } else {
            itemSlotWriter = (packetContainer, slot, itemStack) -> {
                packetContainer.getItemSlots().write(0, slot.slot);
                packetContainer.getItemModifier().write(0, itemStack);
            };
        }
        int armorStandDataWatcherIndex = 10;
        Class<?> ArmorStand = FuzzyReflection.fromClass(MinecraftReflection.getCraftBukkitClass("entity.CraftArmorStand"))
                .getMethodByName("getHandle").getReturnType();
        for (Field field : ArmorStand.getDeclaredFields()) {
            if (field.getType() == DataWatcherObject) {
                field.setAccessible(true);
                WrappedDataWatcher.WrappedDataWatcherObject object;
                try {
                    object = new WrappedDataWatcher.WrappedDataWatcherObject(field.get(null));
                } catch (IllegalAccessException e) {
                    throw new UnsupportedOperationException("unsupported MC version", e);
                }
                armorStandDataWatcherIndex = object.getIndex();
                break;
            }
        }
        int index = armorStandDataWatcherIndex;
        metaOrdinalDeterminer = meta -> {
            switch (meta) {
                case ENTITY_FLAGS: return 0;
                case CUSTOM_NAME: return 2;
                case CUSTOM_NAME_VISIBLE: return 3;
                case FLAGS: return index;
                case HEAD_POSE: return index + 1;
                case BODY_POSE: return index + 2;
                case LEFT_ARM_POSE: return index + 3;
                case RIGHT_ARM_POSE: return index + 4;
                case LEFT_LEG_POSE: return index + 5;
                case RIGHT_LEG_POSE: return index + 6;
                default: throw new IllegalArgumentException("invalid meta");
            }
        };
    }

    public void writeTeleportLocation(PacketContainer packetContainer, Position location) {
        teleportLocationWriter.write(packetContainer, location);
    }

    public void writeSpawnLocation(PacketContainer packetContainer, Position location) {
        spawnLocationWriter.write(packetContainer, location);
    }
    public void writeSpawnUniqueId(PacketContainer packetContainer, UUID id) {
        spawnUniqueIdWriter.write(packetContainer, id);
    }
    public void writeSpawnArmorStandType(PacketContainer packetContainer) {
        spawnArmorStandTypeWriter.write(packetContainer);
    }
    public <T> void writeRegistry(WrappedDataWatcher watcher, Class<T> type, Meta meta, T value) {
        registryWriter.write(watcher, type, meta, value);
    }
    public void writeVector(WrappedDataWatcher watcher, Meta meta, Pose pose) {
        vectorWriter.write(watcher, meta, pose);
    }
    public void writeDestroyEntityId(PacketContainer packetContainer, int entityId) {
        destroyEntityIdWriter.write(packetContainer, entityId);
    }
    public void writeCustomName(WrappedDataWatcher watcher, BaseComponent baseComponent) {
        customNameWriter.write(watcher, baseComponent);
    }
    public void writeItemSlot(PacketContainer packetContainer, Slot slot, ItemStack itemStack) {
        itemSlotWriter.write(packetContainer, slot, itemStack);
    }
    enum Slot {
        MAIN_HAND(EnumWrappers.ItemSlot.MAINHAND),
        BOOTS(EnumWrappers.ItemSlot.FEET),
        LEGGINGS(EnumWrappers.ItemSlot.LEGS),
        CHESTPLATE(EnumWrappers.ItemSlot.CHEST),
        HELMET(EnumWrappers.ItemSlot.HEAD),
        OFF_HAND(EnumWrappers.ItemSlot.OFFHAND);
        EnumWrappers.ItemSlot slot;
        Slot(EnumWrappers.ItemSlot slot) {
            this.slot = slot;
        }
    }
    enum Meta {
        ENTITY_FLAGS, CUSTOM_NAME, CUSTOM_NAME_VISIBLE, FLAGS, HEAD_POSE,
        BODY_POSE, LEFT_ARM_POSE, RIGHT_ARM_POSE, LEFT_LEG_POSE, RIGHT_LEG_POSE
    }
}
