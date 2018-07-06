package core;

import dtsn.Values;
import input.DtsnOneToEachMessageGenerator;
import report.DtsnAppReporter;
import ui.DTNSimTextUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CsvSim {
    public static  HashMap<String, String> cuurnet = null;
    static int attemptLimit=1;
    public static void main(String[] args) {
        //runTrees();
        runProtocols();
    }

    public static void runProtocols(){
        HashMap<String, HashMap<String, String>> result = new HashMap<>();

        double[] per = {0.0125,/*0.05, 0.1, 0.125, 0.15*/};
        String[] protocols = {"dtsn", "srwsn", "stdp"};
        int[] numberofpackets = {/*1000,800,500,250,*/100};
        for (double p : per) {
            for (String pro : protocols) {
                for (int pcakets : numberofpackets) {
                    Values.LOST_EVERY_N_PACKET = 1 - p;
                    Values.NUMBER_OF_PACKETS_TO_SEND = pcakets;
                    DtsnOneToEachMessageGenerator.APP_ID = pro;
                    int attempt=0;
                    while (attempt<attemptLimit && cuurnet == null) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(attempt+"/"+attemptLimit+" of Protocol="+pro+" per= "+p+" number of packets= "+pcakets);
                        DTNSim.initSettings(new String[]{null}, 1);
                        Settings.setRunIndex(0);
                        DTNSim.resetForNextRun();
                        new DTNSimTextUI().start();
                        attempt++;

                    }
                    if (cuurnet==null){
                        result.put(p + "," + pro + "," + pcakets,new HashMap<>());
                        System.err.println("------> simultion faild");
                    }
                    else{
                        result.put(p + "," + pro + "," + pcakets, new HashMap<String, String>(cuurnet));
                        System.err.println("------> simultion good");

                    }
                    cuurnet = null;

                }
            }
        }
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
        FileWriter writer=null;
        try {
            writer = new FileWriter("out.csv",true);
            writer.write(dt1.format(new Date())+"\n");
            for (String pro : protocols) {
                for (int pcakets : numberofpackets) {
                    writer.write(pro+"&"+pcakets+",ack_size_sum,nack_size_sum,data_size_sum,ack_size_count,nack_size_count,data_size_count,time");
                    writer.write("\r\n");
                    for (double p : per) {
                        writer.write(p+",");

                        HashMap<String, String> cresult = result.get(p + "," + pro + "," + pcakets);
                        writer.write(cresult.get("ack_size_sum")+",");
                        writer.write(cresult.get("nack_size_sum")+",");

                        writer.write(cresult.get("data_size_sum")+",");

                        writer.write(cresult.get("ack_size_count")+",");
                        writer.write(cresult.get("nack_size_count")+",");
                        writer.write(cresult.get("data_size_count")+",");

                        writer.write(cresult.get("time")+",");

                        writer.write("\r\n");
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


