package dtsn;



import java.lang.reflect.Array;
import java.util.Arrays;

import core.Application;
import core.DTNHost;
import core.Message;
import dtsn.tools.DtsnMessage;
import dtsn.tools.SortedList;
import dtsn.tools.Stat;
import dtsn.tools.TimerObject;
import report.DtsnAppReporter;

public class DtsnDestention extends DtsnApplication{
	private int sessionId;
	protected int largestSeqGotten;
	private SortedList ReceptionBuffer;
//	private int LastConfirm;

	//ack window flags
	private boolean AWsetStatus;
	private int AW;

	protected boolean SessionEnded;
	protected 	DTNHost Sourcehost;




	public DtsnDestention(){
		super("dtsn");
		ReceptionBuffer =new SortedList();
		sessionId=1;
	//	LastConfirm=1;
		AWsetStatus =false;
		SessionEnded=false;
		largestSeqGotten=0;
		AW=0;
		appID="dtsn";

	}

	public DtsnDestention(int _sessionId){
		super("dtsn");
		sessionId=_sessionId;
		ReceptionBuffer =new SortedList();
		AWsetStatus =false;
		SessionEnded=false;
		largestSeqGotten=0;
		AW=0;
		appID="dtsn";
//		LastConfirm=1;
	}



	public DtsnDestention(DtsnDestention dest) {
		super(dest);
		this.Sourcehost=dest.Sourcehost;
	//	LastConfirm=dest.LastConfirm;
		this.sessionId = dest.sessionId;
		ReceptionBuffer =  dest.ReceptionBuffer;
		AWsetStatus =  dest.AWsetStatus;
		AW =  dest.AW;
		this.largestSeqGotten=dest.largestSeqGotten;
	}



	@Override
	public Message handle(Message m, DTNHost host) {
		String[] s= ReceptionBuffer.getMissingPackets(1,Values.NUMBER_OF_PACKETS_TO_SEND);
		Stat.DEST_MINIMAL_MISSING=s!=null && s.length>0? s[0] : ""+Values.NUMBER_OF_PACKETS_TO_SEND+1;
		if (m.getDest()!=host.getAddress()){
			return m;
		}
		if (!SessionEnded){
			//System.out.println(m);
			return OnReciveMessage(m,host);
		}
		return null;
	}


	@Override
	public void update(DTNHost host) {
		if (!SessionEnded){
			if ((LastActivity!=null && LastActivity.Expired())){
				if (ReceptionBuffer.CheckAllPackets1(Values.NUMBER_OF_PACKETS_TO_SEND)){

					super.sendEventToListeners(ReceptionBuffer.toString(), null, host);

					SendNewAck(sessionId, Values.NUMBER_OF_PACKETS_TO_SEND, this.Sourcehost, host);
				}
				else{
					super.sendEventToListeners(DtsnAppReporter.TimerPop, LastActivity.length(), host);
					System.err.println("activity timer poped and packets= "+ Arrays.toString(ReceptionBuffer.getMissingPackets(1,Values.NUMBER_OF_PACKETS_TO_SEND)));
					super.sendEventToListeners("Sesssion deleted", null, host);
				}
				OnSessionEnd();
			}
		}
       
	}

	protected void OnSessionEnd() {
		SessionEnded=true;
		LastActivity.disable();
		
	}

	@Override
	public Application replicate() {
		return new DtsnDestention(this);
	}



	protected Message OnReciveMessage(Message m, DTNHost host){
		if (m instanceof DtsnMessage) {
			super.sendEventToListeners(DtsnAppReporter.RECIVE, m, host);
			Sourcehost=m.getFrom();
			DtsnMessage m1=(DtsnMessage)m;
			largestSeqGotten=Math.max(largestSeqGotten, m1.getSeqNum());
			return OnReciveControlMessage(m1,host);
		}
		return null;
	}

	protected Message OnReciveControlMessage(DtsnMessage m,DTNHost host){
		LastActivity=new TimerObject(this.ActivtyTimerLength,null,null);
		if (m.IsEAR_BIT() && !AWsetStatus){
			AW=m.getSeqNum();
			AWsetStatus=true;
		}
		if (m.IsDATA_BIT()){
			if (!ReceptionBuffer.IsDuplicate(m)){
				ReceptionBuffer.add(m);
				if (ReceptionBuffer.IsInSenquence(m)){
					DeliverToHigerLevel();
				}
			}
			if(m.IsPiggayBagBit()){
				OnEarDelivered(m.getSeqNum(),host);
			}
		}
		else if (m.IsEAR_BIT()){
			OnEarDelivered(m.getNextExpected(),host);
		}
		return null;

	}



	private void DeliverToHigerLevel() {
		super.sendEventToListeners("higherlevel", null, null);
	}

	/**
	 * if no send ack
	 * if yes send nack
	 * @param
	 */
	protected void OnEarDelivered(int last,DTNHost host) {
		String[] missing=ReceptionBuffer.getMissingPackets(1,last);

		//System.out.println("dest sent missing are: "+ Arrays.toString(missing));
		// Ack or Nack
		if (missing.length>0){
			SendNewNAck(missing,sessionId,Sourcehost, host);
			//System.out.println("dest sent"+ Arrays.toString(missing));

			//	LastConfirm=Integer.parseInt(missing[0]);
		}
		else{
			SendNewAck(sessionId,last,Sourcehost,host);
		//	LastConfirm=last-1;

		}

	}










}
