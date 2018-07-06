package SDTP;


import SDTP.tool.SdtpMessage;
import core.DTNHost;
import dtsn.DtsnDestention;
import dtsn.tools.DtsnMessage;

public class SdtpDestention extends DtsnDestention{
	private int MaxSn;


	
	public SdtpDestention(){
		super();
		appID="stdp";
		MaxSn=0;

	}
	
	
	public SdtpDestention(SdtpDestention sdtpDestention) {
		super(sdtpDestention);
		MaxSn=sdtpDestention.MaxSn;
	}

	

	@Override
	public SdtpDestention replicate() {	
		return new SdtpDestention(this);
	}
	
	@Override
	protected void SendNewNAck(String[] gap,int sessionId,DTNHost  Otherhost,DTNHost host) {
		super.SendNewNAck(gap, sessionId, Otherhost,host);
		MaxSn=Integer.parseInt(gap[0])-1;
	}
	
	@Override
	protected void SendNewAck(int sessionId, int Max_Seq,DTNHost  Otherhost,DTNHost host) {
		MaxSn=Max_Seq;
		super.SendNewAck(sessionId, Max_Seq, Otherhost, host);

	}

	@Override
	protected DtsnMessage NewInstance(DTNHost host, DTNHost otherhost, String string, int sessionId2,
			int seqNum2) {
		return 	new SdtpMessage(host,otherhost , string, 0,sessionId2,seqNum2);
	}

}
