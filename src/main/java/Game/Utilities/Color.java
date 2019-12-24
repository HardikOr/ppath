package Game.Utilities;

import Game.Logic.Entity;
import Game.Logic.Field;

import java.util.HashMap;
import java.util.Map;

public class Color {
    public static final Map<Entity.STATE, double[]> cellColor = new HashMap<Entity.STATE, double[]>() {{
        put(Entity.STATE.STANDING, new double[]{83 / 255d, 98 / 255d, 196 / 255d, 1});
        put(Entity.STATE.MOVING, new double[]{212 / 255d, 53 / 255d, 223 / 255d, 1});
        put(Entity.STATE.PLAYERED, new double[]{223 / 255d, 167 / 255d, 53 / 255d, 1});
    }};
    public static final double[] mouseBorder = new double[] {34 / 255d, 114 / 255d, 42 / 255d, 1};
}
