package stwsn.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import dtsn.Values;
import dtsn.tools.DtsnMessage;


public class StwsnKeyManger  {
	static double numberOFmrekleTrees=Values.STWSN_NUM_OF_MERKLE_TREE;
	static int numberOfLeafInAtree;
	static final byte[] K_ack_key=Values.SDTP_ACK_MASTER_KEY;
	static final byte[] K_nack_key=Values.SDTP_NACK_MASTER_KEY;
	static int numberOfDataPacket;
	static int[] tree_high=new int[1];
	static String HashChainResult=null;
	static MerkleTree[] MT;
	


	public static void initStwsnKeyManger(int m){
		numberOfDataPacket=m;
		HashChainResult=HashFromDepthToDepth(Arrays.toString(K_ack_key),numberOfDataPacket+1);
		String[] LeafV=ComputeLeafSignatures(m);
		double d=Math.floor(LeafV.length/numberOFmrekleTrees);
		numberOfLeafInAtree=(int) Math.ceil(d);
		MT=new MerkleTree[(int) numberOFmrekleTrees];
		for (int i = 0; i < numberOFmrekleTrees-1; i++) {
			List<String> l=Arrays.asList(Arrays.copyOfRange(LeafV, i*numberOfLeafInAtree, (i+1)*numberOfLeafInAtree));
			MT[i]= new MerkleTree(l);
		}
		List<String> l=Arrays.asList(Arrays.copyOfRange(LeafV, (int)(numberOFmrekleTrees-1)*numberOfLeafInAtree, LeafV.length-1));
		MT[MT.length-1]= new MerkleTree(l);

	}

	private static byte[] serialize(Object obj) throws IOException {
		try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
			try(ObjectOutputStream o = new ObjectOutputStream(b)){
				o.writeObject(obj);
			}
			byte[] t= b.toByteArray();
			return t;
		}
	}

	private  static String HAMC256(String secret){
		String hash=null;
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

			sha256_HMAC.init(secret_key);
			hash = Base64.encode(sha256_HMAC.doFinal());
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hash;
	}


	


	public static String HashFromDepthToDepth(String hash, int numberOfTimesToHash){
		for (int i = 0; i < numberOfTimesToHash; i++) {
			hash=HAMC256(hash);
		}
		return hash;
	}
	
	public static String HashFromDepth(String hash, int numberOfTimesToHash){
		for (int i = 0; i < numberOfDataPacket+1-numberOfTimesToHash; i++) {
			hash=HAMC256(hash);
		}
		return hash;
	}




	/**
	 * creareing the init values of the leafs 
	 * @param num number of data packets
	 * @return
	 */
	private static String[] ComputeLeafSignatures(int num){
		String[] ans=new String[num];
		for (int i = 0; i < ans.length; i++) {
			ans[i]=HAMC256(Arrays.toString(Some_pseudo_random_function1(K_nack_key,"nack per packet",i+1)));
		}
		return ans;
	}


	protected static byte[] Some_pseudo_random_function1(byte[] k,String opersion,int sessionid){
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


	public static byte[] ComputeRootWithSibling(String leafSigh,String OtherLeafSign,byte[][] nodesByOrder){
	return new MerkleTree(leafSigh,OtherLeafSign,Arrays.asList(nodesByOrder)).getRoot().sig;
	}

	public static byte[][] getSiblingOfLeaf(int leafNumber){
		try{
		byte[][] sib=MT[TreeIndex(leafNumber)].getSiblings(leafNumber%numberOfLeafInAtree);
		return sib;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getLeafSign(int index){
		return MT[TreeIndex(index)].getLeafSign(index%numberOfLeafInAtree);
	}





	public static String getChainHashOf(int i) {
		return HashFromDepthToDepth(Arrays.toString(K_ack_key),i);
	}


	public static String HashMessage(DtsnMessage m){
		String a=null;
		try {
			 a=HAMC256(Arrays.toString(serialize(m)));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return a;
	}

   public static int TreeIndex(int i){
	   int index=1;
	   while (index*numberOfLeafInAtree<=i){
		   index++;
	   }
	   if ((index-1)==MT.length){
		   index=index-1;
	   }
	   return (index-1);
   }

	public static String[] getTreeRoot() {
		String[] roots=new String[(int) numberOFmrekleTrees];
		for (int i = 0; i < roots.length; i++) {
			String r=Arrays.toString(MT[i].getRoot().sig);
			roots[i]=r.substring(0, r.length());
		}
		return roots;
	}

	public static String[] getLeafValues(int parseInt) {
		String ans[] =new String[2];
		ans[0]=MT[TreeIndex(parseInt)].getLeafSign(parseInt%numberOfLeafInAtree);
		ans[1]=MT[TreeIndex(parseInt)].getBrotherLeaf(parseInt%numberOfLeafInAtree);;
		return ans;
	
	}


	public static String getTreeRoot(String hashMessage, String leafBrotherValue, byte[][] siblingsValue) {
		return Arrays.toString(new MerkleTree(hashMessage,leafBrotherValue,Arrays.asList(siblingsValue)).getRoot().sig);
	}

	public static String getHashchain() {
		return HashChainResult;
	}
}
