package dtsn;

import java.util.ArrayList;
import java.util.Random;


import core.Application;
import core.DTNHost;
import core.Message;
import core.SimClock;
import dtsn.Exception.ScenarioUndefined;
import dtsn.tools.Buffer;
import dtsn.tools.DtsnMessage;
import dtsn.tools.TimerObject;
import report.DtsnAppReporter;

public class DtsnSource extends DtsnApplication {
	/*
	 * EAR HANDLE
	 */
	protected int MaxNumberOfEar;
	protected TimerObject EarTimer=null;
	protected int EarAttemptsCounter;

	/* 
	 * DATA 
	 */
	protected Buffer OutputBuffer;
	protected  byte[][] DataBlockBuffer=null;


	protected boolean dataIsReady;
	protected int seqNum;
	protected int sessionId;
	protected int AW;
	protected int numberOfDataPackt;
	protected 	DTNHost Desthost;


	/**
	 * default constructor
	 */
	public DtsnSource(){
		super("dtsn");
		AW=-1;
		EarAttemptsCounter=0;
		dataIsReady=false;
		seqNum=1;
		MaxNumberOfEar=Values.MAX_NUM_OF_EAR;
		sessionId=(int)(Math.random()*1000);
		OutputBuffer= new Buffer(Values.Source_BUFFER_SIZE) ;
		numberOfDataPackt=Values.NUMBER_OF_PACKETS_TO_SEND;

	}

	/**
	 * default constructor
	 */
	public DtsnSource(String app){
		super(app);
		AW=-1;
		EarAttemptsCounter=0;
		dataIsReady=false;
		seqNum=1;
		MaxNumberOfEar=Values.MAX_NUM_OF_EAR;
		sessionId=(int)(Math.random()*1000);
		OutputBuffer= new Buffer(Values.Source_BUFFER_SIZE) ;
		numberOfDataPackt=Values.NUMBER_OF_PACKETS_TO_SEND;

	}

	/**
	 * copy constructor
	 * @param source
	 */
	public DtsnSource(DtsnSource source) {
		super(source);
		Desthost=source.Desthost;
		dataIsReady=false;
		this.numberOfDataPackt=source.numberOfDataPackt;
		this.EarTimer = source.EarTimer;
		OutputBuffer = source.OutputBuffer;
		this.seqNum = source.seqNum;
		this.EarAttemptsCounter = source.EarAttemptsCounter;
		this.MaxNumberOfEar = source.MaxNumberOfEar;
		this.sessionId = source.sessionId;
		AW = source.AW;
		DataBlockBuffer = source.DataBlockBuffer;
		appID="dtsn";

	}

	@Override
	public Application replicate() {
		return new DtsnSource(this);
	}


	/**
	 * a function to start the data sending the first data packets
	 * @param host
	 */
	private void SendFirstDataPackets(DTNHost host){
		super.sendEventToListeners(DtsnAppReporter.STARTIME, null, host);
		AW=Math.min(OutputBuffer.Totalsize(), DataBlockBuffer[0].length);
		for (int i = 0; i < AW-1; i++) {
			DtsnMessage m=NewInstance(host,Desthost ,  "data"+"_"+host.getAddress()+"_"+seqNum,sessionId,seqNum);
			m.setNoramlData(DataBlockBuffer[seqNum-1]);
			m.setDATA_BIT();
			sendToDestSetApp(m,host);
			seqNum++;
			OutputBuffer.add(m);
		}
		SendDataWithEar(seqNum,host);
	}




	@Override
	public Message handle(Message m, DTNHost host) {
		if (m.getDest()!=host.getAddress()){
			return m;
		}
		return OnMessageRecive(m,host);
	}

	private Message OnMessageRecive(Message m, DTNHost host){
		if (m instanceof DtsnMessage) {
			DtsnMessage CastM=(DtsnMessage)m;
			if (CastM.IsInvoker()){
				SetPackets();
				Desthost=m.getFrom();
				return null;
			}
			else {
				return OnReciveControlMessage(CastM,host);
			}
		}
		return m;
	}


