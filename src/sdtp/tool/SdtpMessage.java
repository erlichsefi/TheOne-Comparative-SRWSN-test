package SDTP.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import core.DTNHost;
import core.Message;
import dtsn.tools.DtsnMessage;

public class SdtpMessage extends DtsnMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * With the Seq Num
	 */
	protected String AckMac=null;
	/**
	 * With the Seq Num
	 */
	protected String NackMac=null;
	/**
	 * With the Seq Num (for nack confirmation and ack )
	 */
	protected String ACK_KEY=null;
	/**
	 * TO BE USED WHEN SENDING NACK
	 */
	protected String[] NACK_KEY=null;

	public SdtpMessage(DTNHost from, DTNHost to, String id, int size, int session,int seqnum) {
		super(from, to, id, size, session,seqnum);

	}

	/**
	 * Constructor to be used in copying to prevent re hashing
	 * @param from
	 * @param to
	 * @param id
	 * @param size
	 * @param session
	 * @param seqnum
	 * @param _AckMac
	 * @param _NackMac
	 */
	private SdtpMessage(DTNHost from, DTNHost to, String id, int size, int session,int seqnum,String _AckMac,String _NackMac) {
		super(from, to, id, size, session,seqnum);
		AckMac=_AckMac;
		NackMac=_NackMac;

	}


	/**
	 * Deep copies message data from other message. If new fields are
	 * introduced to this class, most likely they should be copied here too
	 * (unless done in constructor).
	 * @param m The message where the data is copied
	 */
	protected void copyFrom(SdtpMessage m) {
		this.path = new ArrayList<DTNHost>(m.path);
		this.timeCreated = m.timeCreated;
		this.responseSize = m.responseSize;
		this.requestMsg  = m.requestMsg;
		this.initTtl = m.initTtl;
		this.appID = m.appID;
		appID=m.appID;
		this.data2=m.data2;

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
		this.ACK_KEY=m.ACK_KEY;
		this.NACK_KEY=m.NACK_KEY;
		this.AckMac=m.AckMac;
		this.NackMac=m.NackMac;
		if (m.properties != null) {
			Set<String> keys = m.properties.keySet();
			for (String key : keys) {
				updateProperty(key, m.getProperty(key));
			}
		}
	}


	@Override
	public SdtpMessage replicateRTX() {
		SdtpMessage m = new SdtpMessage(from, to, "C"+id, size,sessionId,this.SeqNum);
		m.copyFrom(this);
		return m;
	}
	/**
	 * Returns a replicate of this message (identical except for the unique id)
	 * @return A replicate of the message
	 */
	public Message replicate() {
		SdtpMessage m = new SdtpMessage(from, to, id, size,sessionId,SeqNum,AckMac,NackMac);
		m.copyFrom(this);
		return m;
	}

	@Override
	public void setDATA_BIT() {
		super.setDATA_BIT();
		//extended with mac ack,mack nack
		SdtpMessage localCopy=MakeAcopyToHash(this);
		try {
			String tempAckMac=SdtpKeyManger.BuildMacAck(localCopy);
			String tempNackMac=SdtpKeyManger.BuildMacNack(localCopy);
			AckMac=tempAckMac;
			NackMac=tempNackMac;
		} catch (IOException e) {
			e.printStackTrace();
		}
		addProperty("NackMac",NackMac);
		addProperty("AckMac",AckMac);


	}

	@Override
	public void setConfiramtionSeq(int max_Seq) {
		super.setConfiramtionSeq(max_Seq);
		// -1 because message n confirm from 1..n-1 and n isn't is the buffer
		//check @link SdtpSource#OnAckReceived
		ACK_KEY=SdtpKeyManger.getAckKey(max_Seq-1);
		addProperty("ACK_KEY",ACK_KEY);
	}

	@Override
	public void setNextExpected(int max_Seq) {
		/*TODO    NEED A HASH? */
		super.setNextExpected(max_Seq);
		ACK_KEY=SdtpKeyManger.getAckKey(max_Seq);
		addProperty("ACK_KEY",ACK_KEY);
	}
	
	@Override
	public void setMissing(String[] seq) {
		super.setMissing(seq);
		NACK_KEY=new String[seq.length];
		for (int i = 0; i < seq.length; i++) {
			NACK_KEY[i]=SdtpKeyManger.getNackKey(Integer.parseInt(seq[i])).toString();
		}
		addProperty("NACK_KEY",NACK_KEY);

		ACK_KEY=SdtpKeyManger.getAckKey(Integer.parseInt(seq[0]));
		addProperty("ACK_KEY",ACK_KEY);
	}

	public boolean validationAck(SdtpMessage MessageToValite){
		SdtpMessage copyWithoutKey=MakeAcopyToHash(MessageToValite);
		try {
			boolean a= SdtpKeyManger.HashMessage(copyWithoutKey, ACK_KEY).equals(MessageToValite.AckMac);
			if (!a){
				System.out.println();
			}
			return a;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean validationNAck(SdtpMessage MessageToValite,int missing_index){
		SdtpMessage copyWithoutKey=MakeAcopyToHash(MessageToValite);
		try {
			return SdtpKeyManger.HashMessage(copyWithoutKey, NACK_KEY[missing_index]).equals(MessageToValite.NackMac);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}



	protected SdtpMessage MakeAcopyToHash(SdtpMessage MessageToValite){
		SdtpMessage copyWithoutKey=new SdtpMessage(MessageToValite.from,MessageToValite.to,MessageToValite.id,MessageToValite.size,MessageToValite.sessionId,MessageToValite.SeqNum,null,null);
		copyWithoutKey.uniqueId=0;
		copyWithoutKey.ACK_KEY=null;
		copyWithoutKey.NACK_KEY=null;
		copyWithoutKey.timeCreated=0;
		copyWithoutKey.timeReceived=0;
		copyWithoutKey.sessionId=0;
		return copyWithoutKey;
	}


	@Override
	public void setSize() {
		super.setSize();
		if (this.AckMac!=null){
			size+=AckMac.getBytes().length;
		}
		if (this.NackMac!=null){
			size+=NackMac.getBytes().length;
		}
		if (this.ACK_KEY!=null){
			size+=ACK_KEY.getBytes().length;
		}
		if (this.NACK_KEY!=null){
			for (int i = 0; i < NACK_KEY.length; i++) {
				size+=NACK_KEY[i].getBytes().length;

			}
		}
	}
}
