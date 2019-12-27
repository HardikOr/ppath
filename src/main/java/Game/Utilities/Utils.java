package Game.Utilities;

import Game.Logic.Entity;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static final int W = 1400;
    public static final int H = 800;

    public static final int xCells = W / 25; // 56
    public static final int yCells = H / 25; // 32
    public static final double xSize = (double) W / xCells;
    public static final double ySize = (double) H / yCells;

    public static final int xZones = xCells / 8; // 7
    public static final int yZones = yCells / 8; // 4
    public static final int zoneSize = 8;

    public static class Color {
        public static final Map<Entity.STATE, double[]> cellColor = new HashMap<Entity.STATE, double[]>() {{
            put(Entity.STATE.CHILL, new double[]{83 / 255d, 98 / 255d, 196 / 255d, 1});
            put(Entity.STATE.PLAYER, new double[]{223 / 255d, 167 / 255d, 53 / 255d, 1});
        }};
        public static final double[] mouseBorder = new double[] {34 / 255d, 114 / 255d, 42 / 255d, 1};
        public static final double[] entityPath = new double[] {200 / 255d, 50 / 255d, 50 / 255d, 1};
        public static final double[] mouseCell = new double[] {129 / 255d, 216 / 255d, 187 / 255d, 1};
    }

    public static int coordVBC(int z, int add) {
        return z * zoneSize + add;
    }

    public static int getZoneId(Vector2i pos) {
        return 1 + (pos.y / zoneSize) * xZones + pos.x / zoneSize;
    }
    public static int getZoneId(int x, int y) {
        return (y / zoneSize) * xZones + x / zoneSize;
    }

    public static Vector2d motoFromCell(Vector2i pos) {
        return new Vector2d( (double) (pos.x - xCells / 2) / xCells, (double) -(pos.y - yCells / 2) / yCells);
    }

    public static Vector2i cellFromMOTO(Vector2d pos) {
        return cellFromMOTO(pos.x, pos.y);
    }
    public static Vector2i cellFromMOTO(double x, double y) { // MOTO - minus onr to one
        return new Vector2i((int) ((x + 1) / 2 * xCells), (int) -((y - 1) / 2 * yCells));
    }

    public static Vector2i cellFromPos(Vector2d pos) {
        return cellFromPos(pos.x, pos.y);
    }
    public static Vector2i cellFromPos(double x, double y) {
        return cellFromMOTO(x / W * 2, y / H * 2);
    }
    public static Vector2d posFromCell(Vector2i pos) {
        Vector2d a = motoFromCell(pos);

        a.x = a.x * W + 12.5;
        a.y = a.y * H - 12.5;

        return a;
    }
    public static Vector2d posFromMOTO(Vector2d pos) {
        return new Vector2d(pos.x * W / 2, pos.y * H / 2);
    }
}
