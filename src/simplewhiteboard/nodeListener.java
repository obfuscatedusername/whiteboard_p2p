//package simplewhiteboard;
//import java.net.*;
//import java.io.*;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
///**
// *
// * @author tdw10kcu
// */
//
//class theMsg{
//    int     x, y;
//    String  ip, id, type;    
//    void set(){
//        x = y = 0;
//        type = ip = id = "";
//    }
//}
//
//class datagramPacket{
//    String  ip, id, name, msg, mcip;
//    int     gPort, sPort, tcpPort;
//    void set(){
//        ip = id = name = msg = mcip = "";
//        gPort = sPort = tcpPort = 0;
//    }
//}
//
//public class nodeListener {
//    
//    Thread senderThread, receiverThread;
//   
//    //input & output
//    Scanner             input;
//    BufferedReader      bReader;
//    ObjectInputStream   objInput;
//    byte[]              buffer;
//    
//    //other class objects
//    datagramPacket      dPacket;
//    thePeer             peer;
//    String              jIP, aIP; //the joiner and accepter IP
//    boolean             accepted, join;
//    Socket              tcpSocket;
//    MulticastSocket     mSocket;
//    InetAddress         ia ;
//    
//    //Default constructor    
//    nodeListener(thePeer p){
//        peer            = p;
//        receiverThread  = new Thread(theReceiver);
//        input           = new Scanner(System.in);
//        dPacket         = new datagramPacket(); //create a new datagramPacket object
//        dPacket.set(); //set the packets fields to empty, 0 etcetera
//        dPacket.gPort   = 64121;
//        //dPacket.sPort   = 64121;
//        dPacket.tcpPort = 64126;
//        dPacket.mcip    = "224.0.132.0";
//        accepted        = false;
//        join            = false; 
//        
//        try{
//            dPacket.ip      = InetAddress.getLocalHost().getHostAddress();
//            dPacket.name    = InetAddress.getLocalHost().getHostName();
//        }catch(UnknownHostException e){
//            System.err.println("ERROR: Could not resolve Host name or address!");
//            e.printStackTrace();
//        }
//        
////        try{
////            mSocket     = new MulticastSocket(dPacket.gPort);
////            ia          = InetAddress.getByName(dPacket.mcip);
////        }catch (IOException e){
////            System.err.println("Could not create multicast socket");
////            e.printStackTrace();
////        }
//        
//    }
//    
//    public void receiveMCMsg() throws Exception{
//        //  mSocket = new MulticastSocket(dPacket.gPort);
//        mSocket     = new MulticastSocket(dPacket.gPort);
//        ia          = InetAddress.getByName(dPacket.mcip);
//        mSocket.joinGroup(ia);
//        buffer                  = new byte[40];
//        DatagramPacket dp       = new DatagramPacket(buffer, buffer.length);
//        try{
//            System.out.println("Ready to receive");
//            mSocket.receive(dp);
//            System.out.println("Recevied Message");
//            String rMsg = new String(buffer);
//            theMsg rm   = processReceivedMSG(rMsg);
//            
//            //If the message type is a join message && the message does 
//            //not originate from my ID
//            System.out.println("ID1 = "+rm.id+" & ID2 = "+dPacket.id);
//            if("join".equals(rm.type) && !rm.id.equalsIgnoreCase(dPacket.id)){
//                jIP     = rm.ip;
//                join    = true;
//                System.out.println("Join request from: "+ jIP);
//            }else{System.err.println("Ignoring message originating from me");}
//            
//            //if the message type is an accepter messager && the message
//            //does not originate from my ID
//            if("accept".equals(rm.type) && !rm.id.equalsIgnoreCase(dPacket.id)){
//                aIP         = rm.ip;
//                accepted    = true;
//                System.out.println("Join request Accepted by: "+aIP);
//                peer.isJoined = true;
//                initTCP();
//            }else{System.err.println("Ignoring message originating from me");}
//        }catch(SocketTimeoutException e){
//            System.err.println("Timed out!");
//            e.printStackTrace();
//        }
//    }//end of receiveMCMsg()
//    
//    public theMsg processReceivedMSG(String rm) {
//        theMsg temp = new theMsg();
//        temp.set();
//        String[] ts = rm.split(",");
//        System.out.println("Received message contents: "+ts[0]+","+ts[1]+","+ts[2]+","+ts[3]+","+ts[4]);
//        temp.type   = ts[0];
//        temp.ip     = ts[1];
//        if(ts[2].length() > 0)
//            temp.x      = Integer.parseInt(ts[2]);
//        else
//            temp.x = 0;
//        if(ts[3].length() > 0)
//            temp.y      = Integer.parseInt(ts[3]);
//        else
//            temp.y = 0;
//        temp.id = ts[4];
//        return temp;
//    }//end of processReceivedMSG()
//    
//    //create a Thread that can listen for any received MultiCast Messages
//    Runnable theReceiver = new Runnable(){
//        @Override
//        public void run(){
//            System.out.println("Receiver Thread Started");
//            while(!Thread.currentThread().isInterrupted()){
//                try{
//                    receiveMCMsg();
//                }catch(Exception e){
//                    System.err.println("Receiver Thread: "
//                            + "Message receive error!");
//                    e.printStackTrace();
//                }
//            }
//            System.err.println("Receiver Thread Interrupted");
//        }
//    }; // end of theReceiver
//    
//    
//    public void initTCP(){
//    try{
//        tcpSocket = new Socket(aIP, dPacket.tcpPort);
//        System.out.println("Successfully connected to: "
//                            +tcpSocket.getInetAddress().getHostName());
//        bReader = new BufferedReader(new InputStreamReader
//                            (tcpSocket.getInputStream()));
//        objInput = new ObjectInputStream(tcpSocket.getInputStream());
//    }catch (IOException e){
//        System.err.println("ERROR: could not initTCP()");
//        e.printStackTrace();
//    }
//    Thread tcpReceiver = new Thread(tcpReceiverRunnable);
//    tcpReceiver.start();
//    System.out.println("tcpReceiver has begun");
//    }//end of initTCP()
//    
//    
//    Runnable tcpReceiverRunnable = new Runnable(){
//        @Override
//        public void run(){
//            while(true){
//                System.out.println("Receiving TCP Packet");
//                try{
//                    Object ro1 = objInput.readObject();
//                    Object ro2 = objInput.readObject();
//                }catch(IOException e){
//                    e.printStackTrace();
//                } catch (ClassNotFoundException ex) {
//                    Logger.getLogger(nodeListener.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//    };//end of tcpListener
//}//end of nodeListener class
