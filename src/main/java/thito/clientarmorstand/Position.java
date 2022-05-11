package thito.clientarmorstand;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.UUID;

public final class Position {
    public static UUID NO_WORLD = new UUID(-1, -1);
    private final UUID worldId;
    private final double x, y, z;
    private final float pitch, yaw;
    
    public static Position fromLocation(Location location) {
        World world = location.getWorld();
        return new Position(world == null ? NO_WORLD : world.getUID(), 
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getPitch(),
                location.getYaw());
    }
    
    public static Position fromVector(World world, Vector vector) {
        return new Position(world == null ? NO_WORLD : world.getUID(),
                vector.getX(),
                vector.getY(),
                vector.getZ(),
                0, 0);
    }

    public Position(UUID worldId, double x, double y, double z, float pitch, float yaw) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
    
    public Position add(Position position) {
        return new Position(worldId, x + position.x, y + position.y, z + position.z, pitch, yaw);
    }
    
    public Position add(Pose pose) {
        return new Position(worldId, x + pose.getX(), y + pose.getY(), z + pose.getZ(), pitch, yaw);
    }
    
    public Position add(double x, double y, double z) {
        return new Position(worldId, this.x + x, this.y + y, this.z + z, pitch, yaw);
    }

    public Position add(Location location) {
        return new Position(worldId, x + location.getX(), y + location.getY(), z + location.getZ(), pitch, yaw);
    }
    
    public Position add(Vector vector) {
        return new Position(worldId, x + vector.getX(), y + vector.getY(), z + vector.getZ(), pitch, yaw);
    }

    public Position subtract(Position position) {
        return new Position(worldId, x - position.x, y - position.y, z - position.z, pitch, yaw);
    }

    public Position subtract(Pose pose) {
        return new Position(worldId, x - pose.getX(), y - pose.getY(), z - pose.getZ(), pitch, yaw);
    }

    public Position subtract(double x, double y, double z) {
        return new Position(worldId, this.x - x, this.y - y, this.z - z, pitch, yaw);
    }

    public Position subtract(Location location) {
        return new Position(worldId, x - location.getX(), y - location.getY(), z - location.getZ(), pitch, yaw);
    }

    public Position subtract(Vector vector) {
        return new Position(worldId, x - vector.getX(), y - vector.getY(), z - vector.getZ(), pitch, yaw);
    }
    
    public Position setX(double x) {
        return new Position(worldId, x, y, z, pitch, yaw);
    }

    public Position setY(double y) {
        return new Position(worldId, x, y, z, pitch, yaw);
    }

    public Position setZ(double z) {
        return new Position(worldId, x, y, z, pitch, yaw);
    }

    public double distance(Position other) {
        return other.getWorldId().equals(worldId) ?
                Math.sqrt(Math.pow(x - other.x, 2) +
                        Math.pow(y - other.y, 2) +
                        Math.pow(z - other.z, 2)) : Double.MAX_VALUE;
    }

    public Position setPitch(float pitch) {
        return new Position(worldId, x, y, z, pitch, yaw);
    }

    public Position setYaw(float yaw) {
        return new Position(worldId, x, y, z, pitch, yaw);
    }

    public Position setWorldId(UUID worldId) {
        return new Position(worldId, x, y, z, pitch, yaw);
    }

    public UUID getWorldId() {
        return worldId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return Double.compare(position.x, x) == 0 && Double.compare(position.y, y) == 0 && Double.compare(position.z, z) == 0 && Float.compare(position.pitch, pitch) == 0 && Float.compare(position.yaw, yaw) == 0 && worldId.equals(position.worldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldId, x, y, z, pitch, yaw);
    }
}
