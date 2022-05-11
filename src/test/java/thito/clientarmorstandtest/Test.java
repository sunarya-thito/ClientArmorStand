package thito.clientarmorstandtest;

import thito.clientarmorstand.property.Observable;

public class Test {
    public static void main(String[] args) {
        Observable<Boolean> test = new Observable<>();
        Observable<String> test2 = new Observable<>("true");
        test.addListener((obs, o, v) -> System.out.println("1: "+o+" > "+v));
        test2.addListener((obs, o, v) -> System.out.println("2: "+o+" > "+v));
        test.bindBidirectionally(test2, String::valueOf, Boolean::valueOf);
        test2.set("edqwo");
        test.set(true);
//
//        ClientArmorStand clientArmorStand = new ClientArmorStand();
//        clientArmorStand.getLocation().get().add(Pose.ZERO);
//        clientArmorStand.getViewerSet().set(new OnlinePlayerList());
//        clientArmorStand.getViewerSet().set(new PlayerList());
//        clientArmorStand.getMeta().get().clone();
//        clientArmorStand.getMeta().get().getBodyPose().set(Pose.create(0, 0, 0));
//        clientArmorStand.getMeta().get().getHelmet().get();
    }
}