	private void SetPackets(){
		if (!dataIsReady){
			dataIsReady=true;
			Random rand = new Random(); 
			DataBlockBuffer=new byte[numberOfDataPackt][100];
			rand.setSeed(System.currentTimeMillis()); 
			for (int i = 0; i < DataBlockBuffer.length; i++) {
				rand.nextBytes(DataBlockBuffer[i]);
			}
		}
		else{
			throw new ScenarioUndefined("data reuest to many times");
		}


	}


	@Override
	public void update(DTNHost host) {
		if (CanStartSendingData()){
			//check ear timer
			if (EarTimer!=null && EarTimer.Expired()){
				EARTimerExpird(host);
			}
			if (LastActivity!=null && LastActivity.Expired())
				OnActivityTimerExpired(host);
			if (seqNum==1){
				SendFirstDataPackets(host);
			}
			
			DtsnMessage m=getNextDataPackets(host);
			if (m!=null){
				TrySendingData(m,host);
			}

		}

	}



	protected void OnActivityTimerExpired(DTNHost host){
		//check of there is packets still waiting to confirmation
		if (!OutputBuffer.isEmpty()){
			if ( EarTimer==null || !EarTimer.Ispending() )
				SendEARandRestartActivity(seqNum,host);
		}
		else{
			EndSession();
		}
	}

	protected boolean CanStartSendingData() {
		return dataIsReady;
	}


	/**
	 * a function that collect the way to handle different packets
	 * @param m
	 * @param host
	 * @return
	 */
	protected Message OnReciveControlMessage(DtsnMessage m,DTNHost host){
		super.sendEventToListeners(DtsnAppReporter.RECIVE, m, host);
		if (m.IsACK_BIT()){
			return OnAckReceived(m.getConfiramtionSeq(),m,host);
		}
		else if (m.IsNACK_BIT()){
			return OnNackReceived(m,m.getMissingPackets(),host);
		}
		else{
			throw new ScenarioUndefined(" Dtsn Source Got a Message that Not Ack and Not Nack ");
		}
	}




	protected Message OnAckReceived(int to,DtsnMessage m,DTNHost host){
		OutputBuffer.removeAllBefore(to);
		EarAttemptsCounter=0;
		if (EarTimer!=null){
			EarTimer.disable();
		}
		LastActivity=new TimerObject(ActivtyTimerLength,null,null);
		if (to!=Values.NUMBER_OF_PACKETS_TO_SEND){
			return SendEARandRestartActivity(seqNum,host);
		}
		else{
			OnAllPacketAcked();
			return null;
		}
	}



	protected Message OnNackReceived(DtsnMessage m,String[] seqNumbers,DTNHost host){
		//send all lost packets
		ArrayList<DtsnMessage> tosend=new ArrayList<DtsnMessage>();
		for (int j = 0; j < seqNumbers.length-1; j++) {
			int seqN=Integer.parseInt(seqNumbers[j]);
			DtsnMessage m2= OutputBuffer.getSeqNum(seqN);
			if (m2!=null){
				m2.setRTX_BIT();
				tosend.add(m2);
			}
		}

		int seqN=Integer.parseInt(seqNumbers[seqNumbers.length-1]);
		DtsnMessage m2= (DtsnMessage) OutputBuffer.getSeqNum(seqN);
		if (m2!=null){
			m2.setRTX_BIT();
			tosend.add( m2);
		}

		if (!tosend.isEmpty()){
			//send the packets
			for (int i = 0; i < tosend.size()-1; i++) {
				sendToDest(tosend.get(i).replicateRTX(), host);
			}
			DtsnMessage last=(DtsnMessage) tosend.get(tosend.size()-1);
			last.setPiggayBagBit(EAR_FLAG);
			sendToDest( last.replicateRTX(),host);
		}
		return OnAckReceived(Integer.parseInt(seqNumbers[0]),m,host);
	}



