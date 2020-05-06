// Starter code for max flow
package vdm180000;

import vdm180000.Graph.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Flow {
    Vertex src;
    Vertex sink;
    int excessFlow[];
    int height[];
    HashMap<Edge, Integer> maxflow;
    HashMap<Edge, Integer> capacity;
    Graph g;
    List<Vertex> listVertex;
    

    public Flow(Graph g, Vertex s, Vertex t, HashMap<Edge, Integer> capacity) {
        this.g = g;
        this.src = s;
        this.sink = t;
        this.capacity = capacity;
        maxflow = new HashMap<>();
        listVertex = new LinkedList<>();
        excessFlow = new int[g.n];
        height = new int[g.n];
    }


    private void initialize() {
        Vertex[] vertices = g.getVertexArray();
        for( Vertex u : vertices ) {
            if(!src.equals(u) && !sink.equals(u)) {
                listVertex.add(u);
            }

            for( Edge e : g.adj(u).outEdges ) {
                maxflow.put(e, 0);
            }

            excessFlow[u.getIndex()] = 0;
            height[u.getIndex()] = 0;
        }

        int src_idx = src.getIndex();
        height[src_idx] = g.n;
        for( Edge e : g.adj(src).outEdges ) {
            int curr_capacity = capacity(e);
            maxflow.put(e, curr_capacity);

            
            excessFlow[src_idx] -= curr_capacity;
            excessFlow[e.otherEnd(src).getIndex()] += curr_capacity;
        }
    }


    public int preflowPush() {
	    relabelFront();
        return excessFlow[sink.getIndex()];
    }

    private void relabelFront() {
        initialize();

        Iterator<Vertex> vertex_itr;
        boolean finished = false;

        while(!finished) {
            finished = true;

            vertex_itr = listVertex.iterator();
            while(vertex_itr.hasNext()) {
                Vertex curr = vertex_itr.next();

                if(excessFlow[curr.getIndex()] == 0)
                    continue;

                int oldHeight = height[curr.getIndex()];
                discharge(curr);
                if(oldHeight != height[curr.getIndex()]) {
                    finished = false;
                    vertex_itr.remove();
                    listVertex.add(0, curr);
                    break;
                }
            }
        }
    }


    private void discharge(Vertex u) {
        while(excessFlow[u.getIndex()] > 0) {
            for( Edge e : g.adj(u).outEdges ) {
                Vertex v = e.otherEnd(u);
                if( ResidualGraphForFlow(e, u) && height[v.getIndex()] + 1 == height[u.getIndex()] ) {
                    push(e, u, v);
                    if(excessFlow[u.getIndex()] == 0) {
                        return;
                    }
                }
            }

            for( Edge e : g.adj(u).inEdges ) {
                Vertex v = e.fromVertex();
                if(ResidualGraphForFlow(e,u) && height[v.getIndex()]+1 == height[u.getIndex()]) {
                    push(e, u, v);
                    if(excessFlow[u.getIndex()] == 0) {
                        return;
                    }
                }
            }

            relabel(u);
        }
    }


    private void relabel(Vertex u) {
        int minHeight = 3*g.n;
        
        for( Edge e: g.adj(u).outEdges ) {
            int mheight = height[e.toVertex().getIndex()];
            if ( ResidualGraphForFlow(e, u) && mheight < minHeight ) {
                minHeight = mheight;
            }
        }

        for( Edge e : g.adj(u).inEdges ) {
            int mheight = height[e.fromVertex().getIndex()];
            if ( ResidualGraphForFlow(e, u) && mheight < minHeight ) {
                minHeight = mheight;
            }
        }
        
        height[u.getIndex()] = minHeight+1;
    }


    private boolean ResidualGraphForFlow(Edge e, Vertex u) {
        if( e.fromVertex().equals(u) ) {
            return flow(e) < capacity(e);
        }
        return flow(e) > 0;
    }


    private void push(Edge e, Vertex u, Vertex v) {
        int flowDelta = 0;
        if ( e.fromVertex().equals(u) ) {
            flowDelta = Math.min((capacity(e) - flow(e)), excessFlow[u.getIndex()]);
            maxflow.put(e, flow(e) + flowDelta);
        }
        else {
            flowDelta = Math.min(flow(e), excessFlow[u.getIndex()]);
            maxflow.put(e, flow(e) - flowDelta);
        }
        excessFlow[u.getIndex()] -= flowDelta;
        excessFlow[v.getIndex()] += flowDelta;
    }

    
    // flow going through edge e
    public int flow(Edge e) {
	    return maxflow.containsKey(e) ? maxflow.get(e) : 0;
    }

    // capacity of edge e
    public int capacity(Edge e) {
	    return capacity.containsKey(e) ? capacity.get(e): 0;
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
