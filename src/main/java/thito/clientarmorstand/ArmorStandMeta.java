package thito.clientarmorstand;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;
import thito.clientarmorstand.property.NonNullObservable;
import thito.clientarmorstand.property.Observable;
import thito.clientarmorstand.property.Observer;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Consumer;

public final class ArmorStandMeta {
    private final Observable<BaseComponent> customName = new Observable<>();
    private final NonNullObservable<Boolean> isMarker = new NonNullObservable<>(false);
    private final NonNullObservable<Boolean> visible = new NonNullObservable<>(true);
    private final NonNullObservable<Boolean> customNameVisible = new NonNullObservable<>(false);
    private final NonNullObservable<Boolean> isSmall = new NonNullObservable<>(false);
    private final NonNullObservable<Boolean> hasBaseplate = new NonNullObservable<>(true);
    private final NonNullObservable<Boolean> hasArms = new NonNullObservable<>(true);
    private final Observable<ItemStack> helmet = new Observable<>();
    private final Observable<ItemStack> chestplate = new Observable<>();
    private final Observable<ItemStack> leggings = new Observable<>();
    private final Observable<ItemStack> boots = new Observable<>();
    private final Observable<ItemStack> itemInHand = new Observable<>();
    private final Observable<ItemStack> itemInOffHand = new Observable<>();
    private final NonNullObservable<Pose> headPose = new NonNullObservable<>(Pose.ZERO);
    private final NonNullObservable<Pose> bodyPose = new NonNullObservable<>(Pose.ZERO);
    private final NonNullObservable<Pose> rightArmPose = new NonNullObservable<>(Pose.ZERO);
    private final NonNullObservable<Pose> leftArmPose = new NonNullObservable<>(Pose.ZERO);
    private final NonNullObservable<Pose> rightLegPose = new NonNullObservable<>(Pose.ZERO);
    private final NonNullObservable<Pose> leftLegPose = new NonNullObservable<>(Pose.ZERO);

    public Observable<BaseComponent> getCustomName() {
        return customName;
    }

    public String getLegacyCustomName() {
        return TextComponent.toLegacyText(getCustomName().get());
    }

    public void setLegacyCustomName(String customName) {
        getCustomName().set(new TextComponent(TextComponent.fromLegacyText(customName)));
    }

    public NonNullObservable<Boolean> getIsMarker() {
        return isMarker;
    }

    public NonNullObservable<Boolean> getVisible() {
        return visible;
    }

    public NonNullObservable<Boolean> getCustomNameVisible() {
        return customNameVisible;
    }

    public NonNullObservable<Boolean> getIsSmall() {
        return isSmall;
    }

    public NonNullObservable<Boolean> getHasBaseplate() {
        return hasBaseplate;
    }

    public NonNullObservable<Boolean> getHasArms() {
        return hasArms;
    }

    public Observable<ItemStack> getHelmet() {
        return helmet;
    }

    public Observable<ItemStack> getChestplate() {
        return chestplate;
    }

    public Observable<ItemStack> getLeggings() {
        return leggings;
    }

    public Observable<ItemStack> getBoots() {
        return boots;
    }

    public Observable<ItemStack> getItemInHand() {
        return itemInHand;
    }

    public Observable<ItemStack> getItemInOffHand() {
        return itemInOffHand;
    }

    public NonNullObservable<Pose> getHeadPose() {
        return headPose;
    }

    public NonNullObservable<Pose> getBodyPose() {
        return bodyPose;
    }

    public NonNullObservable<Pose> getRightArmPose() {
        return rightArmPose;
    }

    public NonNullObservable<Pose> getLeftArmPose() {
        return leftArmPose;
    }

    public NonNullObservable<Pose> getRightLegPose() {
        return rightLegPose;
    }

    public NonNullObservable<Pose> getLeftLegPose() {
        return leftLegPose;
    }

    @Override
    public ArmorStandMeta clone() {
        ArmorStandMeta clone = new ArmorStandMeta();
        clone(visible, clone.visible);
        clone(customName, clone.customName);
        clone(customNameVisible, clone.customNameVisible);
        clone(hasArms, clone.hasArms);
        clone(isSmall, clone.isSmall);
        clone(hasBaseplate, clone.hasBaseplate);
        clone(isMarker, clone.isMarker);
        clone(headPose, clone.headPose);
        clone(bodyPose, clone.bodyPose);
        clone(leftArmPose, clone.leftArmPose);
        clone(rightArmPose, clone.rightArmPose);
        clone(leftLegPose, clone.leftLegPose);
        clone(rightLegPose, clone.rightLegPose);
        cloneItem(itemInHand, clone.itemInHand);
        cloneItem(itemInOffHand, clone.itemInOffHand);
        cloneItem(helmet, clone.helmet);
        cloneItem(chestplate, clone.chestplate);
        cloneItem(leggings, clone.leggings);
        cloneItem(boots, clone.boots);
        return clone;
    }

