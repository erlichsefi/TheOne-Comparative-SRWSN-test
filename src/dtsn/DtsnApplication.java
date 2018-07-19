/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package dtsn;


import java.util.ArrayList;
import java.util.Arrays;

import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.Message;
import core.SimClock;
import dtsn.tools.DtsnMessage;
import dtsn.tools.TimerObject;
import report.DtsnAppReporter;

/**
 * Simple ping application to demonstrate the application support. The
 * application can be configured to send pings with a fixed interval or to only
 * answer to pings it receives. When the application receives a ping it sends
 * a pong message in response.
 *
 * The corresponding <code>PingAppReporter</code> class can be used to record
 * information about the application behavior.
 *
 * @see PingAppReporter
 * @author teemuk
 */
public abstract class DtsnApplication extends Application {
	protected 	TimerObject LastActivity=null;
	protected 	long ActivtyTimerLength=Values.DTSN_LENGTH_OG_ACTIVITY_TIMRER;

	public DtsnApplication(String app){
		super();
		ArrayList<ApplicationListener> a=new 	ArrayList<ApplicationListener> ();
		a.add(new DtsnAppReporter());
		setAppListeners(a);
		appID = app;

	}

	public DtsnApplication(DtsnApplication source){
		super(source);
		LastActivity=source.LastActivity;
		ActivtyTimerLength=source.ActivtyTimerLength;
		appID = source.appID;
	}

	@Override
	public abstract Message handle(Message m, DTNHost host) ;
	@Override
	public abstract void update(DTNHost host) ;
	@Override
	public abstract Application replicate() ;



	protected void SendNewAck(int sessionId, int Max_Seq,DTNHost  Otherhost,DTNHost host) {
		DtsnMessage m= NewInstance(host, Otherhost, "ack"+"_"+host.getAddress()+"_"+Max_Seq+"T_"+SimClock.getTime(),sessionId,-1);
		m.setACK_BIT();
		m.setConfiramtionSeq(Max_Seq);
		sendToDestSetApp(m,host);
	}


	protected void SendNewNAck(String[] gap,int sessionId,DTNHost  Otherhost,DTNHost host) {
		DtsnMessage m= NewInstance(host, Otherhost, "nack"+"_"+host.getAddress()+"_"+Arrays.toString(gap)+"T_"+SimClock.getTime(),sessionId,-1);
		m.setNACK_BIT();
		m.setMissing(gap);
		sendToDestSetApp(m,host);
	}

	protected void sendToDestSetApp(DtsnMessage m,DTNHost host) {
		m.setAppID(appID);
		sendToDest(m,host);
	}


	protected void sendToDest(DtsnMessage m,DTNHost host) {
		m.setSize();
		host.createNewMessage(m);
		super.sendEventToListeners(DtsnAppReporter.SEND, m, host);
	}



	protected DtsnMessage NewInstance(DTNHost host, DTNHost otherhost, String string, int sessionId2,
			int seqNum2) {
		return 	new DtsnMessage(host,otherhost ,  string, 0,sessionId2,seqNum2);
	}




}



