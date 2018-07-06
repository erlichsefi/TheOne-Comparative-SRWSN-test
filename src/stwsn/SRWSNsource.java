package stwsn;


import java.util.ArrayList;

import core.Application;
import core.DTNHost;
import core.Message;
import dtsn.DtsnSource;
import dtsn.Values;
import dtsn.tools.DtsnMessage;
import dtsn.tools.TimerObject;
import report.DtsnAppReporter;
import stwsn.tools.StwsnMessage;

public class SRWSNsource extends DtsnSource{
	//open session values
	private TimerObject OpenSessionTimer=null;
	private long OpenSessionLength;
	private int MaxOpen;
	private int numberOfOpenMessageSent;

	//open session falgs
	private boolean OpenSessionAproved;
	private boolean SendingOpen;
	private int MaxSn;


	public SRWSNsource() {
		super();
		MaxSn=-1;
		MaxOpen=Values.SRWSN_MAX_OPEN_SESSION_TO_SEND;
		OpenSessionAproved=false;
		SendingOpen=false;
		numberOfOpenMessageSent=0;
		OpenSessionLength=Values.SRWSN_TIME_TO_WAIT_TO_OPEN_SESSION;
		appID="srwsn";
		StwsnMessage.openKeyManger(numberOfDataPackt);

	}




	public SRWSNsource(SRWSNsource s) {
		super(s);
		SendingOpen=s.SendingOpen;
		this.OpenSessionTimer=s.OpenSessionTimer;
		this.OpenSessionLength=s.OpenSessionLength;
		this.numberOfOpenMessageSent=s.numberOfOpenMessageSent;
		this.OpenSessionAproved=s.OpenSessionAproved;
		MaxSn=s.MaxSn;
		MaxOpen=s.MaxOpen;
		appID="srwsn";


	}






	@Override
	public void update(DTNHost host) {
		if (dataIsReady){

			//if didn't sent first open session or the timer is expired send open session
			if (!SendingOpen || (OpenSessionTimer!=null && OpenSessionTimer.Expired()))	{
				SendOpenSession(host);
			}
			super.update(host);
		}
	}

	@Override
	public Application replicate() {
		return new SRWSNsource(this);
	}


	private void SendOpenSession(DTNHost host){
		SendingOpen=true;
		if (numberOfOpenMessageSent<MaxOpen){
			StwsnMessage m=NewInstance(host,Desthost ,  "open"+"_"+host.getAddress()+"_"+numberOfOpenMessageSent,sessionId,0);
			m.setOPEN_SESSION_BIT();
			m.setValueForAuto();
			m.setNumberOfExpectedPackets(this.numberOfDataPackt);
			((StwsnMessage) m).setHash();
			numberOfOpenMessageSent++;
			sendToDestSetApp(m, host);
			OpenSessionTimer=new TimerObject(OpenSessionLength, null, null);
		}
		else{
			super.sendEventToListeners(DtsnAppReporter.TO_MUCH_OPEN_SESSION_FAILED, null, host);
		}

	}


	@Override
	protected Message OnNackReceived(DtsnMessage m,String[] seqNumbers,DTNHost host){
		StwsnMessage incoming=(StwsnMessage)m;
		int confi=Integer.parseInt(seqNumbers[0]);
		if (incoming.validationAck(confi)){
	
			super.OnAckReceived(confi, incoming, host);
			MaxSn=confi;
		}
		else{
			super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_Validion, m, host);
			return null;
		}	

		ArrayList<DtsnMessage> RTX=new ArrayList<DtsnMessage>();
		for (int i = 0; i < seqNumbers.length; i++) {
			int cast=Integer.parseInt(seqNumbers[i]);
			DtsnMessage missing=OutputBuffer.getSeqNum(cast);
			if (cast>MaxSn && missing!=null){
				if (incoming.validationNAck(i,missing)){
					RTX.add(missing);
				}
				else{
					super.sendEventToListeners(DtsnAppReporter.NACK_DROP_Validion, m, host);
				}
			}
			else{
				super.sendEventToListeners(DtsnAppReporter.NACK_DROP_TO_OLD, m, host);

			}
		}
		//if there is packet to re transmit , do that
		if (!RTX.isEmpty()){
			String[] trx=new String[RTX.size()];
			for (int i = 0; i < trx.length; i++) {
				trx[i]=RTX.get(i).getSeqNum()+"";
			}
			return super.OnNackReceived(incoming,trx,host);
		}
		return null;
	}

	@Override
	protected Message OnAckReceived(int to,DtsnMessage m,DTNHost host){
		StwsnMessage incoming=(StwsnMessage)m;
		if (m.IsNACK_BIT()) {
			/*TODO WHY? */
			to=Integer.parseInt(m.getMissingPackets()[0])-1;
		}
		//get open session
		if (to==0){
			OpenSessionTimer.disable();
			OpenSessionAproved=true;
			return null;
		}
		else if (to> MaxSn){ 
			if (incoming.validationAck(to) ) {
				Message m2= super.OnAckReceived(to, m, host);
				MaxSn=to;
				return m2;

			}
			else{
				super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_Validion, m, host);
			}
		}
		else{
			super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_OLD, m, host);
		}
		return null;

	}

	@Override
	protected boolean CanStartSendingData() {
		return dataIsReady && OpenSessionAproved;
	}
	@Override



	protected StwsnMessage NewInstance(DTNHost host, DTNHost otherhost, String string, int sessionId2,
			int seqNum2) {
		return 	new StwsnMessage(host,otherhost ,  string, 0,sessionId2,seqNum2);
	}


}