    private <V> void clone(Observable<V> a, Observable<V> b) {
        b.set(a.get());
    }

    private void cloneItem(Observable<ItemStack> a, Observable<ItemStack> b) {
        ItemStack itemStack = a.get();
        b.set(itemStack == null ? null : itemStack.clone());
    }

    void register(ClientArmorStand clientArmorStand) {
        clientArmorStand.broadcastPacket(createPacket(clientArmorStand));
        ArmorStandVersion handler = ClientArmorStand.version;
        visible.addListener(observe(clientArmorStand, visible -> {
            clientArmorStand.broadcastPacket(createPacketSingle(clientArmorStand, ArmorStandVersion.Meta.FLAGS, Byte.class, getEntityFlags()));
        }));
        customName.addListener(observe(clientArmorStand, customName -> {
            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packetContainer.getIntegers().write(0, clientArmorStand.getId());
            WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
            handler.writeCustomName(dataWatcher, customName);
            packetContainer.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
            clientArmorStand.broadcastPacket(packetContainer);
        }));
        customNameVisible.addListener(observe(clientArmorStand, customNameVisible -> {
            clientArmorStand.broadcastPacket(createPacketSingle(clientArmorStand, ArmorStandVersion.Meta.CUSTOM_NAME_VISIBLE, Boolean.class, customNameVisible));
        }));
        Observer<Boolean> observe = observe(clientArmorStand, any -> {
            clientArmorStand.broadcastPacket(createPacketSingle(clientArmorStand, ArmorStandVersion.Meta.FLAGS, Byte.class, getArmorStandFlags()));
        });

        isSmall.addListener(observe);
        hasArms.addListener(observe);
        hasBaseplate.addListener(observe);
        isMarker.addListener(observe);

        initPoseListener(clientArmorStand, headPose, ArmorStandVersion.Meta.HEAD_POSE);
        initPoseListener(clientArmorStand, bodyPose, ArmorStandVersion.Meta.BODY_POSE);
        initPoseListener(clientArmorStand, leftArmPose, ArmorStandVersion.Meta.LEFT_ARM_POSE);
        initPoseListener(clientArmorStand, rightLegPose, ArmorStandVersion.Meta.RIGHT_ARM_POSE);
        initPoseListener(clientArmorStand, leftLegPose, ArmorStandVersion.Meta.LEFT_LEG_POSE);
        initPoseListener(clientArmorStand, rightLegPose, ArmorStandVersion.Meta.RIGHT_LEG_POSE);

        initEquipmentListener(clientArmorStand, itemInHand, ArmorStandVersion.Slot.MAIN_HAND);
        initEquipmentListener(clientArmorStand, itemInOffHand, ArmorStandVersion.Slot.OFF_HAND);
        initEquipmentListener(clientArmorStand, helmet, ArmorStandVersion.Slot.HELMET);
        initEquipmentListener(clientArmorStand, chestplate, ArmorStandVersion.Slot.CHESTPLATE);
        initEquipmentListener(clientArmorStand, leggings, ArmorStandVersion.Slot.LEGGINGS);
        initEquipmentListener(clientArmorStand, boots, ArmorStandVersion.Slot.BOOTS);
    }

    private void initEquipmentListener(ClientArmorStand clientArmorStand, Observable<ItemStack> item, ArmorStandVersion.Slot slot) {
        item.addListener(observe(clientArmorStand, i -> {
            clientArmorStand.broadcastPacket(createEquipmentPacket(clientArmorStand, slot, i));
        }));
    }

    private void initPoseListener(ClientArmorStand clientArmorStand, Observable<Pose> pose, ArmorStandVersion.Meta type) {
        pose.addListener(observe(clientArmorStand, p -> {
            clientArmorStand.broadcastPacket(createPacketSingle(clientArmorStand, type, p));
        }));
    }

