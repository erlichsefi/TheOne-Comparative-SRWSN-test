package stwsn.tools;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import core.DTNHost;
import core.Message;
import dtsn.tools.DtsnMessage;

public class StwsnMessage extends DtsnMessage {
	private static final long serialVersionUID = 1L;
	private String PacketMac=null;
	private String[][] NACK_LEAF_VALUE;
	private byte[][][] NACK_SIB_VALUE;

	private String Ack_Value;
	private byte[] data3;



	public StwsnMessage(DTNHost host, DTNHost otherhost, String string, int size, int sessionId, int seqNum) {
		super(host,otherhost,string, size,  sessionId, seqNum);
	}

	public StwsnMessage(StwsnMessage messageToValite) {
		copyFrom(messageToValite);
	}

	public static void openKeyManger(int numberofpacket){
		StwsnKeyManger.initStwsnKeyManger(numberofpacket);
	}

	public static boolean ValiteKeyManger(int numberofpacket,String hashcainR,String[] treeRoot ){
		return (StwsnKeyManger.HashChainResult.equals(hashcainR) && StwsnKeyManger.numberOfDataPacket==numberofpacket && Arrays.equals(treeRoot, StwsnKeyManger.getTreeRoot()));
	}

	public StwsnMessage(DTNHost from, DTNHost to, String id, int size, int sessionId, int seqNum, String object) {
		super( from,  to,  id,  size, sessionId,  seqNum);
		this.PacketMac=object;

	}

	@Override
	public StwsnMessage replicateRTX() {
		StwsnMessage m = new StwsnMessage(from, to, "C"+id, size,sessionId,this.SeqNum);
		m.copyFrom(this);
		return m;
	}

