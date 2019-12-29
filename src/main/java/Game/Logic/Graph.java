package Game.Logic;

import org.joml.Vector2i;

import java.util.*;

public class Graph {
    public static class Vertex {
        private Vector2i posA;
        private Vector2i posB;
        private int zoneA;
        private int zoneB;
        private Map<Vertex, Field.LinePath> paths = new HashMap<>();

        public void setPath(Vertex vertex, Field.LinePath paths) {
            this.paths.put(vertex, paths);
        }

        public Field.LinePath getPath(Vertex vertex) {
            return paths.get(vertex);
        }

        public boolean hasNotPaths() {
            return paths.isEmpty();
        }

        public Map<Vertex, Field.LinePath> getPaths() {
            return paths;
        }

        Vertex(Vector2i pos, int zone) {
            this.posA = pos;
            this.posB = pos;
            this.zoneA = zone;
            this.zoneB = zone;
        }

        Vertex(Vector2i posA, int zoneA, Vector2i posB, int zoneB) {
            this.posA = posA;
            this.zoneA = zoneA;
            this.posB = posB;
            this.zoneB = zoneB;
        }

        int getMutualZone(Vertex vertex) {
            if (this.zoneA == vertex.zoneA || this.zoneA == vertex.zoneB)
                return this.zoneA;
            if (this.zoneB == vertex.zoneA || this.zoneB == vertex.zoneB)
                return this.zoneB;
            throw new NoSuchElementException();
        }

        Vector2i getPosByZone(int zone) {
            if (zone == zoneA) return posA;
            if (zone == zoneB) return posB;
            throw new NoSuchElementException();
        }

        Vector2i getPosByDifZone(int zone) {
            if (zone == zoneA) return posB;
            if (zone == zoneB) return posA;
            throw new NoSuchElementException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vertex vertex = (Vertex) o;
            return (zoneA == vertex.zoneA && zoneB == vertex.zoneB && posA.equals(vertex.posA) && posB.equals(vertex.posB)
                    || (zoneB == vertex.zoneA && zoneA == vertex.zoneB && posB.equals(vertex.posA) && posA.equals(vertex.posB)));
        }
    }

    static class VertexAdd implements Comparable {
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

        Edge(Edge edge) {
            this.weight = edge.weight;
            this.begin = edge.begin;
            this.end = edge.end;
        }
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    private Set<Vertex> vertices;
    private Map<Vertex, Set<Edge>> connections;

    Graph() {
        vertices = new HashSet<>();
        connections = new HashMap<>();
    }

    Graph(Graph graph) {
        vertices = new HashSet<>(graph.vertices);
        connections = new HashMap<>();

        for (Map.Entry<Vertex, Set<Edge>> set : graph.connections.entrySet()) {
            Set<Edge> newSet = new HashSet<>();
            set.getValue().forEach(it -> newSet.add(new Edge(it)));
            this.connections.put(set.getKey(), newSet);
        }
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

    private int getConWeight(Vertex a, Vertex b) {
        Set<Edge> edges = connections.get(a);

        for (Edge edge : edges) {
            if (edge.end.equals(b))
                return edge.weight;
        }
        return -1;
    }

    void addVertex(Vertex v) {
        vertices.add(v);
    }
    Vertex getVertexOrDefault(Vector2i pos, int zoneId) {
        for (Vertex i : vertices) {
            if (i.posA.equals(pos) || i.posB.equals(pos))
                return i;
        }
        return new Vertex(pos, zoneId);
    }

    void addConnection(Vertex start, Vertex end, int weight) {
        addOneWayConnection(start, end, weight);
        addOneWayConnection(end, start, weight);
    }

    private void addOneWayConnection(Vertex start, Vertex end, int weight) {
        Set<Edge> set = connections.getOrDefault(start, new HashSet<>());
        set.add(new Edge(weight, start, end));
        connections.put(start, set);
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
        for (Vertex i : this.vertices) {
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
