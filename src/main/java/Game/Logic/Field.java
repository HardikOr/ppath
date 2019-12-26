package Game.Logic;

import Game.Utilities.Utils;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static Game.Logic.Field.CELL.*;

public class Field {
    public enum CELL {
        CLEAR,
        OBSTICLE
    }

    public class LinePath {
        public List<Vector2i> getPath() {
            return path;
        }

        List<Vector2i> path;
        Graph.Vertex begin;
        Graph.Vertex end;

        public LinePath(List<Vector2i> path, Graph.Vertex begin, Graph.Vertex end) {
            this.path = new LinkedList<>(path);
            this.begin = begin;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LinePath path1 = (LinePath) o;
            return path.equals(path1.path) &&
                    begin.equals(path1.begin) &&
                    end.equals(path1.end);
        }
    }

    public class Zone {
        private int id = zid++;
        private CELL[][] cells;
        private int[][] debugCells;
        private int posX;
        private int posY;

        private Set<Graph.Vertex> exits = new HashSet<>();
        private Map<Graph.Vertex, Set<LinePath>> exitsPath = new HashMap<>();

        Zone(int posX, int posY){
            cells = new CELL[Utils.zoneSize][Utils.zoneSize];
            debugCells = new int[Utils.zoneSize][Utils.zoneSize];
            this.posX = posX;
            this.posY = posY;
        }

        public int getId() {
            return id;
        }
        public int getPosX() { return posX; }
        public int getPosY() { return posY; }

        public CELL getCell(int x, int y) {
            return cells[y][x];
        }
        public CELL getCellBAC(int x, int y) {
            return cells[y % Utils.zoneSize][x % Utils.zoneSize];
        }

        public int[][] getDebugCells() {
            return debugCells;
        }
    }

    private static int zid = 1;

    private static Zone[][] zones;
    private static Graph graph;

    public static Graph getGraph() {
        return graph;
    }

    public static Zone getZone(int zx, int zy) {
        return zones[zy][zx];
    }
    public static Zone getZoneBAC(int zx, int zy) { return zones[zy / Utils.zoneSize][zx / Utils.zoneSize]; }
    public static CELL getCellBAC(int x, int y) { return getZoneBAC(x, y).getCellBAC(x, y); }

    public static void addLinePath(Set<LinePath> set, LinePath newLP) {
        for (LinePath lp : set) {
            if (lp.equals(newLP))
                return;
        }
        set.add(newLP);
    }

    public Field() {
        zones = new Zone[Utils.yZones][Utils.xZones];
        graph = new Graph();

        readFromFile();
        prepareExits();

        calcPaths();
        prepareDebugPaths();

//        debugPath(generateGraphPath(
//                graph.getVertex(new Vector2i(7, 2), new Vector2i(8, 2)),
////                graph.getVertex(new Vector2i(3, 24), new Vector2i(3, 23))
//                graph.getVertex(new Vector2i(47, 27), new Vector2i(48, 27))
////                graph.getVertex(new Vector2i(15, 11), new Vector2i(16, 11))
//        ));

//        debugPath(generateCellPath(
//            new Vector2i(1, 2),
//            new Vector2i(3, 26)
//        ));
        System.out.println("asd");
//        System.out.println("ISPOS: " + graph.inV(new Vector2i(1, 2)));
//        for (int y = 0; y < yZones; y++) {
//            for (int x = 0; x < xZones; x++) {
//                for (Graph.Vertex vertex : zones[y][x].exits) {
//                    if (vertex.getPosA().equals(new Vector2i(1, 2)) || vertex.getPosB().equals(new Vector2i(1, 2)))
//                        System.out.println("ISPOS IN ZONES: true");
//                }
//            }
//        }
    }
    private void prepareDebugPaths() {
        for (int zy = 0; zy < Utils.yZones; zy++) {
            for (int zx = 0; zx < Utils.xZones; zx++) {
                Zone zone = zones[zy][zx];

                for (Set<Field.LinePath> set : zone.exitsPath.values()) {
                    for (Field.LinePath lP : set) {
                        Vector2i p1 = lP.getPath().get(0);

                        for (int i = 1; i < lP.getPath().size(); i++) {
                            Vector2i p2 = lP.getPath().get(i);

                            int lineX = p1.x - p2.x;
                            int lineY = p1.y - p2.y;

                            if (lineX == 0) for (int k = 0; k <= Math.abs(lineY); k++) {
                                zone.debugCells[p2.y % Utils.zoneSize + Integer.signum(lineY) * k][p2.x % Utils.zoneSize] = 1;
                            }
                            else for (int k = 0; k <= Math.abs(lineX); k++) {
                                zone.debugCells[p2.y % Utils.zoneSize][p2.x % Utils.zoneSize + Integer.signum(lineX) * k] = 1;
                            }

                            p1 = p2;
                        }
                    }
                }
            }
        }
    }

