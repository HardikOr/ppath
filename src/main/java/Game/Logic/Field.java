package Game.Logic;

import Game.Graphics.GameRenderer;
import org.joml.Vector2i;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static Game.Logic.Field.STATE.*;

public class Field {
    public enum STATE {
        CLEAR,
        OBSTICLE
    }

    public class Cell{
        private STATE state;
        private int zoneId;

        Cell(STATE state, int zone) {
            this.state = state;
            this.zoneId = zone;
        }

        Cell(STATE state) {
            this.state = state;
        }

        public int getZoneId() { return zoneId; }
        public STATE getState() { return state; }
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
        public int getId() {
            return id;
        }

        private int id = zid++;

        public Cell[][] getCells() {
            return cells;
        }
        public Cell getCell(int x, int y) {
            return cells[y][x];
        }

        private Cell[][] cells;

        public int[][] getDebugCells() {
            return debugCells;
        }

        private int[][] debugCells;

        public Set<Graph.Vertex> getExits() {
            return exits;
        }

        private Set<Graph.Vertex> exits = new HashSet<Graph.Vertex>();

        private Map<Graph.Vertex, Set<LinePath>> exitsPath = new HashMap<Graph.Vertex, Set<LinePath>>();

        Zone(){
            cells = new Cell[8][8];
            debugCells = new int[8][8];
        }
    }

    private static int zid = 1;

    private static int xCells = GameRenderer.getW() / 25; // 56 - 7*8
    private static int yCells = GameRenderer.getH() / 25; // 32 - 4*8
    public static int xZones = xCells / 8; // 7
    public static int yZones = yCells / 8; // 4
    public static int zoneSize = 8;

    private static double xSize = (double) GameRenderer.getW() / xCells;
    private static double ySize = (double) GameRenderer.getH() / yCells;

    private static Cell[][] field;
    private static Zone[][] zones;
    private static Graph graph;

    public static Graph getGraph() {
        return graph;
    }

    public static int getXCells() { return xCells; }
    public static int getYCells() { return yCells; }

    public static double getXSize() { return xSize; }
    public static double getYSize() { return ySize; }

    public static Zone getZone(int zx, int zy) {
        return zones[zy][zx];
    }
    public static void addLinePath(Set<LinePath> set, LinePath newLP) {
        for (LinePath lp : set) {
            if (lp.equals(newLP))
                return;
        }
        set.add(newLP);
    }

    public Field() {
        field = new Cell[yCells][xCells];
        zones = new Zone[yZones][xZones];
        graph = new Graph();

        readFromFile();
        prepareZones();
        prepareExits();
        calcPaths();
//        prepareDebugPaths();

//        debugPath(generateGraphPath(
//                graph.getVertex(new Vector2i(7, 2), new Vector2i(8, 2)),
////                graph.getVertex(new Vector2i(3, 24), new Vector2i(3, 23))
//                graph.getVertex(new Vector2i(47, 27), new Vector2i(48, 27))
////                graph.getVertex(new Vector2i(15, 11), new Vector2i(16, 11))
//        ));

        debugPath(generateCellPath(
            new Vector2i(1, 2),
            new Vector2i(3, 24)
        ));
    }
    private void prepareDebugPaths() {
        for (int zy = 0; zy < yZones; zy++) {
            for (int zx = 0; zx < xZones; zx++) {
                for (Set<Field.LinePath> set : zones[zy][zx].exitsPath.values()) {
                    for (Field.LinePath lP : set) {
                        Vector2i p1 = lP.getPath().get(0);

                        for (int i = 1; i < lP.getPath().size(); i++) {
                            Vector2i p2 = lP.getPath().get(i);

                            int lineX = p1.x % zoneSize - p2.x % zoneSize;
                            int lineY = p1.y % zoneSize - p2.y % zoneSize;

                            if (lineX == 0) for (int k = 0; k < Math.abs(lineY) + 1; k++) {
                                zones[zy][zx].debugCells[p2.y % zoneSize + Integer.signum(lineY) * k][p2.x % zoneSize] = 1;
                            }
                            else for (int k = 0; k < Math.abs(lineX) + 1; k++) {
                                zones[zy][zx].debugCells[p2.y % zoneSize][p2.x % zoneSize + Integer.signum(lineX) * k] = 1;
                            }

                            p1 = p2;
                        }
                    }
                }
            }
        }
    }

