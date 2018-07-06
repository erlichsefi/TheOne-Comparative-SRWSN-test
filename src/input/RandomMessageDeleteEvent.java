/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import core.Application;
import core.DTNHost;
import core.Message;
import core.World;
import report.DtsnAppReporter;
import routing.MessageRouter;

/**
 * External event for deleting a message.
 */

public class RandomMessageDeleteEvent extends MessageEvent {
	/** is the delete caused by a drop (not "normal" removing) */
	private boolean drop;
	private double Deletion_Prob=0.2;

	/**
	 * Creates a message delete event
	 * @param host Where to delete the message
	 * @param id ID of the message
	 * @param time Time when the message is deleted
	 */
	public RandomMessageDeleteEvent(int host, String id, double time,
			boolean drop,double _Deletion_Prob) {
		super(host, host, id, time);
		this.drop = drop;
		Deletion_Prob=_Deletion_Prob;
	}

	/**
	 * Deletes the message
	 */
	@Override
	public void processEvent(World world) {
		DTNHost host = world.getNodeByAddress(this.fromAddr);
		if (id.equals(StandardEventsReader.ALL_MESSAGES_ID)) {
			List<String> ids = new ArrayList<String>();
			for (Message m : host.getMessageCollection()) {
				ids.add(m.getId());
			}
			for (String nextId : ids) {
//				host.deleteMessage(nextId, drop);
	
			}
		}
		else if (id.equals(StandardEventsReader.LAST_MESSAGES_ID)) {
			double p=new Random(1).nextDouble();
			if (p>(1-Deletion_Prob)){
				String deletionId=null;
				for (Message m:host.getMessageCollection()) {
					deletionId=m.getId();
				}
				host.deleteMessage(deletionId, drop);
			}
		} else {
			host.deleteMessage(id, drop);
		}
	}

	
	
	@Override
	public String toString() {
		return super.toString() + " [" + fromAddr + "] DELETE";
	}



}
