// Starter code for Postman Tour
package vdm180000;

import vdm180000.Graph.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Find a minimum weight postman tour that goes through every edge of g at least once

public class Postman {
    MinCostFlow minflow;
    Graph g;

    public Postman(Graph g) {
        this.g = g;
        HashMap<Edge, Integer> capacity = new HashMap<>();
        HashMap<Edge, Integer> cost = new HashMap<>();

        Graph graph2 = new Graph(g.n + 2, true);
        int i = 1;
        Edge edge;
        for (Vertex u : g) {
            for (Edge e : g.adj(u).outEdges) {
                Vertex v = e.otherEnd(u);
                edge = graph2.addEdge(graph2.getVertex(u), graph2.getVertex(v), e.getWeight(), e.getName());
                capacity.put(edge, Integer.MAX_VALUE);
                cost.put(edge, edge.getWeight());
                i++;
            }
        }

        for (Vertex u : g) {
            int remaining = u.inDegree() - u.outDegree();
            if (remaining > 0) {
                edge = graph2.addEdge(graph2.getVertex(g.n + 1), graph2.getVertex(u), 0, i);
                capacity.put(edge, remaining);
                cost.put(edge, 0);
            } else if (remaining < 0) {
                edge = graph2.addEdge(graph2.getVertex(u), graph2.getVertex(g.n + 2), 0, i);
                capacity.put(edge, -remaining);
                cost.put(edge, 0);
            }
            i++;
        }

        this.minflow = new MinCostFlow(graph2, graph2.getVertex(g.n + 1), graph2.getVertex(g.n + 2), capacity, cost);
        this.minflow.costScalingMinCostFlow(0);
    }

    public Postman(Graph g, Vertex startVertex) {
    }
    
    // Get a postman tour
    public List<Edge> getTour() {
	    List<Edge> postman_tour = new LinkedList<>();
        Map<Edge, Integer> flow = this.minflow.edge_flow;
        for (Edge e : flow.keySet()) {
            for (int i = 0; i < flow.get(e); i++) {
                g.addEdge(e.from, e.to, e.weight, e.name);
            }
        }
        Vertex start = g.getVertex(1);
        Euler euler = new Euler(g, start);
        // Boolean isEuler = euler.isEulerian();
        List<Vertex> tour = euler.findEulerTour();
        for (int i = 0; i < tour.size() - 1; i++) {
            Vertex u = tour.get(i);
            for (Edge e : g.adj(u).outEdges) {
                Vertex v = e.toVertex();
                if (v.equals(tour.get(i + 1))) {
                    postman_tour.add(e);
                }
            }
        }
        return postman_tour;
    }

    // Find length of postman tour
    public long postmanTour() {
	    long len = 0;
        for (Vertex u : g) {
            for (Edge e : g.adj(u).outEdges) {
                len += e.getWeight();
            }
        }
        return len + minflow.minimum_cost;
    }
}