	private DtsnMessage getNextDataPackets(DTNHost host){
		if (seqNum-1<DataBlockBuffer.length){
			DtsnMessage m=NewInstance(host,Desthost , "data"+"_"+host.getAddress()+"_"+seqNum,sessionId,seqNum);
			m.setNoramlData(DataBlockBuffer[seqNum-1]);
			m.setDATA_BIT();
			return m;
		}

		return null;
	}


	private boolean TrySendingData(DtsnMessage m,DTNHost host){
		if (OutputBuffer.IsFull()){
			SingelErrorToApplicaion();
			return false;
		}
		else{
			OutputBuffer.add(m);
			LastActivity=new TimerObject(ActivtyTimerLength,null,null);

			if (OutputBuffer.IsFull()){
				sendToDestWithEar(m,host);
			}
			else{
				int sqeN=m.getSeqNum();
				if (AW%sqeN==0){
					sendToDestWithEar(m,host);
				}
				else{
					sendToDestSetApp(m,host);
				}			
			}
			seqNum++;
			return true;
		}
	}





	private void SingelErrorToApplicaion() {
		//System.err.println("error");
	}

	private void EndSession() {
		super.sendEventToListeners(DtsnAppReporter.ENDTIME, null, null);
		//System.err.println("Session is ended");
	}

	private void NotifyUnconfirmedPackets() {
		super.sendEventToListeners("Unconfirmed Packets", null, null);
	}


	private Message SendEARandRestartActivity(int dataPacketCount,DTNHost host) {
		if (dataPacketCount<numberOfDataPackt){
			//SEND EAR
			DtsnMessage m2 = NewInstance(host, Desthost, "ear"+"_"+host.getAddress()+"_"+EarAttemptsCounter+"_T"+SimClock.getTime(),sessionId,-1);
			m2.setEAR_BIT();
			m2.setNextExpected(dataPacketCount);
			sendToDestSetApp(m2, host);
			//TIMER
			StartNewEARtimer(OutputBuffer.GetEarTime(),host);
			//ACTIVITY
			LastActivity=new TimerObject(ActivtyTimerLength,null,null);
		}
		return null;
	}



	private void sendToDestWithEar(DtsnMessage m,DTNHost host) {
	    //ear flags
		EarAttemptsCounter=1;
		StartNewEARtimer(OutputBuffer.GetEarTime(),host);
		//send ear
		m.setPiggayBagBit(EAR_FLAG);
		sendToDestSetApp(m,host);
	}

	private void EARTimerExpird(DTNHost host){
		EarAttemptsCounter++;
		if (EarAttemptsCounter>MaxNumberOfEar){
			NotifyUnconfirmedPackets();
			EndSession();
		}
		else{
			SendEARandRestartActivity(seqNum,host);
		}
	}



	private void OnAllPacketAcked(){
		EarTimer.disable();
		LastActivity.disable();
		super.sendEventToListeners(DtsnAppReporter.ALL_CONF, null, null);
		EndSession();
	}

	private void StartNewEARtimer(long mili,final DTNHost host){
		if (EarTimer!=null)
			EarTimer.disable();
		EarTimer=new TimerObject(mili,null,host);

	}

	private Message SendDataWithEar(int sending_index2, DTNHost host) {
		DtsnMessage m=NewInstance(host,Desthost ,  "data"+"_"+host.getAddress()+"_"+sending_index2,sessionId,seqNum);
		m.setNoramlData(DataBlockBuffer[seqNum-1]);
		m.setDATA_BIT();
		m.setPiggayBagBit(EAR_FLAG);;
		m.setNextExpected(sending_index2);
		seqNum++;
		OutputBuffer.add(m);
		this.sendToDestSetApp(m, host);
		StartNewEARtimer(OutputBuffer.GetEarTime(),host);
		return null;
	}













}
