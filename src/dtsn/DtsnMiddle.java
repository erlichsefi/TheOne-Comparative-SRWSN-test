package dtsn;

import java.util.ArrayList;

import core.Application;
import core.DTNHost;
import core.Message;
import dtsn.tools.Cache;
import dtsn.tools.DtsnMessage;
import report.DtsnAppReporter;

public class DtsnMiddle<T extends DtsnMessage> extends DtsnApplication {
	protected Cache<T> packets_cache;
	protected double Prob=0;



	public DtsnMiddle() {
		super("dtsn");
		packets_cache=new Cache<T>();

	}

	public DtsnMiddle(DtsnMiddle<T> dtsnMiddle) {
		super(dtsnMiddle);
		packets_cache=dtsnMiddle.packets_cache;
		this.appID="dtsn";
	}


	@Override
	public Message handle(Message m, DTNHost host) {
		return OnReceivedControlMessage(m,host);
	}


	@Override
	public void update(DTNHost host) {
	}


	@Override
	public Application replicate() {
		return new DtsnMiddle<T>(this);
	}


	protected Message OnReceivedControlMessage(Message m1,DTNHost host){
		if (m1 instanceof DtsnMessage) {
			super.sendEventToListeners(DtsnAppReporter.RECIVE, m1, host);

			DtsnMessage m=(DtsnMessage)m1;
			if (m.IsDATA_BIT()){
				return OnDataPacket(m);
			}
			else if (m.IsNACK_BIT()){
				return OnHandleNack(m,m.getMissingPackets(),host,true);

			}
			else if (m.IsACK_BIT()){
				return OnHandleAck(m,m.getConfiramtionSeq(),host);
			}
			return m;
		}
		return m1;
	}


	protected Message OnDataPacket(DtsnMessage m){
		packets_cache.RemoveOldSessions( m.getSource(), m.getDest(), m.getAppID(), m.getSession());
		if (!packets_cache.IsInCache(m.getSeqNum())){
			Prob=GetProbiblityToSavePacket(); 
			double PackP=Math.random();
			if (PackP<Prob)
				packets_cache.Add(m.getAppID(), m, m.getSession(), m.getSource(), m.getDest());
		}
		return m;
	}

	protected Message OnHandleAck(DtsnMessage m,int ConfiramtionSeq, DTNHost host) {
		packets_cache.DeleteAllBefore(ConfiramtionSeq);
		return m;
	}

	protected Message OnHandleNack(DtsnMessage m,String[] MissingSeq, DTNHost host,boolean decied) {
		int Max_Seq=0;
		//find messages in the list
		DtsnMessage[] messages=packets_cache.getAllinCache(MissingSeq);

		//if there is any
		if (messages.length>0){
			packets_cache.DeleteAllBefore(Integer.parseInt(MissingSeq[0]));

			//send all the packets he found but not the last
			for (int i = 0; i < messages.length-1; i++) {
				Max_Seq=Math.max(Max_Seq, messages[i].getSeqNum());
				DtsnMessage m2= messages[i].replicateRTX();
				m2.setRTX_BIT();
				sendToDest(m2,host);
			}


			//Getting the last packet 
			DtsnMessage last=messages[messages.length-1];
			Max_Seq=Math.max(Max_Seq, messages[messages.length-1].getSeqNum());


			//checking how much packets are still missing
			ArrayList<String> stillMissing=new ArrayList<String>();
			for (int i = 0; i < MissingSeq.length; i++) {
				if (!IsInMessageList(messages,Integer.parseInt(MissingSeq[i]))){
					stillMissing.add(MissingSeq[i]);
				}
			}

			// to send with EAR_FLAG  or not
			if (stillMissing.size()>0){
				sendToDest(last.replicateRTX(),host);
			}
			else{
				last.setPiggayBagBit(EAR_FLAG);
				sendToDest( last.replicateRTX(),host);
			}

			//returning them to array representing to prevent double code 
			MissingSeq=new String[stillMissing.size()];
			for (int i = 0; i < stillMissing.size(); i++) {
				MissingSeq[i]=stillMissing.get(i);
			}



		}
		if (decied){
			return NackOrAck(m,Max_Seq,MissingSeq,host);
		}
		else{
			return null;
		}


	}

	protected Message NackOrAck(DtsnMessage m,int Max_Seq,String[] missing, DTNHost host){
		// Nack or Ack
		if (missing.length>0){
			DtsnMessage m2=(m);
			m2.setMissing(missing);
			return m2;
		}
		else{
			SendNewAck(m.getSession(),Max_Seq+1,m.getTo(),m.getFrom());
			return null;
		}
	}
	private double GetProbiblityToSavePacket() {
		return 0.8;
	}


	private boolean IsInMessageList(DtsnMessage[] messages, int Seq) {
		for (int i = 0; i < messages.length; i++) {
			if (messages[i].getSeqNum()==Seq)
				return true;
		}
		return false;
	}













}