    private void debugPath(List<Vector2i> list) {
        if (!list.isEmpty()) {
            Vector2i p1 = list.get(0);

            for (int i = 1; i < list.size(); i++) {
                Vector2i p2 = list.get(i);

                int lineX = p1.x - p2.x;
                int lineY = p1.y - p2.y;

                if (lineX == 0) for (int k = 0; k <= Math.abs(lineY); k++) {
                    int newY = p2.y + Integer.signum(lineY) * k;
                    zones[newY / zoneSize][p2.x / zoneSize].debugCells[newY % zoneSize][p2.x % zoneSize] = 1;
                }
                else for (int k = 0; k <= Math.abs(lineX); k++) {
                    int newX = p2.x + Integer.signum(lineX) * k;
                    zones[p2.y / zoneSize][newX / zoneSize].debugCells[p2.y % zoneSize][newX % zoneSize] = 1;
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
            Set<LinePath> aaa = zones[a.getPosByZone(zone).y / zoneSize][a.getPosByZone(zone).x / zoneSize].exitsPath.get(a);
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

    private List<Vector2i> generateCellPath(Vector2i start, Vector2i exit) {
        List<Vector2i> ans = new LinkedList<>();

        if (zones[start.y / zoneSize][start.x / zoneSize].id == zones[exit.y / zoneSize][exit.x / zoneSize].id) {
            ans.addAll(generateStartCellPathInSingleZone(start, exit));
        } else {
            Graph.Vertex s = generateStartCellPath(start, ans);

            List<Vector2i> ans2 = new LinkedList<>();
            Graph.Vertex e = generateStartCellPath(exit, ans2);

            ans.addAll(generateGraphPath(s, e));
            ans.addAll(ans2);
        }

        return ans;
    }

    private List<Vector2i> generateStartCellPathInSingleZone(Vector2i start, Vector2i exit) {
        return findPathInTable(generatePathTable(start.x % zoneSize, start.y % zoneSize), exit);
    }

    private Graph.Vertex generateStartCellPath(Vector2i start, List<Vector2i> list) {
        int zy = start.y / zoneSize;
        int zx = start.x / zoneSize;
        int zoneId = zones[zy][zx].id;
        Map<Graph.Vertex, Integer> map = new HashMap<>();

        int[][] mas = generatePathTable(start.x, start.y);

        for (Graph.Vertex i : zones[zy][zx].exits) {
            Vector2i posI = i.getPosByZone(zoneId);

            int sy = zy * zoneSize;
            int sx = zx *zoneSize;

            for (int y = 0; y < zoneSize; y++) {
                if (posI.equals(sx, sy + y)) {
                    map.put(i, mas[y][0] - 2);
                }
                if (posI.equals(sx + zoneSize - 1, sy + y)) {
                    map.put(i, mas[y][zoneSize - 1] - 2);
                }
            }
            System.out.println("asd");
            for (int x = 1; x < zoneSize - 1; x++) {
                if (posI.equals(sx + x, sy * zoneSize)) {
                    map.put(i, mas[0][x] - 2);
                }
                if (posI.equals(sx + x, sy + zoneSize - 1)) {
                    map.put(i, mas[zoneSize - 1][x] - 2);
                }
            }
        }

        Graph.Vertex ans = Collections.min(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        list.addAll(findPathInTable(mas, ans.getPosByZone(zoneId)));
        return ans;
    }

    private void prepareZones() {
        for (int y = 0; y < yZones; y++) {
            for (int x = 0; x < xZones; x++) {
                zones[y][x] = new Zone();
            }
        }

        for (int zy = 0; zy < yZones; zy++) {
            for (int zx = 0; zx < xZones; zx++) {
                for (int y = 0; y < zoneSize; y++) {
                    for (int x = 0; x < zoneSize; x++) {
                        zones[zy][zx].cells[y][x] = field[zy * zoneSize + y][zx * zoneSize + x];
                        field[zy * zoneSize + y][zx * zoneSize + x].zoneId = zones[zy][zx].id;
                        zones[zy][zx].cells[y][x].zoneId = zones[zy][zx].id;
                    }
                }
            }
        }
    }

//    private static Graph.Vertex nearCell(int zx, int zy, int zxA, int zyA, int numX, int numY) {
//        return graph.getOrDefaultVertex(
//                new Vector2i(zx * zoneSize + numX, zy * zoneSize + numY),
//                zones[zy][zx].cells[numY + zy][numX].zoneId,
//                new Vector2i(zx * zoneSize + zxA + numX, zy * zoneSize + zyA + numY),
//                zones[zy + zyA][zx + zxA].cells[numY][numX + zoneSize - 1].zoneId
//        );
//    }

    private void prepareExits() {
        for (int zy = 0; zy < yZones; zy++) {
            for (int zx = 0; zx < xZones; zx++) {
                if (zx > 0) {
                    int len = 0;
                    for (int num = 0; num < zoneSize; num++) {
                        if (zones[zy][zx].cells[num][0].state == CLEAR
                                && zones[zy][zx - 1].cells[num][zoneSize - 1].state == CLEAR) {
                            len++;
                        } else {
                            if (len != 0) {
                                Graph.Vertex V = graph.getOrDefaultVertex( // zx - 1, zy, num by y
                                        new Vector2i(zx * zoneSize, zy * zoneSize + num - 1 - len / 2),
                                        zones[zy][zx].cells[num - 1 - len / 2][0].zoneId,
                                        new Vector2i(zx * zoneSize - 1, zy * zoneSize + num - 1 - len / 2),
                                        zones[zy][zx - 1].cells[num - 1 - len / 2][zoneSize - 1].zoneId
                                );

                                graph.addVertex(V);
                                zones[zy][zx].exits.add(V);
                            }
                            len = 0;
                        }
                        if (num == zoneSize - 1 && len != 0) {
                            Graph.Vertex V = graph.getOrDefaultVertex( // zx - 1, zy, num by y
                                    new Vector2i(zx * zoneSize, zy * zoneSize + num - len / 2),
                                    zones[zy][zx].cells[num - len / 2][0].zoneId,
                                    new Vector2i(zx * zoneSize - 1, zy * zoneSize + num - len / 2),
                                    zones[zy][zx - 1].cells[num - len / 2][zoneSize - 1].zoneId
                            );

                            graph.addVertex(V);
                            zones[zy][zx].exits.add(V);
                        }
                    }
                }
                if (zy > 0) {
                    int len = 0;
                    for (int num = 0; num < zoneSize; num++) {
                        if (zones[zy][zx].cells[0][num].state == CLEAR && zones[zy - 1][zx].cells[zoneSize - 1][num].state == CLEAR) {
                            len++;
                        } else {
                            if (len != 0) {
                                Graph.Vertex V = graph.getOrDefaultVertex( // zx, zy - 1, num by x
                                        new Vector2i(zx * zoneSize + num - 1 - len / 2, zy * zoneSize),
                                        zones[zy][zx].cells[0][num - 1 - len / 2].zoneId,
                                        new Vector2i(zx * zoneSize + num - 1 - len / 2, zy * zoneSize - 1),
                                        zones[zy - 1][zx].cells[zoneSize - 1][num - 1 - len / 2].zoneId
                                );

                                graph.addVertex(V);
                                zones[zy][zx].exits.add(V);
                            }
                            len = 0;
                        }
                        if (num == zoneSize - 1 && len != 0) {
                            Graph.Vertex V = graph.getOrDefaultVertex( // zx, zy - 1, num by x
                                    new Vector2i(zx * zoneSize + num - len / 2, zy * zoneSize),
                                    zones[zy][zx].cells[0][num - len / 2].zoneId,
                                    new Vector2i(zx * zoneSize + num - len / 2, zy * zoneSize - 1),
                                    zones[zy - 1][zx].cells[zoneSize - 1][num - len / 2].zoneId
                            );

                            graph.addVertex(V);
                            zones[zy][zx].exits.add(V);
                        }
                    }
                }
                if (zx < xZones - 1) {
                    int len = 0;
                    for (int num = 0; num < zoneSize; num++) {
                        if (zones[zy][zx].cells[num][zoneSize - 1].state == CLEAR && zones[zy][zx + 1].cells[num][0].state == CLEAR) {
                            len++;
                        } else {
                            if (len != 0) {
                                Graph.Vertex V = graph.getOrDefaultVertex( // zx + 1, zy, num by y
                                        new Vector2i((zx + 1) * zoneSize - 1, zy * zoneSize + num - 1 - len / 2),
                                        zones[zy][zx].cells[num - 1 - len / 2][zoneSize - 1].zoneId,
                                        new Vector2i((zx + 1) * zoneSize, zy * zoneSize + num - 1 - len / 2),
                                        zones[zy][zx + 1].cells[num - 1 - len / 2][0].zoneId
                                );

                                graph.addVertex(V);
                                zones[zy][zx].exits.add(V);
                            }
                            len = 0;
                        }
                        if (num == zoneSize - 1 && len != 0) {
                            Graph.Vertex V = graph.getOrDefaultVertex( // zx + 1, zy, num by y
                                    new Vector2i((zx + 1) * zoneSize - 1, zy * zoneSize + num - len / 2),
                                    zones[zy][zx].cells[num - len / 2][zoneSize - 1].zoneId,
                                    new Vector2i((zx + 1) * zoneSize, zy * zoneSize + num - len / 2),
                                    zones[zy][zx + 1].cells[num - len / 2][0].zoneId
                            );

                            graph.addVertex(V);
                            zones[zy][zx].exits.add(V);
                        }
                    }
                }
                if (zy < yZones - 1) {
                    int len = 0;
                    for (int num = 0; num < zoneSize; num++) {
                        if (zones[zy][zx].cells[zoneSize - 1][num].state == CLEAR && zones[zy + 1][zx].cells[0][num].state == CLEAR) {
                            len++;
                        } else {
                            if (len != 0) {
                                Graph.Vertex V = graph.getOrDefaultVertex( // zx, zy + 1, num by x
                                        new Vector2i(zx * zoneSize + num - 1 - len / 2, (zy + 1) * zoneSize - 1),
                                        zones[zy][zx].cells[zoneSize - 1][num - 1 - len / 2].zoneId,
                                        new Vector2i(zx * zoneSize + num - 1 - len / 2, (zy + 1) * zoneSize),
                                        zones[zy + 1][zx].cells[0][num - 1 - len / 2].zoneId
                                );

                                graph.addVertex(V);
                                zones[zy][zx].exits.add(V);
                            }
                            len = 0;
                        }
                        if (num == zoneSize - 1 && len != 0) {
                            Graph.Vertex V = graph.getOrDefaultVertex( // zx, zy + 1, num by x
                                    new Vector2i(zx * zoneSize + num - len / 2, (zy + 1) * zoneSize - 1),
                                    zones[zy][zx].cells[zoneSize - 1][num  - len / 2].zoneId,
                                    new Vector2i(zx * zoneSize + num - len / 2, (zy + 1) * zoneSize),
                                    zones[zy + 1][zx].cells[0][num - len / 2].zoneId
                            );

                            graph.addVertex(V);
                            zones[zy][zx].exits.add(V);
                        }
                    }
                }
            }
        }
    }

    private List<Vector2i> findPathInTable(int[][] mas, Vector2i pos) {
        List<Vector2i> path = new LinkedList<Vector2i>();

        int num = mas[pos.y % zoneSize][pos.x % zoneSize] - 1;
        int prev = -1;
        while (num != 0) {
            if (pos.x % zoneSize > 0 && mas[pos.y % zoneSize][(pos.x - 1) % zoneSize] == num) {
                if (prev != 0) { path.add(new Vector2i(pos)); }
                prev = 0;
                pos.x--;
            } else if (pos.y % zoneSize > 0 && mas[(pos.y - 1) % zoneSize][pos.x % zoneSize] == num) {
                if (prev != 1) { path.add(new Vector2i(pos)); }
                prev = 1;
                pos.y--;
            } else if (pos.x % zoneSize < zoneSize - 1 && mas[pos.y % zoneSize][(pos.x + 1) % zoneSize] == num) {
                if (prev != 2) { path.add(new Vector2i(pos)); }
                prev = 2;
                pos.x++;
            } else if (pos.y % zoneSize < zoneSize - 1 && mas[(pos.y + 1) % zoneSize][pos.x % zoneSize] == num) {
                if (prev != 3) { path.add(new Vector2i(pos)); }
                prev = 3;
                pos.y++;
            }

            num = mas[pos.y % zoneSize][pos.x % zoneSize] - 1;
        }
        path.add(new Vector2i(pos));

        return path;
    }

    private void tracePath(Graph.Vertex sV, Graph.Vertex eV, int[][] mas, int zx, int zy) {
        Vector2i pos = new Vector2i(eV.getPosByZone(zones[zy][zx].id));
        List<Vector2i> path = findPathInTable(mas, pos);

        Set<LinePath> set1 = zones[zy][zx].exitsPath.getOrDefault(eV, new HashSet<LinePath>());
        addLinePath(set1, new LinePath(path, eV, sV));
        zones[zy][zx].exitsPath.put(eV, set1);

        Collections.reverse(path);
        Set<LinePath> set2 = zones[zy][zx].exitsPath.getOrDefault(sV, new HashSet<LinePath>());
        addLinePath(set2, new LinePath(path, sV, eV));
        zones[zy][zx].exitsPath.put(sV, set2);
    }

    private int[][] generatePathTable(int zx, int zy) {
        int[][] mas = new int[zoneSize][zoneSize]; // default 0

        Deque<Vector2i> deq = new ArrayDeque<>();
        mas[zy % zoneSize][zx % zoneSize] = 1;
        deq.addFirst(new Vector2i(zx, zy));

        while (!deq.isEmpty()) {
            Vector2i pos = deq.removeLast();
            int num = mas[pos.y % zoneSize][pos.x % zoneSize] + 1;

            if (pos.x % zoneSize > 0 && field[pos.y][pos.x - 1].state == CLEAR && mas[pos.y % zoneSize][(pos.x - 1) % zoneSize] == 0) {
                mas[pos.y % zoneSize][(pos.x - 1) % zoneSize] = num;
                deq.addFirst(new Vector2i(pos.x - 1, pos.y));
            }
            if (pos.y % zoneSize > 0 && field[pos.y - 1][pos.x].state == CLEAR && mas[(pos.y - 1) % zoneSize][pos.x % zoneSize] == 0) {
                mas[(pos.y - 1) % zoneSize][pos.x % zoneSize] = num;
                deq.addFirst(new Vector2i(pos.x, pos.y - 1));
            }
            if (pos.x % zoneSize < zoneSize - 1 && field[pos.y][pos.x + 1].state == CLEAR && mas[pos.y % zoneSize][(pos.x + 1) % zoneSize] == 0) {
                mas[pos.y % zoneSize][(pos.x + 1) % zoneSize] = num;
                deq.addFirst(new Vector2i(pos.x + 1, pos.y));
            }
            if (pos.y % zoneSize < zoneSize - 1 && field[pos.y + 1][pos.x].state == CLEAR && mas[(pos.y + 1) % zoneSize][pos.x % zoneSize] == 0) {
                mas[(pos.y + 1) % zoneSize][pos.x % zoneSize] = num;
                deq.addFirst(new Vector2i(pos.x, pos.y + 1));
            }
        }

        return mas;
    }

    private void findWayLens(List<Graph.Vertex> list, Graph.Vertex start, int zx, int zy) {
        int zoneId = zones[zy][zx].id;
        int pX = start.getPosByZone(zoneId).x;
        int pY = start.getPosByZone(zoneId).y;

        int[][] mas = generatePathTable(pX, pY);

        int aPosX = zx * zoneSize;
        int aPosY = zy * zoneSize;

        for (Graph.Vertex i : list) {
            Vector2i posI = i.getPosByZone(zoneId);

            for (int y = 0; y < zoneSize; y++) {
                if (posI.equals(aPosX, aPosY + y)) {
                    graph.addConnection(start, i, mas[y][0] - 2);
                    tracePath(start, i, mas, zx, zy);
                }
                if (posI.equals(aPosX + zoneSize - 1, aPosY + y)) {
                    graph.addConnection(start, i, mas[y][zoneSize - 1] - 2);
                    tracePath(start, i, mas, zx, zy);
                }
            }
            for (int x = 1; x < zoneSize - 1; x++) {
                if (posI.equals(aPosX + x, aPosY)) {
                    graph.addConnection(start, i, mas[0][x] - 2);
                    tracePath(start, i, mas, zx, zy);
                }
                if (posI.equals(aPosX + x, aPosY + zoneSize - 1)) {
                    graph.addConnection(start, i, mas[zoneSize - 1][x] - 2);
                    tracePath(start, i, mas, zx, zy);
                }
            }
        }
    }

    private void calcPaths() {
        for (int zy = 0; zy < yZones; zy++) {
            for (int zx = 0; zx < xZones; zx++) {
                List<Graph.Vertex> list = new ArrayList<Graph.Vertex>(zones[zy][zx].exits);

                while (list.size() != 1) {
                    Graph.Vertex gV = list.remove(0);
                    findWayLens(list, gV, zx, zy);
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
                for (int x = 0; x < xCells; x++) {
                    if (a[x].equals("0"))
                        field[line][x] = new Cell(CLEAR);
                    else
                        field[line][x] = new Cell(OBSTICLE);
                }
                line++;
            }
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Cell getCell(int x, int y) {
        return field[y][x];
    }
}
