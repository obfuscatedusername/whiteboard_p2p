//package simplewhiteboard;
//
//import java.net.*;
//import java.io.*;
//import java.util.*;
//
//
///**
// *
// * @author tdw10kcu
// */
//public class nodeBroadcaster {
//    
//    //
//    
//    Thread sender, joinListener, joiner, acceptListener;
//    
//    //
//    
//    Scanner             input;
//    DatagramSocket      dSocket;
//    ServerSocket        sSocket;
//    DataOutputStream    output;
//    Socket              sConnection;
//    ObjectOutputStream  oOutput;
//    
//    //
//    String              mcip;
//    String              ip;
//    String              msg;
//    int                 gPort; //group port
//    int                 sPort; //socket port
//    int                 tcpPort;
//    
//    //
//    String              ID;
//    thePeer             tPeer;
//    
//    nodeBroadcaster(thePeer p){
//        tPeer           = p;
//        joiner          = new Thread(joinSenderStatus);
//        joinListener    = new Thread(checkJoinStatus);
//        acceptListener  = new Thread(checkAcceptStatus);
//        input           = new Scanner(System.in);
//        msg = ip        = "";
//        gPort           = 64121;
//       // sPort           = 64121;
//        tcpPort         = 64126;
//        mcip            = "224.0.132.0";
//    
//        //Try creating a Datagram socket
//        try{
//            dSocket = new DatagramSocket();
//        }catch(SocketException e){
//            System.err.println("Couldn't create Datagram socket");
//            e.printStackTrace();
//        }
//        
//        //Try creating a Server socket
//        try{
//            sSocket = new ServerSocket(tcpPort, 1);
//        }catch(IOException e){
//            System.err.println("Couldn't create Server socket");
//            e.printStackTrace();
//        }
//        
//        try{
//            ip = InetAddress.getLocalHost().getHostAddress();
//        }catch(UnknownHostException e){
//            System.err.println("Could not find host!");
//            e.printStackTrace();
//        }
//        ID = String.valueOf(1 + (int)(Math.random() * ((100 - 1)+1)));
//        tPeer.nl.dPacket.id = ID;
//      //  System.out.println("ID = "+ID);
//    }
//    
//    public void send(String m)throws Exception{
//        byte[] sendMsg = m.getBytes();
//        InetAddress ia = InetAddress.getByName(mcip);
//        DatagramPacket dPacket = new DatagramPacket(sendMsg, sendMsg.length, ia, gPort);
//        try{
//            System.out.println("Sending Message: "+m);
//            dSocket.send(dPacket);
//        }catch(Exception e){
//            e.printStackTrace();
//            System.err.println("couldn't send message!");
//        }finally{System.out.println("Message send Success!");}
//        dSocket.setSoTimeout(3000);
//    }
//    
//    public String createMsg(String type, String ip, String xPos, String yPos, String id){
//        String[] msg = new String[5];
//        if(type !=null)msg[0] = type;       else msg[0] = "type";
//        if(ip   !=null)msg[1] = ip;         else msg[1] = "ip"; 
//        if(!xPos.isEmpty())msg[2] = xPos;   else msg[2] = "000";
//        if(!yPos.isEmpty())msg[3] = yPos;   else msg[3] = "000";  
//        msg[4] = ID;
//       //create a string from the char array and return it 
//       String temp = new String(msg[0]+","+msg[1]+","+msg[2]+","+msg[3]+","+msg[4]+",");
//       return temp;
//    }
//    
//    //create a thread to send join requests 
//    Runnable joinSenderStatus = new Runnable(){ //joiner
//    @Override
//    public void run(){
//            System.out.println("JoinSenderStatus Thread Started..");
//            tPeer.nl.receiverThread.start();
//            acceptListener.start();
//            try{
//                Thread.sleep(2000);
//            }catch(InterruptedException e){
//                System.err.println("Could not interrupt joinSenderStatus");
//                e.printStackTrace();
//                return;
//            }
//            try{
//                String msg = createMsg("join",ip,"","", ID);
//                send(msg);
//                System.out.println("Sent join message successfully: " +msg);
//            }catch(Exception e){
//                System.err.println("ERROR: Could not send join message");
//                e.printStackTrace();
//            }
//            joinListener.start();
//            try{
//                Thread.sleep(10000);
//                if(!tPeer.nl.accepted){
//                    System.out.println("Join Sender Finished");
//                }
//            }catch(InterruptedException e){
//                System.err.println("Thread already interrupted");
//                e.printStackTrace();
//            }
//      }
//    };
//    
//    //create a thread to check if there are any join requests
//    Runnable checkJoinStatus = new Runnable(){ //joinListener
//      @Override
//      public void run(){
//        System.out.println("checkJoinStatus Thread Started");
//          while(true){
//              try{
//                  Thread.sleep(3000);
//              }catch(InterruptedException e){
//                  System.err.println("ERROR: Could not interrupt "
//                          + "+checkJoinStatus");
//                  e.printStackTrace();
//              }
//              if(tPeer.nl.join){
//                  if(joiner.isAlive()){
//                      joiner.interrupt();
//                      System.out.println("Join sender interrupted");
//                  }
//                acceptListener.interrupt();
//                System.err.println("acceptListener has been interrupted");
//                try{
//                    String msg = createMsg("accept", ip, "", "", ID);
//                    send(msg);
//                    tPeer.nl.join = false;
//                }catch(Exception e){
//                    System.err.println("Could not send message from: "
//                            + "checkJoinStatus");
//                    e.printStackTrace();
//                }
//              }
//
//          }
//      }
//    };//end of checkJoinStatus thread
//    
//    //create a thread to check if any join requests have been accepted
//    Runnable checkAcceptStatus = new Runnable(){ //acceptListener
//      @Override
//      public void run(){
//        System.out.println("checkAcceptStatus Thread Started");
//          while(!Thread.currentThread().isInterrupted()){
//              if(tPeer.nl.accepted){
//                  try{
//                      if(joiner.isAlive()){
//                          joiner.interrupt();
//                          System.out.println("Join sender interrupted");
//                      }
//                      return;
//                  }catch(Exception e){
//                      e.printStackTrace();
//                  }
//              }
//          }
//      }
//    };//end of checkAcceptStatus thread
//    
//    public void initTCP(){
//        Thread initTCP = new Thread(initTCPRunnable);
//        initTCP.start();
//        try{
//            initTCP.join(10000);
//        }catch(InterruptedException e){
//            e.printStackTrace();
//        }
//        if(initTCP.isAlive()){
//            System.out.println("Timmy");
//        }else{
//            Thread senderTCP = new Thread(senderTCPRunnable);
//            senderTCP.start();
//        }
//    }//end of initTCP();
//    
//    Runnable initTCPRunnable = new Runnable(){
//        @Override
//        public void run(){
//            System.out.println("Initialising the TCP");
//            try{
//                System.out.println("Connecting....");
//                sConnection = sSocket.accept();
//                System.out.println("Connection established w/:"
//                        + sConnection.getInetAddress().getHostName());
//                output = new DataOutputStream(sConnection.getOutputStream());
//                oOutput = new ObjectOutputStream(sConnection.getOutputStream());
//                
//            }catch(IOException e){
//                e.printStackTrace();
//            }
//        }        
//    };//end of iniTCPRunnable
//    
//    
//    Runnable senderTCPRunnable = new Runnable(){
//      @Override
//      public void run(){
//          
//      }        
//    };//end of senderTCPRunnable
//    
//    
//}//end of class
//