	/**
	 * Deep copies message data from other message. If new fields are
	 * introduced to this class, most likely they should be copied here too
	 * (unless done in constructor).
	 * @param m The message where the data is copied
	 */
	protected void copyFrom(StwsnMessage m) {
		this.path = new ArrayList<DTNHost>(m.path);
		this.timeCreated = m.timeCreated;
		this.responseSize = m.responseSize;
		this.requestMsg  = m.requestMsg;
		this.initTtl = m.initTtl;
		this.appID = m.appID;
		appID=m.appID;
		this.data2=m.data2;
		this.data3=m.data3;
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
		this.PacketMac=m.PacketMac;
		this.NACK_LEAF_VALUE=m.NACK_LEAF_VALUE;
		this.NACK_SIB_VALUE=m.NACK_SIB_VALUE;

		this.Ack_Value=m.Ack_Value;
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
	@Override
	public Message replicate() {
		StwsnMessage m = new StwsnMessage(from, to, id, size,sessionId,this.SeqNum,this.PacketMac);
		m.copyFrom(this);
		return m;
	}

	@Override
	public void setDATA_BIT() {
		DATA_BIT = 1;
		PacketMac=StwsnKeyManger.HashMessage(this);
		addProperty("PacketMac",PacketMac);
		addProperty("DATA_BIT",1);

	}

	@Override
	public void setMissing(String[] seq) {
		super.setMissing(seq);
		int min_missing=Integer.parseInt(seq[0]);
		NACK_LEAF_VALUE=new String[seq.length][];
		NACK_SIB_VALUE=new byte[seq.length][][];

		for (int i = 0; i < seq.length; i++) {
			NACK_SIB_VALUE[i]=StwsnKeyManger.getSiblingOfLeaf(Integer.parseInt(seq[i]));
			NACK_LEAF_VALUE[i]=StwsnKeyManger.getLeafValues(Integer.parseInt(seq[i]));	
			min_missing=Math.min(min_missing, Integer.parseInt(seq[i]));

		}
		addProperty("NACK_LEAF_VALUE",NACK_LEAF_VALUE);

		addProperty("NACK_SIB_VALUE",NACK_SIB_VALUE);

		Ack_Value=StwsnKeyManger.getChainHashOf(Integer.parseInt(seq[0])-1);
		addProperty("Ack_Value",Ack_Value);

		super.setConfiramtionSeq(Integer.parseInt(seq[0])-1);
	}

	public String getLeafBrotherValue(int i){
		return NACK_LEAF_VALUE[i][1];
	}
	public String getLeafValue(int i){
		return NACK_LEAF_VALUE[i][0];
	}

	public byte[][] getSiblingsValue(int i){
		return NACK_SIB_VALUE[i];
	}
	
	@Override
	public void setConfiramtionSeq(int max_Seq) {
		super.setConfiramtionSeq(max_Seq);
		Ack_Value=StwsnKeyManger.getChainHashOf(max_Seq-1);
		addProperty("Ack_Value",Ack_Value);
	}

//	@Override
//	public void setNextExpected(int max_Seq) {
//		super.setNextExpected(max_Seq);
//		System.out.println("!");
//		Ack_Value=StwsnKeyManger.getChainHashOf(max_Seq);
//		addProperty("Ack_Value",Ack_Value);
//	}

	@Override
	public void setOPEN_SESSION_BIT() {
		OPEN_SESSION_BIT = 1;
		addProperty("OPEN_SESSION_BIT",1);

	}



	public void setValueForAuto() {
		data=StwsnKeyManger.getHashchain().getBytes();
		addProperty("data",data);
		String a=RoottoString(StwsnKeyManger.getTreeRoot());
		data2=a.substring(0, a.length()).getBytes();
		addProperty("data2",data2);

	}

	private String RoottoString(String[] treeRoot) {
		String ans=treeRoot[0];
		for (int i = 1; i < treeRoot.length; i++) {
			ans=ans+"#"+treeRoot[i];
		}
		return ans;
	}



	public String getHashChainValue(){
		try {
			return  new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}

	public String[] getMerkleTreeValue(){
		try {
			return  new String(data2, "UTF-8").split("#");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected StwsnMessage MakeAcopyToHash(StwsnMessage messageToValite){
		StwsnMessage copyWithoutKey=new StwsnMessage(messageToValite);
		copyWithoutKey.uniqueId=0;
		copyWithoutKey.PacketMac=null;
		copyWithoutKey.timeCreated=0;
		copyWithoutKey.timeReceived=0;
		copyWithoutKey.sessionId=0;
		copyWithoutKey.EAR_BIT=0;
		copyWithoutKey.RTX_BIT=0;
		return copyWithoutKey;
	}


	public boolean validationAck(int hash){
		boolean an=StwsnKeyManger.HashFromDepth(Ack_Value,hash-1).equals(StwsnKeyManger.HashChainResult);
		return an;

	}

	public boolean validationNAck(int i,DtsnMessage m){
		return (!StwsnKeyManger.getTreeRoot(getLeafValue(i),getLeafBrotherValue(i),getSiblingsValue(i)).equals(StwsnKeyManger.getTreeRoot()));

	}

	public boolean ValidateHash(){
		return PacketMac.equals(StwsnKeyManger.HashMessage(MakeAcopyToHash(this)));

	}



	public void setNumberOfExpectedPackets(int numberOfDataPackt) {
		data3=new byte[1];
		data3=Integer.toBinaryString(numberOfDataPackt).getBytes();
		addProperty("data3",data3);
	}

	public int getNumberOfExpectedPackets(){
		return Integer.parseInt(new String(data3),2);

	}



	public void setHash() {
		this.PacketMac=StwsnKeyManger.HashMessage(MakeAcopyToHash(this));
		addProperty("PacketMac",PacketMac);

	}




	public DtsnMessage removeElementLowerEquleThen(int confiramtionSeq) {
		String[] missing=getMissingPackets();
		int i=0;
		while (i<missing.length && Integer.parseInt(missing[i])<confiramtionSeq){
			i++;
		}
		super.setMissing(Arrays.copyOfRange(missing, i, missing.length));
		NACK_SIB_VALUE=Arrays.copyOfRange(NACK_SIB_VALUE, i, NACK_SIB_VALUE.length);
		NACK_LEAF_VALUE=Arrays.copyOfRange(NACK_LEAF_VALUE, i, NACK_LEAF_VALUE.length);	
		return this;
	}

	@Override
	public void setSize() {
		super.setSize();
		if (this.PacketMac!=null){
			size+=PacketMac.getBytes().length;
		}
		if (this.Ack_Value!=null){
			size+=Ack_Value.getBytes().length;
		}
		if (this.NACK_LEAF_VALUE!=null){
			for (int i = 0; i < NACK_LEAF_VALUE.length; i++) {
				size+=NACK_LEAF_VALUE[i].length;

			}
		}
		if (this.NACK_SIB_VALUE!=null){
			for (int i = 0; i < NACK_SIB_VALUE.length; i++) {
				for (int j = 0; j < NACK_SIB_VALUE[i].length; j++) {
					size+=NACK_SIB_VALUE[i][j].length;
				}   
			}
		}
		if (this.data3!=null){
			size+=data3.length;

		}
	}
}