    public void debugPath(List<Vector2i> list) {
        if (!list.isEmpty()) {
            for (int x = 0; x < Utils.yZones; x++) {
                for (int y = 0; y < Utils.yZones; y++) {
                    for (int dx = 0; dx < Utils.zoneSize; dx++) {
                        for (int dy = 0; dy < Utils.zoneSize; dy++) {
                            zones[y][x].debugCells[dy][dx] = 0;
                        }
                    }
                }
            }

            Vector2i p1 = list.get(0);

            for (int i = 1; i < list.size(); i++) {
                Vector2i p2 = list.get(i);

                int lineX = p1.x - p2.x;
                int lineY = p1.y - p2.y;

                if (lineX == 0) for (int k = 0; k <= Math.abs(lineY); k++) {
                    int newY = p2.y + Integer.signum(lineY) * k;
                    zones[newY / Utils.zoneSize][p2.x / Utils.zoneSize].debugCells[newY % Utils.zoneSize][p2.x % Utils.zoneSize] = 1;
                }
                else for (int k = 0; k <= Math.abs(lineX); k++) {
                    int newX = p2.x + Integer.signum(lineX) * k;
                    zones[p2.y / Utils.zoneSize][newX / Utils.zoneSize].debugCells[p2.y % Utils.zoneSize][newX % Utils.zoneSize] = 1;
                }

                p1 = p2;
            }
        }
    }

    public List<Vector2i> generateGraphPath(Graph.Vertex start, Graph.Vertex end) {
        Deque<Graph.Vertex> path = graph.findShortPath(start, end);
        List<Vector2i> list = new LinkedList<>();

        Graph.Vertex a = path.removeLast();

        while (!path.isEmpty()) {
            Graph.Vertex b = path.removeLast();

            int zone = Graph.Vertex.getMutualZone(a, b);
            Set<LinePath> aaa = zones[a.getPosByZone(zone).y / Utils.zoneSize][a.getPosByZone(zone).x / Utils.zoneSize].exitsPath.get(a);
            for (LinePath lP : aaa) {
                if (lP.end.equals(b)) {
                    list.addAll(lP.path);
                    break;
                }
            }

            a = b;
        }

        return list;
    }

    public List<Vector2i> generateCellPath(Vector2i start, Vector2i exit) {
        List<Vector2i> ans = new LinkedList<>();

        if (zones[start.y / Utils.zoneSize][start.x / Utils.zoneSize].id == zones[exit.y / Utils.zoneSize][exit.x / Utils.zoneSize].id) {
            ans.addAll(generateStartCellPathInSingleZone(start, exit));
        } else {
            Graph.Vertex s = generateStartCellPath(start, ans);

            List<Vector2i> ans2 = new LinkedList<>();
            Graph.Vertex e = generateStartCellPath(exit, ans2);
//            Collections.reverse(ans2);

            ans.addAll(generateGraphPath(s, e));
            ans.addAll(ans2);
        }

        return ans;
    }

    public static Vector2i doubleToInt(Vector2d vector2d) {
        return new Vector2i((int) vector2d.x, (int) vector2d.y);
    }

    public static Vector2i getCellFromMousePos(Vector2d mousePos) {
        Vector2d convert = new Vector2d(mousePos);

        convert.x += 1;
        convert.y -= 1;

        convert.x = convert.x / 2 * Utils.xCells;
        convert.y = convert.y / 2 * Utils.yCells;

        return new Vector2i((int) convert.x, (int) -convert.y);
    }

    public static Vector2i getCellFromPos(Vector2d pos) {
        Vector2d convert = new Vector2d(pos);

        convert.x = convert.x + (double) Utils.W / 2;
        convert.y = convert.y - (double) Utils.H / 2;

        convert.x = convert.x / 2 * Utils.xCells;
        convert.y = convert.y / 2 * Utils.yCells;

        return new Vector2i((int) convert.x, (int) -convert.y);
    }

    private List<Vector2i> generateStartCellPathInSingleZone(Vector2i start, Vector2i exit) {
        return findPathInTable(generatePathTableBAC(start.x % Utils.zoneSize, start.y % Utils.zoneSize), exit);
    }