    <T> PacketContainer createPacketSingle(ClientArmorStand clientArmorStand, ArmorStandVersion.Meta meta, Class<T> type, T value) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, clientArmorStand.getId());
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        ClientArmorStand.version.writeRegistry(dataWatcher, type, meta, value);
        packetContainer.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        return packetContainer;
    }

    <T> PacketContainer createPacketSingle(ClientArmorStand clientArmorStand, ArmorStandVersion.Meta meta, Pose pose) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetContainer.getIntegers().write(0, clientArmorStand.getId());
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        ClientArmorStand.version.writeVector(dataWatcher, meta, pose);
        packetContainer.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        return packetContainer;
    }

    PacketContainer createPacket(ClientArmorStand clientArmorStand) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        ArmorStandVersion version = ClientArmorStand.version;

        packetContainer.getIntegers().write(0, clientArmorStand.getId());

        byte entityFlags = getEntityFlags();

        byte armorStandFlags = getArmorStandFlags();

        version.writeRegistry(dataWatcher, Byte.class, ArmorStandVersion.Meta.ENTITY_FLAGS, entityFlags);
        version.writeCustomName(dataWatcher, customName.get());
        version.writeRegistry(dataWatcher, Boolean.class, ArmorStandVersion.Meta.CUSTOM_NAME_VISIBLE, customNameVisible.get());
        version.writeRegistry(dataWatcher, Byte.class, ArmorStandVersion.Meta.FLAGS, armorStandFlags);
        version.writeVector(dataWatcher, ArmorStandVersion.Meta.HEAD_POSE, headPose.get());
        version.writeVector(dataWatcher, ArmorStandVersion.Meta.BODY_POSE, bodyPose.get());
        version.writeVector(dataWatcher, ArmorStandVersion.Meta.LEFT_ARM_POSE, leftArmPose.get());
        version.writeVector(dataWatcher, ArmorStandVersion.Meta.RIGHT_ARM_POSE, rightArmPose.get());
        version.writeVector(dataWatcher, ArmorStandVersion.Meta.LEFT_LEG_POSE, leftLegPose.get());
        version.writeVector(dataWatcher, ArmorStandVersion.Meta.RIGHT_LEG_POSE, rightLegPose.get());

        packetContainer.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        return packetContainer;
    }

    private byte getEntityFlags() {
        byte entityFlags = 0;
        if (!visible.get()) {
            entityFlags |= 0x20;
        }
        return entityFlags;
    }

    private byte getArmorStandFlags() {
        byte armorStandFlags = 0;
        if (isSmall.get()) {
            armorStandFlags |= 0x01;
        }
        if (hasArms.get()) {
            armorStandFlags |= 0x04;
        }
        if (!hasBaseplate.get()) {
            armorStandFlags |= 0x08;
        }
        if (isMarker.get()) {
            armorStandFlags |= 0x10;
        }
        return armorStandFlags;
    }

    PacketContainer createEquipmentPacket(ClientArmorStand clientArmorStand, ArmorStandVersion.Slot slot, ItemStack itemStack) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packetContainer.getIntegers().write(0, clientArmorStand.getId());
        ClientArmorStand.version.writeItemSlot(packetContainer, slot, itemStack);
        return packetContainer;
    }

    void unregister(ClientArmorStand clientArmorStand) {
        Observer<?> key = new ArmorStandObserver<>(clientArmorStand);
        customName.removeListener(key);
        isMarker.removeListener(key);
        visible.removeListener(key);
        customNameVisible.removeListener(key);
        isSmall.removeListener(key);
        hasBaseplate.removeListener(key);
        hasArms.removeListener(key);
        helmet.removeListener(key);
        chestplate.removeListener(key);
        leggings.removeListener(key);
        boots.removeListener(key);
        itemInHand.removeListener(key);
        itemInOffHand.removeListener(key);
        headPose.removeListener(key);
        bodyPose.removeListener(key);
        rightArmPose.removeListener(key);
        leftArmPose.removeListener(key);
        rightLegPose.removeListener(key);
        leftLegPose.removeListener(key);
    }

    static <V> Observer<V> observe(ClientArmorStand clientArmorStand, Consumer<V> observer) {
        return new ArmorStandObserver<V>(clientArmorStand) {
            @Override
            public void onChange(Observable<V> observable, V oldValue, V newValue) {
                observer.accept(newValue);
            }
        };
    }

    static class ArmorStandObserver<V> implements Observer<V> {
        private final WeakReference<ClientArmorStand> armorStand;

        public ArmorStandObserver(ClientArmorStand armorStand) {
            this.armorStand = new WeakReference<>(armorStand);
        }

        @Override
        public boolean shouldRemove() {
            return armorStand.get() == null;
        }

        @Override
        public void onChange(Observable<V> observable, V oldValue, V newValue) {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArmorStandObserver)) return false;
            ArmorStandObserver<?> that = (ArmorStandObserver<?>) o;
            ClientArmorStand clientArmorStand = armorStand.get();
            return clientArmorStand != null && clientArmorStand.equals(that.armorStand.get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(armorStand.get());
        }
    }
}
