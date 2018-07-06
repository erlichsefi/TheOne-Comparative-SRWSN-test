package SDTP;

import java.util.ArrayList;

import SDTP.tool.SdtpMessage;
import core.Application;
import core.DTNHost;
import core.Message;
import dtsn.DtsnMiddle;
import dtsn.tools.DtsnMessage;
import report.DtsnAppReporter;

public class SdtpMiddle extends DtsnMiddle<SdtpMessage> {
	private int MaxSn;

	public SdtpMiddle() {
		super();
		appID="stdp";
		MaxSn=0;
	}


	public SdtpMiddle(SdtpMiddle sdtpMiddle) {
		super(sdtpMiddle);
		this.MaxSn=sdtpMiddle.MaxSn;
		appID="stdp";

	}


	@Override
	public Application replicate() {
		return new SdtpMiddle(this);
	}

	@Override
	protected Message OnHandleAck(DtsnMessage m,int confirmtionSeq, DTNHost host) {
		SdtpMessage incoming=(SdtpMessage)m;
		SdtpMessage stored=packets_cache.getMesage(m.getConfiramtionSeq());
		if (stored != null){
			if( confirmtionSeq> MaxSn ){
				if (incoming.validationAck(stored) ){
					MaxSn=m.getSeqNum();
					return super.OnHandleAck(m,confirmtionSeq,host);
				}
				else{
					super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_Validion, m, host);
					/*TODO ACK DROP? */
					return m;
				}
			}
			else{
				super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_OLD, m, host);
				/*TODO ACK DROP? */
				return m;
			}
		}
		return m;
	}


	@Override
	protected Message OnHandleNack(DtsnMessage m,String[] seqNumbers, DTNHost host,boolean decied) {
		int max=0;
		SdtpMessage incoming=(SdtpMessage)m;

		//message not found
		ArrayList<String> TobeSent=new ArrayList<String>();
		//message found and valite
		ArrayList<String> PassToSource=new ArrayList<String>();

		boolean listChanged=false;

		for (int i = 0; i < seqNumbers.length; i++) {
			int cast=Integer.parseInt(seqNumbers[i]);
			SdtpMessage message =packets_cache.getMesage(cast);
			if (cast>MaxSn){
				if (message!=null){
					listChanged=true;
					if (incoming.validationNAck(message, i)){
						max=Math.max(max, cast);
						TobeSent.add(cast+"");
					}
				}
				else{
					//passing to source only packets that are not found!
					//if a packet is found but not valite - forget her seq
					/* TODO ask amit */
					PassToSource.add(cast+"");
				}
			}
		}
		if (listChanged){

			//convert list to array of message to send
			seqNumbers=new String[TobeSent.size()];
			for (int i = 0; i < seqNumbers.length; i++) {
				seqNumbers[i]=TobeSent.get(i);
			}
			//convert list to array of message to pass the source
			String[] stillmissing=new String[PassToSource.size()];
			for (int i = 0; i < stillmissing.length; i++) {
				stillmissing[i]=PassToSource.get(i);
			}

			//if there is something to send - send it
			if (seqNumbers.length>0){
				super.OnHandleNack(incoming,seqNumbers,host,false);
			}
			//decide if there is message that not found to send nack about
			return super.NackOrAck(m,max,stillmissing,host);
		}
		else{
			//didnt found any message pass 
			return m;
		}

	}
	@Override
	protected DtsnMessage NewInstance(DTNHost host, DTNHost otherhost, String string, int sessionId2,
			int seqNum2) {
		return 	new SdtpMessage(host,otherhost ,  string, 0,sessionId2,seqNum2);
	}

}
