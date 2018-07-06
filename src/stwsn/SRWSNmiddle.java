package stwsn;

import java.util.ArrayList;
import java.util.Arrays;

import core.Application;
import core.DTNHost;
import core.Message;
import core.SimClock;
import dtsn.DtsnMiddle;
import dtsn.Values;
import dtsn.tools.DtsnMessage;
import dtsn.tools.TimerObject;
import report.DtsnAppReporter;
import stwsn.tools.MessageAggregation;
import stwsn.tools.RetransmissionCounter;
import stwsn.tools.StwsnMessage;

public class SRWSNmiddle extends DtsnMiddle<StwsnMessage>{
	//Aggregation flags
	private	 TimerObject AggregationTimer=null;
	private	MessageAggregation ControlMessageAggregation;

	//re transmit tracker
	private	RetransmissionCounter Rxt;

	private	boolean openSessionConfirmed;
	private	int sessionID;

	// Flags for returning packets
	private	int MaxSn_returens;
	private	int Max_SN_Return;
	private	int MaxSn;




	public SRWSNmiddle() {
		super();
		appID="srwsn";
		MaxSn=0;
		sessionID=0;
		openSessionConfirmed=false;
		MaxSn_returens=0;
		ControlMessageAggregation=new MessageAggregation();   
		Rxt=new RetransmissionCounter(Values.SRWSN_RTX_LIMITION_PER_PACKET);
		Max_SN_Return=Values.SRWSN_MAX_SN_RETURN;

	}

	public SRWSNmiddle(SRWSNmiddle r) {
		super(r);
		AggregationTimer=r.AggregationTimer;
		sessionID=r.sessionID;
		MaxSn=r.MaxSn;
		appID="srwsn";
		openSessionConfirmed=r.openSessionConfirmed;
		ControlMessageAggregation=r.ControlMessageAggregation;
		Rxt=r.Rxt;
		Max_SN_Return=r.Max_SN_Return;
		MaxSn_returens=r.MaxSn_returens;



	}

	@Override
	public Application replicate() {
		return new SRWSNmiddle(this);
	}



