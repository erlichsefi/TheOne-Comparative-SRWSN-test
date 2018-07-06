package dtsn.tools;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import core.DTNHost;
import core.Message;
import core.SimError;
import dtsn.Exception.ScenarioUndefined;

public class DtsnMessage extends Message implements Serializable ,Comparator<DtsnMessage> {
	
	private static final long serialVersionUID = 1L;
	protected int piggayBagBit;
	protected int SeqNum;
	protected int sessionId;
	protected byte[] data;
	protected byte[] data2;

	protected int RTX_BIT=0;
	protected int EAR_BIT=0;
	protected int ACK_BIT=0;
	protected int NACK_BIT=0;
	protected int DATA_BIT=0;
	protected int OPEN_SESSION_BIT=0;
	protected int INVOKER=0;

	public DtsnMessage(){

	}


	/**
	 * Creates a new Message.
	 * @param from Who the message is (originally) from
	 * @param to Who the message is (originally) to
	 * @param id Message identifier (must be unique for message but
	 * 	will be the same for all replicates of the message)
	 * @param size Size of the message (in bytes)
	 * @param seqNum2 
	 */
	public DtsnMessage(DTNHost from, DTNHost to, String id, int size,int session, int seqNum2) {
		super(from,to,id,size);
		sessionId=session;
		this.SeqNum=seqNum2;
	}


	


	/**
	 * Deep copies message data from other message. If new fields are
	 * introduced to this class, most likely they should be copied here too
	 * (unless done in constructor).
	 * @param m The message where the data is copied
	 */
	protected void copyFrom(DtsnMessage m) {
		this.path = new ArrayList<DTNHost>(m.path);
		this.timeCreated = m.timeCreated;
		this.responseSize = m.responseSize;
		this.requestMsg  = m.requestMsg;
		this.initTtl = m.initTtl;
		this.appID = m.appID;
		appID=m.appID;

		piggayBagBit=m.piggayBagBit;

		SeqNum=m.SeqNum;

		sessionId=m.sessionId;

		data=m.data;

		RTX_BIT=m.RTX_BIT;
		EAR_BIT=m.EAR_BIT;
		ACK_BIT=m.ACK_BIT;
		NACK_BIT=m.NACK_BIT;
		DATA_BIT=m.DATA_BIT;
		OPEN_SESSION_BIT=m.OPEN_SESSION_BIT;
		INVOKER=m.INVOKER;
		data2=m.data2;
		if (m.properties != null) {
			Set<String> keys = m.properties.keySet();
			for (String key : keys) {
				updateProperty(key, m.getProperty(key));
			}
		}
	}

	/**
	 * Returns a replicate of this message (identical except for the unique id)
	 * @return A replicate of the message
	 */
	public Message replicate() {
		DtsnMessage m = new DtsnMessage(from, to, id, size,sessionId,SeqNum);
		m.copyFrom(this);
		return m;
	}
	
	/**
	 * Returns a replicate of this message (identical except for the unique id)
	 * @return A replicate of the message
	 */
	public DtsnMessage replicateRTX() {
		DtsnMessage m = new DtsnMessage(from, to, "C"+id, size,sessionId,SeqNum);
		m.copyFrom(this);
		return m;
	}



	public int getPiggayBagBit() {
		return piggayBagBit;
	}

	public void setPiggayBagBit(int piggayBagBit) {
		this.piggayBagBit = piggayBagBit;
		addProperty("piggayBagBit",piggayBagBit);

	}


	public int getSeqNum() {
		// TODO Auto-generated method stub
		return SeqNum;
	}

	public boolean IsRTX_BIT() {
		return RTX_BIT==1;
	}

	public void setRTX_BIT() {
		RTX_BIT = 1;
		addProperty("RTX_BIT",1);

	}

	public boolean IsEAR_BIT() {
		return EAR_BIT==1;
	}

