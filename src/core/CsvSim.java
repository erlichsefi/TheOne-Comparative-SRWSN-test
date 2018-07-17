package core;

import dtsn.Values;
import input.DtsnOneToEachMessageGenerator;
import report.DtsnAppReporter;
import ui.DTNSimTextUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class CsvSim {
    public static  HashMap<String, String> cuurnet = null;
    public static Object[] timerpop=null;
    static int attemptLimit=1;
    static ArrayList<String> ignore_list=new ArrayList<>();
    public static void main(String[] args) {
        //runTrees();
        runProtocols();
    }

    public static void build_ignore(){
        ignore_list.add(key("dtsn",0.025,1000));


        ignore_list.add(key("dtsn",0.00625,800));
        ignore_list.add(key("dtsn",0.0125,800));
        ignore_list.add(key("dtsn",0.025,800));

        ignore_list.add(key("dtsn",0.00625,500));
        ignore_list.add(key("dtsn",0.0125,500));
        ignore_list.add(key("dtsn",0.025,500));

        ignore_list.add(key("dtsn",0.00625,250));
        ignore_list.add(key("dtsn",0.0125,250));
        ignore_list.add(key("dtsn",0.025,250));

        ignore_list.add(key("dtsn",0.00625,100));

        ////////////////////////

        ignore_list.add(key("srwsn",0.00625,1000));
        ignore_list.add(key("srwsn",0.025,1000));
        ignore_list.add(key("srwsn",0.05,1000));

        ignore_list.add(key("srwsn",0.05,800));

        ignore_list.add(key("srwsn",0.00625,500));
        ignore_list.add(key("srwsn",0.0125,500));
        ignore_list.add(key("srwsn",0.025,500));
        ignore_list.add(key("srwsn",0.05,500));


        ignore_list.add(key("srwsn",0.00625,250));
        ignore_list.add(key("srwsn",0.0125,250));
        ignore_list.add(key("srwsn",0.025,250));
        ignore_list.add(key("srwsn",0.05,250));
        ignore_list.add(key("srwsn",0.1,250));

        ignore_list.add(key("srwsn",0.00625,100));
        ignore_list.add(key("srwsn",0.0125,100));
        ignore_list.add(key("srwsn",0.025,100));
        ignore_list.add(key("srwsn",0.05,100));
        ignore_list.add(key("srwsn",0.1,250));


        ///

        ignore_list.add(key("stdp",0.00625,1000));
        ignore_list.add(key("stdp",0.0125,1000));
        ignore_list.add(key("stdp",0.025,1000));
        ignore_list.add(key("stdp",0.05,1000));


        ignore_list.add(key("stdp",0.05,800));
        ignore_list.add(key("stdp",0.00625,800));

        ignore_list.add(key("stdp",0.00625,500));
        ignore_list.add(key("stdp",0.025,500));
        ignore_list.add(key("stdp",0.0125,500));
        ignore_list.add(key("stdp",0.05,500));


        ignore_list.add(key("stdp",0.00625,250));
        ignore_list.add(key("stdp",0.0125,250));
        ignore_list.add(key("stdp",0.025,250));
        ignore_list.add(key("stdp",0.05,250));



        ignore_list.add(key("stdp",0.00625,100));
        ignore_list.add(key("stdp",0.0125,100));
        ignore_list.add(key("stdp",0.05,100));
        ignore_list.add(key("stdp",0.025,100));


    }

    public static String key(String protocol,double per,int packets){
        return  protocol+per+packets;
    }


    public static void runProtocols(){
        SimpleDateFormat dt1 = new SimpleDateFormat("_yyyy_MM_dd_hh_mm_ss");
        FileWriter writer=null;
        try {
            writer = new FileWriter("out1"+dt1.format(new Date())+".csv");

            double[] per = {0.00625,0.0125,0.025,0.05,0.1};
            String[] protocols = {"dtsn", "srwsn", "stdp"};
            int[] numberofpackets = {1000,800,500,250,100};
            for (String pro : protocols) {
                for (int pcakets : numberofpackets) {
                    writer.write(pro+"&"+pcakets+",ack_size_sum,nack_size_sum,data_size_sum,ack_size_count,nack_size_count,data_size_count,time,extra_parm");
                    writer.write("\r\n");
                    for (double p : per) {
                        if (!ignore_list.contains(key(pro,p,pcakets))) {
                            Values.LOST_EVERY_N_PACKET = 1 - p;
                            Values.NUMBER_OF_PACKETS_TO_SEND = pcakets;
                            DtsnOneToEachMessageGenerator.APP_ID = pro;
                            int attempt = 0;
                            while (attempt < attemptLimit && cuurnet == null) {
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                System.out.println(attempt + "/" + attemptLimit + " of Protocol=" + pro + " per= " + p + " number of packets= " + pcakets);
                                DTNSim.initSettings(new String[]{null}, 1);
                                Settings.setRunIndex(0);
                                DTNSim.resetForNextRun();
                                new DTNSimTextUI().start();
                                attempt++;

                            }
                            if (cuurnet == null) {
                                writeOneresult(writer, new HashMap<>(), p + "");
                                System.err.println("------> simultion faild");
                            } else {
                                writeOneresult(writer, cuurnet, p + "");
                                System.err.println("------> simultion good");

                            }
                        }
                        else{
                            System.out.println("ignoreing= "+key(pro,p,pcakets));
                            writeOneresult(writer, new HashMap<>(), p + "");

                        }
                        writer.flush();
                        cuurnet = null;
                        timerpop=null;
                    }
                    writer.write("\r\n");
                }
                writer.write("\r\n");

            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (writer!=null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public static void writeOneresult(FileWriter writer,HashMap<String, String> cresult,String p) throws IOException {
        writer.write(p+",");

        writer.write(cresult.get("ack_size_sum")+",");
        writer.write(cresult.get("nack_size_sum")+",");

        writer.write(cresult.get("data_size_sum")+",");

        writer.write(cresult.get("ack_size_count")+",");
        writer.write(cresult.get("nack_size_count")+",");
        writer.write(cresult.get("data_size_count")+",");

        writer.write(cresult.get("time")+",");
        writer.write(Arrays.toString(timerpop).replace(",",";")+";sim_time"+DTNSimTextUI.expird_time+",");
        writer.write("\r\n");
    }

    public static void runTrees(){
        HashMap<Integer, HashMap<String, String>> result = new HashMap<>();

        int[] number_of_trees = { 1,2,4,6};

        for (int tree : number_of_trees) {
            Values.LOST_EVERY_N_PACKET = 1 - 0.05;
            Values.NUMBER_OF_PACKETS_TO_SEND = 800;
            DtsnOneToEachMessageGenerator.APP_ID = "srwsn";
            Values.STWSN_NUM_OF_MERKLE_TREE=tree;
            int attempt=0;
            while (attempt<attemptLimit && cuurnet == null) {
                System.out.println(attempt+"/"+attemptLimit+" trees=" +tree);
                DTNSim.initSettings(new String[]{null}, 1);
                Settings.setRunIndex(0);
                DTNSim.resetForNextRun();
                new DTNSimTextUI().start();
                attempt++;

            }
            if (attempt<attemptLimit) {
                result.put(tree, cuurnet);
            }
            else{
                result.put(tree,new HashMap<>());
            }
            cuurnet = null;

        }

        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
        FileWriter writer=null;
        try {
            writer = new FileWriter("tree.csv",true);
            writer.write(dt1.format(new Date())+"\n");
            writer.write(Values.NUMBER_OF_PACKETS_TO_SEND+",ack_size_sum,nack_size_sum,data_size_sum,ack_size_count,nack_size_count,data_size_count,time");
            writer.write("\r\n");
            for (int tree : number_of_trees) {
                writer.write(tree+",");

                HashMap<String, String> cresult = result.get(tree);
                writer.write(cresult.get("ack_size_sum")+",");
                writer.write(cresult.get("nack_size_sum")+",");

                writer.write(cresult.get("data_size_sum")+",");

                writer.write(cresult.get("ack_size_count")+",");
                writer.write(cresult.get("nack_size_count")+",");
                writer.write(cresult.get("data_size_count")+",");

                writer.write(cresult.get("time")+",");

                writer.write("\r\n");
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (writer!=null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }



}