	@Override
	protected Message OnHandleAck(DtsnMessage m,int before, DTNHost host) {
		StwsnMessage incoming=(StwsnMessage)m;		

		if (incoming.validationAck(before)){ 

			if(before> MaxSn ){
				MaxSn=before;
				return super.OnHandleAck(m,before,host);
			}
			else if (before== MaxSn){ 
				MaxSn_returens++;
				if (MaxSn_returens >Max_SN_Return){
					Message m2=packets_cache.getNextSeqMessage(before);
					MaxSn_returens=0;
					if (m2!=null)
					sendToDest(m2, host);
				}
				/* TODO to pass ? */
				return m;
			}
			else{
				super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_OLD, m, host);
				/* TODO to pass ? */
				return m;
			}
		}
		else{
			super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_Validion, m, host);
			/* TODO to pass ? */
			return m;
		}
	}

	@Override
	protected Message OnReceivedControlMessage(Message m,DTNHost host){
		if (m instanceof StwsnMessage) {
			//get data from open session
			StwsnMessage m1=(StwsnMessage)m;
			if (m1.IsOPEN_SESSION_BIT()){
				super.sendEventToListeners(DtsnAppReporter.RECIVE, m1, host);
				return OnOpenSession(m1);
			}
			else if (m1.IsACK_BIT() && m1.getConfiramtionSeq()==0 ){
				super.sendEventToListeners(DtsnAppReporter.RECIVE, m1, host);
				return OnAckOpenSession(m1,m1.getConfiramtionSeq()+1);
			}



			//handle data
			else if (openSessionConfirmed){
				StwsnMessage sm= (StwsnMessage) super.OnReceivedControlMessage(m1, host);

				//check if the Fit To Aggregation before passing
				if(sm!=null && ControlMessageAggregation.FitToAggregation(sm)){
					ControlMessageAggregation.Add(sm);
				}
				else{
					return sm;
				}

			}
			// Valid ack open session
			//else 

		}
		return m;
	}

	protected Message OnOpenSession(StwsnMessage m1){
		if (m1.ValidateHash()){
			return m1;
		}
		else{
			Prob=0;
			return m1;
		}
	}

	protected Message OnAckOpenSession(StwsnMessage incoming,int seq){
		if (incoming.validationAck(seq)){
			AggregationTimer=new TimerObject(ControlMessageAggregation.getAggregationTime(), incoming.getSession(), incoming.getTo());
			openSessionConfirmed=true;
		}
		return incoming;
	}

	@Override
	public void update(DTNHost host) {
		if (AggregationTimer!=null && AggregationTimer.Expired()){
			//super.sendEventToListeners("Aggregation timer expird", null, host);
			int sessionId=(int) AggregationTimer.getMessageThatInvokeTimer();
			HandleAggregateMessage(sessionId,host);
			AggregationTimer=new TimerObject(ControlMessageAggregation.getAggregationTime(), sessionId, host);
		}
		super.update(host);
	}

	protected void sendToDest(Message m,DTNHost host) {
		StwsnMessage sm=(StwsnMessage)m;
		//create Aggregation if nack became ack
		if(ControlMessageAggregation.FitToAggregation(sm)){
			ControlMessageAggregation.Add(sm);
		}
		else{
			super.sendToDest(sm, host);
		}
	}

	protected void SendNewAckWithoutAggregation(int sessionId, int Max_Seq,DTNHost  Otherhost,DTNHost host) {
		DtsnMessage m= NewInstance(host, Otherhost, "ack"+"_"+host.getAddress()+"_"+Max_Seq+"c_"+SimClock.getTime(),sessionId,-1);
		m.setACK_BIT();
		m.setConfiramtionSeq(Max_Seq);
		m.setAppID(this.appID);
		super.sendToDest(m,host);
	}

	protected void SendNewNAckWithoutAggregation(String[] gap,int sessionId,DTNHost host,DTNHost Otherhost) {
		DtsnMessage m= NewInstance(host, Otherhost, "nack"+"_"+host.getAddress()+"_"+Arrays.toString(gap)+"c_"+SimClock.getTime(),sessionId,-1);
		m.setNACK_BIT();
		m.setMissing(gap);
		m.setAppID(this.appID);
		super.sendToDest(m,host);
	}


	@Override
	protected Message OnHandleNack(DtsnMessage m,String[] seqNumbers, DTNHost host,boolean decide) {
		StwsnMessage incoming=(StwsnMessage)m;
		int max=0;
		//if the nack confiraming all packets
		if (Integer.parseInt(seqNumbers[0])>=MaxSn){
			/*TODO what about all the other numbers that higer */

			ArrayList<String> Pass=new ArrayList<String>();
			ArrayList<String> RTX=new ArrayList<String>();

			for (int i = 0; i < seqNumbers.length; i++) {
				int cast=Integer.parseInt(seqNumbers[i]);
				max=Math.max(max, cast);
				StwsnMessage stored=packets_cache.getMesage(cast);
				if (cast>MaxSn && stored!=null){
					if (incoming.validationNAck( i,stored)){
						if (Rxt.Totransmite(cast)){
							RTX.add(cast+"");
						}
						else{
							super.sendEventToListeners(DtsnAppReporter.TO_MUCH_RETRANSMITE, null, host);
						}
					}
					else{
						super.sendEventToListeners(DtsnAppReporter.NACK_DROP_Validion, null, host);
					}
				}
				else{
					Pass.add(cast+"");
					super.sendEventToListeners(DtsnAppReporter.NACK_DROP_TO_OLD, null, host);
				}
			}
			//if changed - change the list elements
			if (!RTX.isEmpty()){
				seqNumbers=new String[RTX.size()];
				for (int i = 0; i < seqNumbers.length; i++) {
					seqNumbers[i]=RTX.get(i);
				}
			}
			//either way send all that can be sent
			if (seqNumbers.length>0){
				super.OnHandleNack(incoming,seqNumbers,host,false);
			}
			if (!Pass.isEmpty()){
				//convert list to array of message to pass the source
				String[] stillmissing=new String[Pass.size()];
				for (int i = 0; i < stillmissing.length; i++) {
					stillmissing[i]=Pass.get(i);
				}
				//decide if there is message that not found to send nack about
				return super.NackOrAck(m,max,stillmissing,host);

			}

		}
		return m;

	}

	@Override
	protected StwsnMessage NewInstance(DTNHost host, DTNHost otherhost, String string, int sessionId2,
			int seqNum2) {
		return 	new StwsnMessage(host,otherhost ,  string,0,sessionId2,seqNum2);
	}

	public void HandleAggregateMessage(int session,DTNHost host){
		DtsnMessage c=ControlMessageAggregation.Collect();
		if (c!=null){
			super.sendToDest(c, c.getFrom());
			ControlMessageAggregation.init();
		}
	}


}

