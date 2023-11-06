// COP 3503C Assignment 5
// This program is written by: Brendan Shrader

import java.util.*;

class Main {
	static Scanner in;
	static int c, r, s, l;

	public static void main(String[] args) {
		in = new Scanner(System.in);

		// Construct MonsterLand using input from user
		MonsterLand monsterLand = new MonsterLand(in);

		monsterLand.findTreasures();
	}
}

class MonsterLand {
	ArrayList<ArrayList<Road>> adj;
	int c, r, s, l;

	public MonsterLand(Scanner in) {
		// Set first three constants from user
		c = in.nextInt();
		r = in.nextInt();
		s = in.nextInt() - 1;

		// Set up adj list
		addRoads(in);

		// Set last constant from user
		l = in.nextInt();
	}

	// Initializes adj and adds all user-input roads to it
	private void addRoads(Scanner in) {
		int v1, v2, w;

		adj = new ArrayList<ArrayList<Road>>();

		// Initialize adj list to hold an ArrayList of roads at each index
		for (int i = 0; i < c; i++) {
			adj.add(new ArrayList<Road>());
		}

		for (int i = 0; i < r; i++) {
			// Get v1, v2, and w from user
			v1 = in.nextInt() - 1;
			v2 = in.nextInt() - 1;
			w = in.nextInt();

			// Roads are bidirectional, so add road between both cities
			(adj.get(v1)).add(new Road(v1, v2, w));
			(adj.get(v2)).add(new Road(v2, v1, w));
		}
	}

	public void findTreasures() {
		// Calculate shortest distance to each city
		int[] dist = dijkstra();

		int numCityTreasures = countCityTreasures(dist);

		int numRoadTreasures = countRoadTreasures(dist);

		System.out.println("In city: " + numCityTreasures);
		System.out.println("On the road: " + numRoadTreasures);
	}

	// Dijkstra's Algorithm from class; returns dist[] array
	private int[] dijkstra() {
		PriorityQueue<Node> minheap = new PriorityQueue<Node>();
		boolean[] done = new boolean[c];		// Indicates if city 'c' is done
		int[] dist = new int[c];				// Holds shortest distance to city 'c'
		int city, nextCity, weight;				// Temp variables
		Iterator<Road> it;
		Node node;								// Temp node polled from minheap
		Road road;								// Temp Road between city and nextCity

		// Initialize minheap, dist, and done data structures
		minheap.add(new Node(s, 0));
		Arrays.fill(dist, Integer.MAX_VALUE);
		dist[s] = 0;
		done[s] = true;

		while (!minheap.isEmpty()) {
			// Remove the city with the smallest distance
			node = minheap.poll();
			city = node.city;

			it = (adj.get(city)).iterator();

			// For each city connected to this city
			while (it.hasNext()) {
				road = it.next();
				nextCity = road.dest;
				weight = road.weight;

				// If the next city isn't already done and we can make a shorter path
				if (!done[nextCity] && (dist[city] + weight < dist[nextCity])) {
					dist[nextCity] = dist[city] + weight;
					minheap.offer(new Node(nextCity, dist[nextCity]));
				}
			}

			done[city] = true;
		}

		// Fill in remaining fields for each Road now that dijkstra is done
		for (int i = 0; i < c; i++) {
			for (Road r : adj.get(i)) {
				r.distSrc = dist[r.src];
				r.distDest = dist[r.dest];
			}
		}

		return dist;
	}

	private int countCityTreasures(int[] dist) {
		int numCityTreasures = 0;

		for (int i = 0; i < c; i++) {
			if (dist[i] == l) {
				numCityTreasures++;
			}
		}

		return numCityTreasures;
	}

	private int countRoadTreasures(int[] dist) {
		int v1, v2, t1, t2;
		int v1dist, v2dist, weight, numRoadTreasures = 0;

		// Keeps track if we have already checked an road for treasure
		boolean[][] checked = new boolean[c][c];

		// Go through all of the edges
		for (int i = 0; i < c; i++) {
			for (Road r : adj.get(i)) {
				// Changing to v1/v2 notation for convenience
				v1 = r.src;
				v2 = r.dest;

				// If this road has already been checked, continue
				if (checked[v1][v2]) {
					continue;
				}

				// Mark road as checked (marking both roads from v1->v2 and v2->v1)
				checked[v1][v2] = true;
				checked[v2][v1] = true;

				// Extract variables from Road object (for convenience)
				v1dist = r.distSrc;			// Shortest distance to v1
				v2dist = r.distDest;		// Shortest distance to v2
				weight = r.weight;			// Weight of road between v1, v2

				t1 = l - v1dist;	// How far a treasure is from v1
				t2 = l - v2dist;	// How far a treasure is from v2

				// If both cities are already too far, there cannot be treasure between them
				if (v1dist >= l && v2dist >= l) {
					continue;
				}

				// If the road is not long enough to have one of the treasures, skip this edge
				// (the other treasure can't be here, because it wouldn't be the shortest distance)
				if (t1 >= weight || t2 >= weight) {
					continue;
				}

				// Check if there can possibly be 2 treasures in this range
				if (0 < t1 && t1 < weight && 0 < t2 && t2 < weight) {
					// If their summed distance < weight, the two treasures do not cross over
					if (t1 + t2 < weight) {
						System.out.println("Adding 2 road treasures");
						numRoadTreasures += 2;
					}

					// If their summed distance == weight, there is only one treasure (they point
					// to the same location)
					if (t1 + t2 == weight) {
						System.out.println("Adding 1 road treasure");
						numRoadTreasures += 1;
					}

					// If their summed distance > weight, they cross over, so neither is at the
					// shortest distance to the capital
					if (t1 + t2 > weight) {
						continue;
					}
				}

				// Now, we know there is only one treasure
				else {
					System.out.println("Adding 1 road treasure");
					numRoadTreasures++;
				}
			}
		}

		return numRoadTreasures;
	}

	// Prints adj, c, r, s, and l
	public void print() {
		System.out.println("c = " + c + "\nr = " + r + "\ns = " + s + "\nl = " + l);
		for (int i = 0; i < c; i++) {
			Iterator it = (adj.get(i)).iterator();

			while (it.hasNext()) {
				System.out.println(it.next());
			}
		}
	}
}

class Road {
	int src, dest, weight;
	int distSrc;			// Shortest distance to source
	int distDest;			// Shortest distance to destination

	public Road(int src, int dest, int weight) {
		this.src = src;
		this.dest = dest;
		this.weight = weight;
		distSrc = -1;
		distDest = -1;
	}

	@Override
	public String toString() {
		return "[" + (src+1) + " -> " + (dest+1) + ", w = " + weight + ", dist to src = " 
		+ distSrc + ", dist to dest = " + distDest + "]";
	}
}

class Node implements Comparable<Node> {
	int city, dist;

	public Node(int city, int dist) {
		this.city = city;
		this.dist = dist;
	}

	public int compareTo(Node n2) {
		return dist - n2.dist;
	}
}