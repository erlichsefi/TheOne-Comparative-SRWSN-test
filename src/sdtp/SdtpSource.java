package SDTP;

import java.util.ArrayList;

import SDTP.tool.SdtpMessage;
import core.Application;
import core.DTNHost;
import core.Message;
import dtsn.DtsnSource;
import dtsn.tools.DtsnMessage;
import report.DtsnAppReporter;

public class SdtpSource extends DtsnSource {
	private int MaxSn=0;

	public SdtpSource() {
		super();
		appID="stdp";

	}


	public SdtpSource(SdtpSource source) {
		super(source);
		MaxSn = source.MaxSn;
		appID="stdp";
	}


	@Override
	public Application replicate() {
		return new SdtpSource(this);
	}

	@Override
	protected Message OnAckReceived(int to,DtsnMessage m,DTNHost host){
		SdtpMessage incoming=(SdtpMessage)m;
		// -1 because message n confirm from 1..n-1 and n isn't is the buffer
		//check @link SdtpMessage#setConfiramtionSeq
		SdtpMessage stored=(SdtpMessage)OutputBuffer.getSeqNum(to-1);

		if (stored!=null && to> MaxSn ) {
			if (incoming.validationAck(stored)){
				MaxSn=to;
				return super.OnAckReceived(to, m, host);
			}
			else{
				super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_Validion, m, host);

			}
			super.sendEventToListeners(DtsnAppReporter.ACK_DROP_TO_OLD, m, host);
		}
		//source drooping packets
		return null;
	}

	@Override
	protected Message OnNackReceived(DtsnMessage m,String[] seqNumbers,DTNHost host){
		SdtpMessage incoming=(SdtpMessage)m;

		ArrayList<String> tobesent=new ArrayList<String>();

		for (int i = 0; i < seqNumbers.length; i++) {
			int cast=Integer.parseInt(seqNumbers[i]);
			SdtpMessage message =(SdtpMessage) OutputBuffer.getSeqNum(cast);
			if (cast>MaxSn && message!=null){
				if (incoming.validationNAck(message, i)){
					tobesent.add(cast+"");
				}
				else{
					super.sendEventToListeners(DtsnAppReporter.NACK_DROP_Validion, m, host);
				}
			}
			else{
				super.sendEventToListeners(DtsnAppReporter.NACK_DROP_TO_OLD, m, host);
			}
		}
		//there is something to send
		if (!tobesent.isEmpty()){
			//convert to a array
			seqNumbers=new String[tobesent.size()];
			for (int i = 0; i < seqNumbers.length; i++) {
				seqNumbers[i]=tobesent.get(i);
			}
			return	super.OnNackReceived(incoming,seqNumbers,host);
		}
		return null;

	}

	@Override
	protected DtsnMessage NewInstance(DTNHost host, DTNHost otherhost, String string, int sessionId2,
			int seqNum2) {
		return 	new SdtpMessage(host,Desthost ,  string, 0,sessionId,seqNum);
	}
}
