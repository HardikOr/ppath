package Game.Logic;

import org.joml.Vector2i;
import org.lwjgl.system.CallbackI;

import java.rmi.MarshalledObject;
import java.util.*;

public class Graph {
    public static class Vertex {
        private static int id = sid++;
        private Vector2i posA;
        private Vector2i posB;
        private int zoneA;
        private int zoneB;

        Vertex() {}
        Vertex(Vector2i posA, int zoneA, Vector2i posB, int zoneB) {
            this.posA = posA;
            this.zoneA = zoneA;
            this.posB = posB;
            this.zoneB = zoneB;
        }
        Vertex(Vertex v) {
            this.posA = v.posA;
            this.zoneA = v.zoneA;
            this.posB = v.posB;
            this.zoneB = v.zoneB;
        }

        void setVertex(Vertex v) {
            this.posA = v.posA;
            this.zoneA = v.zoneA;
            this.posB = v.posB;
            this.zoneB = v.zoneB;
        }

        static int getMutualZone(Vertex a, Vertex b){
            return a.zoneB == b.zoneB || a.zoneB == b.zoneA ? a.zoneB : a.zoneA == b.zoneA || a.zoneA == b.zoneB ? a.zoneA : -1;
        }

        Vector2i getPosA() {
            return posA;
        }
        Vector2i getPosB() {
            return posB;
        }
        Vector2i getPosByZone(int zone) {
            if (zone == zoneA) return posA;
            if (zone == zoneB) return posB;
            throw new NoSuchElementException();
        }
        Vector2i getPosBySecondZone(int zone) {
            if (zone == zoneA) return posB;
            return posA;
        }

        int getId() { return id; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vertex vertex = (Vertex) o;
            return (zoneA == vertex.zoneA && zoneB == vertex.zoneB && posA.equals(vertex.posA) && posB.equals(vertex.posB)
                    || (zoneB == vertex.zoneA && zoneA == vertex.zoneB && posB.equals(vertex.posA) && posA.equals(vertex.posB)));
        }
    }

    class VertexAdd implements Comparable {
        private Vertex vertex;
        private int distance;
        private Vertex parent;

        VertexAdd(Vertex vertex, int distance, Vertex parent) {
            this.vertex = vertex;
            this.distance = distance;
            this.parent = parent;
        }

        @Override
        public int compareTo(Object o) {
            return distance - ((VertexAdd) o).distance;
        }
    }

    static class Edge {
        private int weight;
        Vertex begin;
        Vertex end;

        Edge(int weight, Vertex s, Vertex e) {
            this.weight = weight;
            begin = s;
            end = e;
        }

        public void setPoints(Vertex a, Vertex b) {
            begin = a;
            end = b;
        }
    }

    private static int sid = 0;

    private Set<Vertex> vertices;

    private Map<Vertex, Set<Edge>> connections;

    Graph() {
        vertices = new HashSet<>();
        connections = new HashMap<>();
    }

    private Set<Vertex> getNei(Vertex v) {
        Set<Vertex> ans = new HashSet<>();

        connections.get(v).forEach(it -> ans.add(it.end));

        return ans;
    }

    public Vertex getOrDefaultVertex(Vector2i first, int zoneFirst, Vector2i second, int zoneSecond) {
        Vertex ans = new Vertex(first, zoneFirst, second, zoneSecond);
        for (Vertex v : vertices) {
            if (v.equals(ans))
                return v;
        }
        return ans;
    }

    public Vertex getVertex(Vector2i a, Vector2i b) {
        for (Vertex v : vertices) {
            if (v.getPosA().equals(a) && v.getPosB().equals(b) || (v.getPosA().equals(b) && v.getPosB().equals(a)))
                return v;
        }
        throw new NoSuchElementException();
    }

    private int getConWeight(Vertex a, Vertex b) {
        Set<Edge> edges = connections.get(a);

        for (Edge edge : edges) {
            if (edge.begin.equals(b))
                return edge.weight;
            if (edge.end.equals(b))
                return edge.weight;
        }
        throw new NoSuchElementException();
    }

    void addVertex(Vertex v) {
        vertices.add(v);
    }

    void addConnection(Vertex start, Vertex end, int weight) {
        Set<Edge> aV = connections.getOrDefault(start, new HashSet<>());
        aV.add(new Edge(weight, start, end));
        connections.put(start, aV);

        Set<Edge> bV = connections.getOrDefault(end, new HashSet<>());
        bV.add(new Edge(weight, end, start));
        connections.put(end, bV);
    }

    public boolean inV(Vector2i coord) {
        for (Vertex i : vertices) {
            if (i.posA.equals(coord) || i.posB.equals(coord))
                return true;
        }
        return false;
    }

    public Deque<Vertex> findShortPath(Vertex begin, Vertex end) { // Dijkstra
        Map<Vertex, VertexAdd> map = new HashMap<>();
        for (Vertex i : vertices) {
            map.put(i, new VertexAdd(i, Integer.MAX_VALUE, null));
        }

        Set<Vertex> visited = new HashSet<>();
        VertexAdd beg = map.get(begin);
        beg.distance = 0;

        Queue<VertexAdd> queue = new PriorityQueue<>();
        queue.add(beg);

        while (!queue.isEmpty()) {
            VertexAdd current = queue.poll();
            Vertex currentVertex = current.vertex;
            visited.add(currentVertex);

            for (Vertex vertex : getNei(currentVertex)) {
                if (!visited.contains(vertex)) {
                    int weight = getConWeight(currentVertex, vertex);
                    if (weight >= 0) {
                        int newDistance = map.get(currentVertex).distance + weight;
                        if (map.get(vertex).distance > newDistance) {
                            VertexAdd newV = new VertexAdd(vertex, newDistance, currentVertex);
                            queue.add(newV);
                            map.put(vertex, newV);
                        }
                    }
                }
            }
        }

        Deque<Vertex> ans = new ArrayDeque<>();
        Vertex cur = end;
        while (cur != null) {
            ans.addLast(cur);
            cur = map.get(cur).parent;
        }

        return ans;
    }
}
