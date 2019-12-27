package Game.Logic;

import Game.Utilities.Utils;
import org.joml.Vector2i;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static Game.Logic.Field.CELL.*;

public class Field {
    public enum CELL {
        CLEAR,
        OBSTACLE
    }

    public static class LinePath {
        List<Vector2i> path;
        Graph.Vertex begin;
        Graph.Vertex end;

        public LinePath(List<Vector2i> path, Graph.Vertex begin, Graph.Vertex end) {
            this.path = new ArrayList<>(path);
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

    public static class Zone {
        private int id = zid++;
        private CELL[][] cells;
        private int[][] debugCells;
        private int posX;
        private int posY;

        private Set<Graph.Vertex> exits = new HashSet<>();

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
    public static Zone getZoneById(int id) {
        return zones[(id - 1) / Utils.xZones][(id - 1) % Utils.xZones];
    }

    public static void fieldInit() {
        zones = new Zone[Utils.yZones][Utils.xZones];
        graph = new Graph();

        readFromFile();
        prepareExits();

        calcPaths(graph);
//        prepareDebugPaths(graph);

//        debugPath(generateGraphPath(graph,
//                graph.getVertex(new Vector2i(7, 2), new Vector2i(8, 2)),
////                graph.getVertex(new Vector2i(3, 24), new Vector2i(3, 23))
//                graph.getVertex(new Vector2i(47, 26), new Vector2i(48, 26))
////                graph.getVertex(new Vector2i(15, 11), new Vector2i(16, 11))
//        ));

//        debugPath(generateCellPath(
//            new Vector2i(2, 3),
//            new Vector2i(40, 3)
//        ));
    }

//    public static void debugPath(List<Vector2i> list) {
//        if (!list.isEmpty()) {
//            for (int x = 0; x < Utils.xZones; x++) {
//                for (int y = 0; y < Utils.yZones; y++) {
//                    for (int dx = 0; dx < Utils.zoneSize; dx++) {
//                        for (int dy = 0; dy < Utils.zoneSize; dy++) {
//                            zones[y][x].debugCells[dy][dx] = 0;
//                        }
//                    }
//                }
//            }
//
//            Vector2i p1 = list.get(0);
//
//            for (int i = 1; i < list.size(); i++) {
//                Vector2i p2 = list.get(i);
//
//                int lineX = p2.x - p1.x;
//                int lineY = p2.y - p1.y;
//
//                if (lineX == 0) for (int k = 0; k <= Math.abs(lineY); k++) {
//                    int newY = p1.y + Integer.signum(lineY) * k;
//                    getZoneBAC(p1.x, newY).debugCells[newY % Utils.zoneSize][p1.x % Utils.zoneSize] = 1;
//                }
//                else for (int k = 0; k <= Math.abs(lineX); k++) {
//                    int newX = p1.x + Integer.signum(lineX) * k;
//                    getZoneBAC(newX, p1.y).debugCells[p1.y % Utils.zoneSize][newX % Utils.zoneSize] = 1;
//                }
//
//                p1 = p2;
//            }
//        }
//    }

    private static List<Vector2i> generateGraphPath(Graph graph, Graph.Vertex start, Graph.Vertex end) {
        Deque<Graph.Vertex> path = graph.findShortPath(start, end);
        List<Vector2i> list = new ArrayList<>();

        Graph.Vertex a = path.removeLast();

        while (!path.isEmpty()) {
            Graph.Vertex b = path.removeLast();
            list.addAll(b.getPath(a).path);
            a = b;
        }

        return list;
    }

    private static void checkAndAddCellVertex(Graph graphCell, Graph.Vertex vertex, Vector2i vertexPos,
                                              List<Graph.Vertex> exits, int[][] mas, Zone zone) {
        if (!graphCell.inV(vertexPos)) {
            graphCell.addVertex(vertex);
            for (Graph.Vertex i : exits) {
                int num = getMasBAC(mas, i.getPosByZone(zone.id).x, i.getPosByZone(zone.id).y);
                if (num != 0) {
                    graphCell.addConnection(vertex, i, num - 1);
                    tracePath(vertex, i, mas, zone.id);
                }
            }
        }
    }

    public static List<Vector2i> generateCellPath(Vector2i start, Vector2i exit) {
        if (getCellBAC(start.x, start.y) == OBSTACLE || getCellBAC(exit.x, exit.y) == OBSTACLE)
            return new ArrayList<>();

        List<Vector2i> nearPath = new ArrayList<>();
        List<Vector2i> graphPath;

        int[][] masS = generatePathTableBAC(start.x, start.y);
        int[][] masE = generatePathTableBAC(exit.x, exit.y);

        Zone zoneS = getZoneBAC(start.x, start.y);
        Zone zoneE = getZoneBAC(exit.x, exit.y);

        if (zoneS.id == zoneE.id && getMasBAC(masS, exit.x, exit.y) != 0) {
            nearPath = findPathInTable(masS, exit);
            Collections.reverse(nearPath);
        }

        Graph graphCell = new Graph(graph);

        Graph.Vertex startV = graphCell.getVertexOrDefault(start, zoneS.id);
        checkAndAddCellVertex(graphCell, startV, start, new ArrayList<>(zoneS.exits), masS, zoneS);

        Graph.Vertex exitV = graphCell.getVertexOrDefault(exit, zoneE.id);
        checkAndAddCellVertex(graphCell, exitV, exit, new ArrayList<>(zoneE.exits), masE, zoneE);

        if (startV.hasNotPaths() || exitV.hasNotPaths())
            return new ArrayList<>();

        graphPath = generateGraphPath(graphCell, startV, exitV);

        if (nearPath.isEmpty()) return graphPath;
        if (nearPath.size() < graphPath.size()) return nearPath;
        return graphPath;
    }

    private static void addVE(Zone zone, int x, int y, int len, int numAdd, int addX, int addY) {
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

    private static void checkAndAdd(Zone zone, int addX, int addY) {
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

    private static void prepareExits() {
        for (int zy = 0; zy < Utils.yZones; zy++) {
            for (int zx = 0; zx < Utils.xZones; zx++) {
                Zone zone = zones[zy][zx];

                if (zx > 0) checkAndAdd(zone, -1, 0);
                if (zy > 0) checkAndAdd(zone, 0, -1);
                if (zx < Utils.xZones - 1) checkAndAdd(zone, 1, 0);
                if (zy < Utils.yZones - 1) checkAndAdd(zone, 0, 1);
            }
        }
    }

    private static List<Vector2i> findPathInTable(int[][] mas, Vector2i oldPos) {
        Vector2i pos = new Vector2i(oldPos);
        List<Vector2i> path = new ArrayList<>();

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

    private static void masCheckAndAddBAC(Deque<Vector2i> deq, int[][] mas, int x, int y, int num) {
        if (getCellBAC(x, y) == CLEAR && getMasBAC(mas, x % Utils.zoneSize, y % Utils.zoneSize) == 0) {
            setMasBAC(mas, x, y, num);
            deq.addFirst(new Vector2i(x, y));
        }
    }

    private static int[][] generatePathTableBAC(int zx, int zy) {
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

    private static void tracePath(Graph.Vertex start, Graph.Vertex end, int[][] mas, int zoneId) {
        List<Vector2i> path = findPathInTable(mas, end.getPosByZone(zoneId));

        start.setPath(end, new LinePath(path, end, start));
        Collections.reverse(path);
        end.setPath(start, new LinePath(path, start, end));
    }

    private static void calcPaths(Graph graph) {
        for (int zy = 0; zy < Utils.yZones; zy++) {
            for (int zx = 0; zx < Utils.xZones; zx++) {
                Zone zone = zones[zy][zx];
                List<Graph.Vertex> list = new ArrayList<>(zone.exits);

                while (list.size() > 1) {
                    Graph.Vertex start = list.remove(0);
                    int[][] mas = generatePathTableBAC(start.getPosByZone(zone.id).x, start.getPosByZone(zone.id).y);

                    for (Graph.Vertex i : list) {
                        int num = getMasBAC(mas, i.getPosByZone(zone.id).x, i.getPosByZone(zone.id).y);
                        if (num != 0) {
                            graph.addConnection(start, i, num - 1);
                            tracePath(start, i, mas, zone.id);
                        }
                    }
                }
            }
        }
    }

    private static void readFromFile() {
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
                        a[x].equals("0") ? CLEAR : OBSTACLE;
                }
                line++;
            }
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}