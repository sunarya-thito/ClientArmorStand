package thito.clientarmorstand;

import org.bukkit.Location;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Objects;

public final class Pose {
    public static final Pose ZERO = new Pose(0, 0, 0);

    public static Pose fromEulerAngle(EulerAngle eulerAngle) {
        return new Pose(eulerAngle.getX(), eulerAngle.getY(), eulerAngle.getZ());
    }

    public static Pose fromVector(Vector vector) {
        return new Pose(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Pose fromLocation(Location location) {
        return new Pose(location.getX(), location.getY(), location.getZ());
    }

    public static Pose create(double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0) return ZERO;
        return new Pose(x, y, z);
    }

    public static Pose fromPosition(Position position) {
        return new Pose(position.getX(), position.getY(), position.getZ());
    }

    private final double x, y, z;

    public Pose(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public Pose setX(double x) {
        return new Pose(x, y, z);
    }

    public Pose setY(double y) {
        return new Pose(x, y, z);
    }

    public Pose setZ(double z) {
        return new Pose(x, y, z);
    }

    public Pose setXYZ(double x, double y, double z) {
        return new Pose(x, y, z);
    }

    public Pose setXY(double x, double y) {
        return new Pose(x, y, z);
    }

    public Pose setXZ(double x, double z) {
        return new Pose(x, y, z);
    }

    public Pose setYZ(double y, double z) {
        return new Pose(x, y, z);
    }

    public Pose add(Pose other) {
        return new Pose(x + other.x, y + other.y, z + other.z);
    }

    public Pose add(double x, double y, double z) {
        return new Pose(this.x + x, this.y + y, this.z + z);
    }

    public Pose subtract(Pose other) {
        return new Pose(x - other.x, y - other.y, z - other.z);
    }

    public Pose subtract(double x, double y, double z) {
        return new Pose(this.x - x, this.y - y, this.z - z);
    }

    public double length() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    public Pose multiply(Pose other) {
        return new Pose(x * other.x, y * other.y, z * other.z);
    }

    public Pose multiply(double x, double y, double z) {
        return new Pose(this.x * x, this.y * y, this.z * z);
    }

    public Pose multiply(double m) {
        return new Pose(this.x * m, this.y * m, this.z * m);
    }

    public double distance(Pose other) {
        return subtract(other).length();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pose pose = (Pose) o;
        return Double.compare(pose.x, x) == 0 && Double.compare(pose.y, y) == 0 && Double.compare(pose.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
