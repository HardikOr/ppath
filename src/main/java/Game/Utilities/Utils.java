package Game.Utilities;

import Game.Logic.Entity;
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
    }

    public static int coordVBC(int z, int add) {
        return z * zoneSize + add;
    }

    public static int getZoneIdVBC(int zx, int addX, int zy, int addY) {
        return getZoneId(coordVBC(zx, addX), coordVBC(zy, addY));
    }

    public static int getZoneId(Vector2i pos) {
        return 1 + (pos.y / zoneSize) * xZones + pos.x / zoneSize;
    }
    public static int getZoneId(int x, int y) {
        return (y / zoneSize) * xZones + x / zoneSize;
    }
    public static Vector2i newVBC(int zx, int addX, int zy, int addY) { // new vector by coordinates
        return new Vector2i(coordVBC(zx, addX), coordVBC(zy, addY));
    }
    public static Vector2i vecFromAC(int x, int y) {
        return new Vector2i(x);
    }
}
