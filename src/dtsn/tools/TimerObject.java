package dtsn.tools;

import core.DTNHost;

public class TimerObject {
	long startTime;
	boolean pending=true;
	long TimeToWait;
	Object messageThatInvokeTimer;
	DTNHost hostThatInvokeTimer;

	public TimerObject( long millis, Object m, DTNHost host){
		startTime=System.currentTimeMillis();
		TimeToWait=millis;
		messageThatInvokeTimer=m;
		hostThatInvokeTimer=host;
	}

	public void disable(){
		pending=false;
	}
	
	public boolean Ispending(){
		return pending;
	}
	
	public boolean Expired(){
		if ((System.currentTimeMillis()-startTime)>TimeToWait && pending){
			pending=false;
		return true;
		}
		return false;
	}

	public Object getMessageThatInvokeTimer() {
		return messageThatInvokeTimer;
	}

	public DTNHost getHostThatInvokeTimer() {
		return hostThatInvokeTimer;
	}

    public long length() {
		return TimeToWait;
    }
}
