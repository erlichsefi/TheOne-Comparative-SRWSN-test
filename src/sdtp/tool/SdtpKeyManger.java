package SDTP.tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import dtsn.Values;
import dtsn.tools.DtsnMessage;



public class SdtpKeyManger {
	
	static  int key_length=128;

	static  byte[] Ack_master__key=Values.SDTP_ACK_MASTER_KEY;

	static  byte[] Nack_master__key=Values.SDTP_NACK_MASTER_KEY;



	public static int getKetsLength(){
		return key_length;
	}

	/**
	 * for nack confirmation and ack
	 * @param ACK_KEY
	 * @param ACK_MCK_FROM_DATA_MESSAGE
	 * @return
	 */
	public  static String getAckKey(int Seq){
		return Arrays.toString(Some_pseudo_random_function(Ack_master__key,"per packet ACK KEY",Seq));

	}

	
	/**
	 * for nack confirmation and ack
	 * @param ACK_KEY
	 * @param ACK_MCK_FROM_DATA_MESSAGE
	 * @return
	 */
	public static String getNackKey(int Seq){
		return Arrays.toString(Some_pseudo_random_function(Nack_master__key,"per packet NACK KEY",Seq));

	}

	protected static byte[] Some_pseudo_random_function(byte[] k,String opersion,int sessionid){
		byte[] ans=new byte[128];
		byte[] StringByte=opersion.getBytes();
		byte SessionIdByte=new Integer(sessionid).byteValue();
		for (int i=0 ,j= 0; i < k.length || j < StringByte.length; ) {
			k[i%k.length]= (k[i%k.length]==1 || StringByte[j%StringByte.length]==1) ? new Integer(1).byteValue():new Integer(0).byteValue();
			j++;
			i++;
		}
		return ans;
	}



	public static String BuildMacAck(DtsnMessage m) throws IOException{
		int SeqNum=m.getSeqNum();
		return HAMC256(Arrays.toString(serialize(m)),Arrays.toString(Some_pseudo_random_function(Ack_master__key,"per packet ACK KEY",SeqNum)));

	}
	
	public static String BuildMacNack(DtsnMessage m) throws IOException{
		int SeqNum=m.getSeqNum();
		return HAMC256(Arrays.toString(serialize(m)),Arrays.toString(Some_pseudo_random_function(Nack_master__key,"per packet NACK KEY",SeqNum)));
	}
	
	
	public static String HashMessage(DtsnMessage m,String key) throws IOException{
		String a=Arrays.toString(serialize(m));
		String mac= HAMC256(a,key);
		return mac;
	}
	
	
	private static  byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            byte[] t= b.toByteArray();
            return t;
        }
    }
	
	protected static  String HAMC256(String secret,String message){
		String hash=null;
		try {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		
			sha256_HMAC.init(secret_key);
		 hash = Base64.encode(sha256_HMAC.doFinal(message.getBytes()));
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash;
	}

}