	public void setEAR_BIT() {
		EAR_BIT = 1;
		addProperty("EAR_BIT",1);

	}

	public boolean IsACK_BIT() {
		return ACK_BIT==1;
	}

	public void setACK_BIT() {
		ACK_BIT = 1;
		addProperty("ACK_BIT",1);

	}

	public boolean IsNACK_BIT() {
		return NACK_BIT==1;
	}

	public void setNACK_BIT() {
		NACK_BIT = 1;
		addProperty("NACK_BIT",1);

	}

	public boolean IsDATA_BIT() {
		return DATA_BIT==1;
	}

	public void setDATA_BIT() {
		DATA_BIT = 1;
		addProperty("DATA_BIT",1);

	}

	public boolean IsOPEN_SESSION_BIT() {
		return OPEN_SESSION_BIT==1;
	}

	public void setOPEN_SESSION_BIT() {
		OPEN_SESSION_BIT = 1;
		addProperty("OPEN_SESSION_BIT",1);

	}


	public void setAsInvoker() {
		INVOKER = 1;
		addProperty("INVOKER",1);

	}

	public boolean IsInvoker() {
		return INVOKER==1;
	}


	public int getSession() {
		return sessionId;
	}


	public String[] getMissingPackets() {
		try {
			return  new String(data, "UTF-8").split(",");
		} catch (UnsupportedEncodingException e) {
		throw new ScenarioUndefined("getMissingPackets called on message: "+this);
		}
	}


	public void setMissing(String[] seq) {
		String ans=seq[0]+"";
		for (int i = 1; i < seq.length; i++) {
			ans+=","+seq[i];
		}
		data= ans.getBytes();
		addProperty("data",data);
	}

	public void setNextExpected(int max_Seq) {
		data=Integer.toBinaryString(max_Seq).getBytes();
		addProperty("data",data);
	}

	public int getNextExpected() {
		return Integer.parseInt(new String(data),2);
	}

	public void setConfiramtionSeq(int max_Seq) {
		data2=Integer.toBinaryString(max_Seq).getBytes();
		addProperty("data2",data2);
	}

	public int getConfiramtionSeq() {
		return Integer.parseInt(new String(data2),2);
	}



	public void setNoramlData(byte[] bs) {
		data=bs;
		addProperty("data",data);

	}

	public byte[] getNormalData(){
		return data;
	}


	@Override
	public void addProperty(String key, Object value) throws SimError {
		if (this.properties !=null && properties.containsKey(key)){
			properties.remove(key);
		}
		super.addProperty(key, value);
	}

 


	public void setSize() {
		size=0;
		if (data!=null){
			size+=data.length;
		}
		if (data2!=null){
			size+=data2.length;
		}
		
	}


	

	@Override
	public int compare(DtsnMessage o1, DtsnMessage o2) {
		if (o1.getSeqNum()<o2.getSeqNum()){
			return -1;
		}
		else{
			return 1;
		}
	}


	public boolean equles(Object o){
		return ((DtsnMessage)(o)).getSeqNum()==this.SeqNum;
	}

	@Override
	public String toString() {
		String ans= super.toString()+ ", from=" + from + ", to=" + to+" , piggayBagBit=" + piggayBagBit + ", SeqNum=" + SeqNum + ", RTX_BIT=" + RTX_BIT
				+ ", EAR_BIT=" + EAR_BIT + ", ACK_BIT=" + ACK_BIT + ", NACK_BIT=" + NACK_BIT + ", DATA_BIT=" + DATA_BIT
				+ ", INVOKER=" + INVOKER 
				+ ", appID=" + appID + "";
		if (this.IsACK_BIT())
			ans=ans+ ", ConfiramtionSeq=" + getConfiramtionSeq() + "";
		if (this.IsNACK_BIT())
			ans=ans+ ", MissingPackets=" + Arrays.toString(getMissingPackets()) + "";

		return ans;
	}
}
