/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import java.util.ArrayList;
import java.util.List;

import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.PointsOfInterest;
import core.Coord;
import core.Settings;

/**
 * Map based movement model that uses Dijkstra's algorithm to find shortest
 * paths between two random map nodes and Points Of Interest
 */
public class WithoutMove extends MapBasedMovement implements
	SwitchableMovement {
    static Coord[] nodesLocation={new Coord(1,1),new Coord(750,750),new Coord(1500,1500),new Coord(2250,2250),new Coord(3000,3000)};
    static int taken=0;
    

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * @param settings The Settings object where the settings are read from
	 */
	public WithoutMove(Settings settings) {
		super(settings);
		
		
	}

	
	public Coord getInitialLocation() {
		return nodesLocation[taken++];
	}
	/**
	 * Copyconstructor.
	 * @param mbm The ShortestPathMapBasedMovement prototype to base
	 * the new object to
	 */
	protected WithoutMove(WithoutMove mbm) {
		super(mbm);
		
	}

	@Override
	public Path getPath() {
		return null;
	}

	@Override
	public WithoutMove replicate() {
		return new WithoutMove(this);
	}

}
