// Starter code for max flow
package vdm180000;

import vdm180000.Graph.*;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Flow {
    Graph g;
    int g_size;
    Vertex src;
    Vertex sink;
    int flow_excess[];
    int node_height[];
    HashMap<Edge, Integer> flow_max;
    HashMap<Edge, Integer> edge_capacity;
    List<Vertex> activeList;    

    public Flow(Graph g, Vertex s, Vertex t, HashMap<Edge, Integer> capacity) {
        this.g = g;
        g_size = g.n;
        this.src = s;
        this.sink = t;
        this.edge_capacity = capacity;
        flow_max = new HashMap<>();
        activeList = new LinkedList<>();
        flow_excess = new int[g_size];
        node_height = new int[g_size];
    }


    private void initialize() {
        Vertex[] vertices = g.getVertexArray();
        for( Vertex u : vertices ) {
            if(!src.equals(u) && !sink.equals(u)) {
            	activeList.add(u);
            }

            for(Edge e : g.adj(u).outEdges) {
                flow_max.put(e, 0);  // init flow is 0
            }

            flow_excess[u.getIndex()] = 0;
            node_height[u.getIndex()] = 0;
        }

        int src_idx = src.getIndex();
        node_height[src_idx] = g_size;
        for( Edge e : g.adj(src).outEdges ) {
            int curr_capacity = capacity(e);
            flow_max.put(e, curr_capacity);

            
            flow_excess[src_idx] -= curr_capacity;
            flow_excess[e.otherEnd(src).getIndex()] += curr_capacity;
        }
    }


    public int preflowPush() {
    	frontRelabel();
        return flow_excess[sink.getIndex()];
    }

    private void frontRelabel() {
        initialize();

        boolean finished = false;
        Iterator<Vertex> vertex_itr;

        while(!finished) {
            finished = true;

            vertex_itr = activeList.iterator();
            while(vertex_itr.hasNext()) {
                Vertex curr = vertex_itr.next();

                if(flow_excess[curr.getIndex()] == 0) {
                    continue;
                }

                int height_old = node_height[curr.getIndex()];
                discharge(curr);
                if(height_old != node_height[curr.getIndex()]) {
                    finished = false;
                    vertex_itr.remove();
                    activeList.add(0, curr);
                    break;
                }
            }
        }
    }


    private void discharge(Vertex u) {
        while(flow_excess[u.getIndex()] > 0) {
            List<Edge> in_edges = g.adj(u).inEdges; //reverse edges
            for(Edge e : in_edges) {
                Vertex v = e.fromVertex();
                if(flow_ResidualGraph(u, e) && node_height[u.getIndex()] == node_height[v.getIndex()]+1) {
                	add(e, u, v);
                    
                    if(flow_excess[u.getIndex()] == 0) {
                        return;
                    }
                }
            }
            
        	List<Edge> out_edges = g.adj(u).outEdges; //forward edges
            for(Edge e : out_edges) {
                Vertex v = e.otherEnd(u);
                if(flow_ResidualGraph(u, e) && node_height[u.getIndex()] == node_height[v.getIndex()] + 1) {
                	add(e, u, v);
                    
                    if(flow_excess[u.getIndex()] == 0) {
                        return; 
                    }
                }
            }
            
            relabel(u); // if push more flow not possible, relabel
        }
    }


    private void add(Edge e, Vertex u, Vertex v) {
        int delta = 0;
        if (e.fromVertex().equals(u)) {
        	delta = Math.min(flow_excess[u.getIndex()], (capacity(e) - flow(e)));
        	int t = flow(e) + delta;
            flow_max.put(e, t);
        }
        else {
        	delta = Math.min(flow_excess[u.getIndex()], flow(e));
        	int t = flow(e) - delta;
            flow_max.put(e, t);
        }
        
        flow_excess[v.getIndex()] = flow_excess[v.getIndex()] + delta;
        flow_excess[u.getIndex()] = flow_excess[u.getIndex()] - delta;
    }


    private void relabel(Vertex u) {
        int height_minimum = 3 * g_size;
        
        //for all (u,v) in Gf u.height<= v.height
        List<Edge> in_edges = g.adj(u).inEdges;
        for(Edge e : in_edges) {
        	int v_index = e.fromVertex().getIndex();
            int ht = node_height[v_index];
            if (flow_ResidualGraph(u, e) && ht < height_minimum)
            	height_minimum = ht;
        }
        
        List<Edge> out_edges = g.adj(u).outEdges;
        for(Edge e: out_edges) {
        	int v_index = e.toVertex().getIndex();
            int ht = node_height[v_index];
            if (flow_ResidualGraph(u, e) && ht < height_minimum)
            	height_minimum = ht;
        }
        
        height_minimum += 1;
        node_height[u.getIndex()] = height_minimum;
    }


    private boolean flow_ResidualGraph(Vertex u, Edge e) {
        if(e.fromVertex().equals(u))
            return flow(e) < capacity(e);
        else
        	return flow(e) > 0;
    }

    
    // flow going through edge e
    public int flow(Edge e) {
	    return flow_max.containsKey(e) ? flow_max.get(e) : 0;
    }

    // capacity of edge e
    public int capacity(Edge e) {
	    return edge_capacity.containsKey(e) ? edge_capacity.get(e): 0;
    }

    /* After maxflow has been computed, this method can be called to
       get the "S"-side of the min-cut found by the algorithm
    */
    public Set<Vertex> minCutS() {
	    return null;
    }

    /* After maxflow has been computed, this method can be called to
       get the "T"-side of the min-cut found by the algorithm
    */
    public Set<Vertex> minCutT() {
	    return null;
    }
}