    private Graph.Vertex generateStartCellPath(Vector2i start, List<Vector2i> path) {
        int zy = start.y / Utils.zoneSize;
        int zx = start.x / Utils.zoneSize;
        int zoneId = zones[zy][zx].id;
        Map<Graph.Vertex, Integer> map = new HashMap<>();

        int[][] mas = generatePathTableBAC(start.x, start.y);

        for (Graph.Vertex i : zones[zy][zx].exits) {
            Vector2i posI = i.getPosByZone(zoneId);

            int sy = zy * Utils.zoneSize;
            int sx = zx * Utils.zoneSize;

            for (int y = 0; y < Utils.zoneSize; y++) {
                if (posI.equals(sx, sy + y)) {
                    map.put(i, mas[y][0] - 2);
                }
                if (posI.equals(sx + Utils.zoneSize - 1, sy + y)) {
                    map.put(i, mas[y][Utils.zoneSize - 1] - 2);
                }
            }
//            System.out.println("asd");
            for (int x = 1; x < Utils.zoneSize - 1; x++) {
                if (posI.equals(sx + x, sy)) {
                    map.put(i, mas[0][x] - 2);
                }
                if (posI.equals(sx + x, sy + Utils.zoneSize - 1)) {
                    map.put(i, mas[Utils.zoneSize - 1][x] - 2);
                }
            }
        }

        Graph.Vertex ans = Collections.min(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        path.addAll(findPathInTable(mas, new Vector2i(ans.getPosByZone(zoneId))));
//        System.out.println("ISPOS: " + zoneId + " " + graph.inV(new Vector2i(1, 2)));
        return ans;
    }

    private void addVE(Zone zone, int x, int y, int len, int numAdd, int addX, int addY) {
        if (len != 0) {
            int xx = x + (numAdd - 1 - len / 2) * Math.abs(addY);
            int yy = y + (numAdd - 1 - len / 2) * Math.abs(addX);

            Vector2i v1 = new Vector2i(xx, yy);
            Vector2i v2 = new Vector2i(xx + addX, yy + addY);

            Graph.Vertex V = graph.getOrDefaultVertex(v1, Utils.getZoneId(v1), v2, Utils.getZoneId(v2));

            graph.addVertex(V);
            zone.exits.add(V);
        }
    }

    private void checkAndAdd(Zone zone, int addX, int addY) {
        int modeX = Math.abs(addY);
        int modeY = Math.abs(addX);
        int xx = zone.getPosX() * Utils.zoneSize + (addX == 1 ? Utils.zoneSize - 1 : 0);
        int yy = zone.getPosY() * Utils.zoneSize + (addY == 1 ? Utils.zoneSize - 1 : 0);

        int len = 0;
        for (int num = 0; num < Utils.zoneSize; num++) {
            int x = xx + modeX * num;
            int y = yy + modeY * num;

            if (getCellBAC(x, y) == CLEAR && getCellBAC(x + addX, y + addY) == CLEAR) {
                len++;
            } else {
                addVE(zone, x, y, len, 0, addX, addY);
                len = 0;
            }
        }
        addVE(zone, xx, yy, len, Utils.zoneSize, addX, addY);
    }

    private void prepareExits() {
        for (int zy = 0; zy < Utils.yZones; zy++) {
            for (int zx = 0; zx < Utils.xZones; zx++) {
                Zone zone = zones[zy][zx];
//                if (zy == 3 && zx == 6) {
//                    System.out.println("sdd");
//                }
                if (zx > 0) checkAndAdd(zone, -1, 0);
                if (zy > 0) checkAndAdd(zone, 0, -1);
                if (zx < Utils.xZones - 1) checkAndAdd(zone, 1, 0);
                if (zy < Utils.yZones - 1) checkAndAdd(zone, 0, 1);
            }
        }
    }

    private List<Vector2i> findPathInTable(int[][] mas, Vector2i oldPos) {
        Vector2i pos = new Vector2i(oldPos);
        List<Vector2i> path = new LinkedList<>();

        int num;
        int prev = -1;
        while ((num = getMasBAC(mas, pos.x, pos.y) - 1) != 0) {
            if (pos.x % Utils.zoneSize > 0 && getMasBAC(mas, pos.x - 1, pos.y) == num) {
                if (prev != 0) path.add(new Vector2i(pos));
                prev = 0;
                pos.x--;
            } else if (pos.y % Utils.zoneSize > 0 && getMasBAC(mas, pos.x, pos.y - 1) == num) {
                if (prev != 1) path.add(new Vector2i(pos));
                prev = 1;
                pos.y--;
            } else if (pos.x % Utils.zoneSize < Utils.zoneSize - 1 && getMasBAC(mas, pos.x + 1, pos.y) == num) {
                if (prev != 2) path.add(new Vector2i(pos));
                prev = 2;
                pos.x++;
            } else if (pos.y % Utils.zoneSize < Utils.zoneSize - 1 && getMasBAC(mas, pos.x, pos.y + 1) == num) {
                if (prev != 3) path.add(new Vector2i(pos));
                prev = 3;
                pos.y++;
            }
        }
        path.add(new Vector2i(pos));

        return path;
    }

    private static int getMasBAC(int[][] mas, int x, int y) {
        return mas[y % Utils.zoneSize][x % Utils.zoneSize];
    }
    private static void setMasBAC(int[][] mas, int x, int y, int num) {
        mas[y % Utils.zoneSize][x % Utils.zoneSize] = num;
    }

    private void masCheckAndAddBAC(Deque<Vector2i> deq, int[][] mas, int x, int y, int num) {
        if (getCellBAC(x, y) == CLEAR && getMasBAC(mas, x % Utils.zoneSize, y % Utils.zoneSize) == 0) {
            setMasBAC(mas, x, y, num);
            deq.addFirst(new Vector2i(x, y));
        }
    }

    private int[][] generatePathTableBAC(int zx, int zy) {
        int[][] mas = new int[Utils.zoneSize][Utils.zoneSize]; // default 0

        Deque<Vector2i> deq = new ArrayDeque<>();
        masCheckAndAddBAC(deq, mas, zx, zy, 1);

        while (!deq.isEmpty()) {
            Vector2i pos = deq.removeLast();
            int num = getMasBAC(mas, pos.x, pos.y) + 1;
            if (pos.x % Utils.zoneSize > 0) masCheckAndAddBAC(deq, mas, pos.x - 1, pos.y, num);
            if (pos.y % Utils.zoneSize > 0) masCheckAndAddBAC(deq, mas, pos.x, pos.y - 1, num);
            if (pos.x % Utils.zoneSize < Utils.zoneSize - 1) masCheckAndAddBAC(deq, mas, pos.x + 1, pos.y, num);
            if (pos.y % Utils.zoneSize < Utils.zoneSize - 1) masCheckAndAddBAC(deq, mas, pos.x, pos.y + 1, num);
        }

        return mas;
    }

    private void tracePath(Graph.Vertex start, Graph.Vertex end, int[][] mas, Zone zone) {
        List<Vector2i> path = findPathInTable(mas, end.getPosByZone(zone.id));

        Set<LinePath> set1 = zone.exitsPath.getOrDefault(end, new HashSet<>());
        addLinePath(set1, new LinePath(path, end, start));
        zone.exitsPath.put(end, set1);

        Collections.reverse(path);
        Set<LinePath> set2 = zone.exitsPath.getOrDefault(start, new HashSet<>());
        addLinePath(set2, new LinePath(path, start, end));
        zone.exitsPath.put(start, set2);
    }

    private void calcPaths() {
        for (int zy = 0; zy < Utils.yZones; zy++) {
            for (int zx = 0; zx < Utils.xZones; zx++) {
                Zone zone = zones[zy][zx];
                List<Graph.Vertex> list = new ArrayList<>(zone.exits);

                if (!list.isEmpty()) for (Graph.Vertex v : list) zone.exitsPath.put(v, new HashSet<>());

                while (list.size() > 1) {
                    Graph.Vertex start = list.remove(0);
                    int[][] mas = generatePathTableBAC(start.getPosByZone(zone.id).x, start.getPosByZone(zone.id).y);

//                    if (zx == 1 && zy == 0) {
//                        System.out.println("ZODEID: " + zone.id + " SX: " + start.getPosByZone(zone.id).x +
//                                " SY: " + start.getPosByZone(zone.id).y);
//                        for (int y = 0; y < Utils.zoneSize; y++) {
//                            StringBuilder str = new StringBuilder("");
//                            for (int x = 0; x < Utils.zoneSize; x++) {
//                                str.append(String.format("%2d ", mas[y][x]));
//                            }
//                            System.out.println(str + "\n");
//                        }
//                        System.out.println("\n");
//                    }

                    for (Graph.Vertex i : list) {
                        int num = getMasBAC(mas, i.getPosByZone(zone.id).x, i.getPosByZone(zone.id).y);
                        if (num != 0) {
                            graph.addConnection(start, i, num - 1);
                            tracePath(start, i, mas, zone);
                        }
                    }
                }
            }
        }
    }

    private void readFromFile() {
        try {
            int line = 0;
            BufferedReader bf = new BufferedReader(new FileReader("src\\main\\resources\\maps\\map1.txt"));
            String str;
            while ((str = bf.readLine()) != null) {
                String[] a = str.split("\t");
                for (int x = 0; x < Utils.xCells; x++) {
                    if (getZoneBAC(x, line) == null) zones[line / Utils.zoneSize][x / Utils.zoneSize] =
                            new Zone(x / Utils.zoneSize, line / Utils.zoneSize);

                    getZoneBAC(x, line).cells[line % Utils.zoneSize][x % Utils.zoneSize] =
                        a[x].equals("0") ? CLEAR : OBSTICLE;
                }
                line++;
            }
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}