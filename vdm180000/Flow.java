// Starter code for max flow
package vdm180000;

import vdm180000.Graph.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Flow {
    Graph g;
    int g_size;
    Vertex src;
    Vertex sink;
    int flow_excess[];
    int node_height[];
    int maxflow = 0;
    HashMap<Edge, Integer> flow_max;
    HashMap<Edge, Integer> edge_capacity;
    List<Vertex> activeList;    

    /**
     * Class constructor for initializing class attributes
     * @param g - Graph
     * @param s - Source node
     * @param t - Sink node
     * @param capacity
     */
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


    /**
     * Method for initializing attributes of the object
     */
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

    /**
     * Using Relabel to front
     * @return maximum flow from source to sink
     */

    public int preflowPush() {
        frontRelabel();
        maxflow = flow_excess[sink.getIndex()];
        return maxflow;
    }

    /**
     * Implemented front relabel for Pre-FLow Push algorithm
     */
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

    /**
     * Implented method to discharge excess flow
     * @param u
     */

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

    /**
     * Add method implemented pushes the excess flow out from vertex and make it 0
     * @param e
     * @param u
     * @param v
     */
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

    /**
     * Relabel method implemented to change the height of the vertex
     * @param u
     */
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

    /**
     * Flow Residual graph method returns if there is an edge which is reversed in residual graph
     * as compared to original graph
     * @param u
     * @param e
     * @return
     */
    private boolean flow_ResidualGraph(Vertex u, Edge e) {
        if(e.fromVertex().equals(u))
            return flow(e) < capacity(e);
        else
        	return flow(e) > 0;
    }

    
    /**
     * flow going through edge e
     *  */ 
    public int flow(Edge e) {
	    return flow_max.containsKey(e) ? flow_max.get(e) : 0;
    }

    /**
     * Capacity method to find the capacity of graph
     * @param e
     * @return
     */
    public int capacity(Edge e) {
	    return edge_capacity.containsKey(e) ? edge_capacity.get(e): 0;
    }

    /* After maxflow has been computed, this method can be called to
       get the "S"-side of the min-cut found by the algorithm
    */
    public Set<Vertex> minCutS() {
        Set<Vertex> cut = new HashSet<>();
        
        Queue<Vertex> queue = new LinkedList<Vertex>();

        queue.add(src);

        while ( !queue.isEmpty() ) {
            Vertex node = queue.poll();
            cut.add(node);
            for ( Edge e : g.adj(node).outEdges ) {
                if ( flow_max.get(e) < edge_capacity.get(e) ) {
                    Vertex v = e.otherEnd(node);
                    queue.add(v);
                }
            } 
        }
        
        return cut;
    }

    /* After maxflow has been computed, this method can be called to
       get the "T"-side of the min-cut found by the algorithm
    */
    public Set<Vertex> minCutT() {
        Set<Vertex> cutS = minCutS();
        Set<Vertex> cutT = new HashSet<>();

        for ( Vertex v : g) {
            if ( !cutS.contains(v) ) {
                cutT.add(v);
            }
        }

        return cutT;
    }
}
