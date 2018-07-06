package stwsn;

import core.Application;
import core.DTNHost;
import core.Message;
import dtsn.DtsnDestention;
import dtsn.Values;
import dtsn.tools.DtsnMessage;
import dtsn.tools.TimerObject;
import stwsn.tools.StwsnMessage;

public class SRWSNdestention extends DtsnDestention{
	private int MaxSn;
	private int MaxNumberOfEARtoHandle;

	//status timer values
	private TimerObject StatusTimer=null;
	private long StatusTimerLength;

	//there is an ear timer in the source,
	//Different use: reset the number of ear gotten  
	private int CurrentNumberOfEARgotten;
	private TimerObject EarTimer=null;
	private long EarTimerLength;


	public SRWSNdestention(){
		super();
		appID="srwsn";
		MaxNumberOfEARtoHandle=Values.SRWN_EAR_TO_HANDLE;
		StatusTimerLength=Values.SRWN_EAR_STATUS_TIMER;
		EarTimerLength=Values.SRWN_EAR_TIMER_LENGTH;
		CurrentNumberOfEARgotten=0;

	}


	public SRWSNdestention(SRWSNdestention sdtpDestention) {
		super(sdtpDestention);
		MaxSn=sdtpDestention.MaxSn;
		MaxNumberOfEARtoHandle=sdtpDestention.MaxNumberOfEARtoHandle;
		CurrentNumberOfEARgotten=sdtpDestention.CurrentNumberOfEARgotten;
		StatusTimer=sdtpDestention.StatusTimer;
		StatusTimerLength=sdtpDestention.StatusTimerLength;
		appID="srwsn";

	}

	@Override
	public Application replicate() {
		return new SRWSNdestention(this);
	}


	@Override
	public void update(DTNHost host) {
		if (!SessionEnded){
			if (StatusTimer!=null && StatusTimer.Expired()){
				super.OnEarDelivered(MaxSn+1, host);
				StatusTimer=new TimerObject(StatusTimerLength, largestSeqGotten, host);
			}
			if (EarTimer!=null && EarTimer.Expired()){
				CurrentNumberOfEARgotten=0;
				EarTimer=new TimerObject(EarTimerLength, null, null);
			}
			super.update(host);
		}
	}

	@Override
	protected void OnSessionEnd() {
		super.OnSessionEnd();
		if (StatusTimer!=null){
			StatusTimer.disable();
		}
	}
	@Override
	protected Message OnReciveControlMessage(DtsnMessage m,DTNHost host){
		StwsnMessage m1=(StwsnMessage)m;
		if (m.IsOPEN_SESSION_BIT() && m.getSeqNum()==0){
			OnOpenSession(m1,host);
		}
		return super.OnReciveControlMessage(m1, host);
	}

	protected void OnOpenSession(StwsnMessage m,DTNHost host){
		if (StwsnMessage.ValiteKeyManger(m.getNumberOfExpectedPackets(), m.getHashChainValue(), m.getMerkleTreeValue())){
			super.SendNewAck(m.getSession(),0 , m.getFrom(), host);
			StatusTimer=new TimerObject(StatusTimerLength, largestSeqGotten, host);
			EarTimer=new TimerObject(EarTimerLength, null, null);

		}
	}




	@Override
	protected void OnEarDelivered(int last,DTNHost host) {
		if (EarTimer!=null && EarTimer.Ispending()){
			if (CurrentNumberOfEARgotten<MaxNumberOfEARtoHandle){
				super.OnEarDelivered(last, host);
				CurrentNumberOfEARgotten++;
			}
			else{
				System.out.println("Does handle more then "+MaxNumberOfEARtoHandle+" EAR");
			}
		}
	}




	@Override
	protected void SendNewNAck(String[] gap,int sessionId,DTNHost OtherHost,DTNHost host) {
		super.SendNewNAck(gap, sessionId, OtherHost,host);
		MaxSn=Integer.parseInt(gap[0])-1;
	}

	@Override
	protected void SendNewAck(int sessionId, int Max_Seq,DTNHost  Otherhost,DTNHost host) {
		MaxSn=Max_Seq;
		super.SendNewAck(sessionId, Max_Seq, Otherhost, host);

	}




	@Override
	protected StwsnMessage NewInstance(DTNHost host, DTNHost otherhost, String string, int sessionId2,
			int seqNum2) {
		return 	new StwsnMessage(host,otherhost ,  string,0,sessionId2,seqNum2);
	}

}
