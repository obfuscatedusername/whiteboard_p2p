package simplewhiteboard;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
/**
 * @author tdw10kcu
 *         Listener.java: This class uses three types of network communication, 
 *         UDP, Multi casting and TCP. UDP is used to retrieve draw, text, and 
 *         connect/disconnect instructions and TCP is used to send and receive
 *         the bulk instruction pack that is sent to a client who has joined 
 *         the peer to peer system.
 */

//This small class imitates a struct and holds all variables and fields 
//pertaining to a received message.
class theMsg{
    int     x, y, size;
    String  ip, id, type, font, payload;  
    Color   col;
    void set(){
        x = y = size = 0;
        type = ip = id = font ="";
        col = Color.BLACK;
    }
}

public class Listener {
    private thePeer     p;
    int                 udpPort, tcpPort;
    boolean             isJoined, isAcc, joinReq, running, receive;
    MulticastSocket      ds;
    String              localIP, joinerIP, accIP, hostName, mcIP, peerID;
    MulticastSocket     mcSocket;
    Socket              tcpSocket;
    InetAddress         ia;
    //input & output
    Scanner             input;
    BufferedReader      bReader;
    ObjectInputStream   objInput;
    byte[]              buffer;
    ArrayList<lineInst>   draw;
    
    // Threads
    Thread udpThread, tcpThread;
    
    Listener(thePeer p){
        this.p = p;
        udpPort     = 64121;
        tcpPort     = 55555;
        mcIP        = "224.0.132.0";
        isJoined    = false;
        isAcc       = false;
        joinReq     = false;
        running     = true;
        receive    = true;
        udpThread   = new Thread(udpThreadRunnable);
        tcpThread   = new Thread(tcpThreadRunnable);
        try{
            localIP    = InetAddress.getLocalHost().getHostAddress();
            p.peerIP = localIP;
            hostName    = InetAddress.getLocalHost().getHostName();
        }catch(UnknownHostException e){
            System.err.println("ERROR: Could not resolve Host name or address!");
            e.printStackTrace();
        }
        
    }
    
    //This thread starts a UDP socket and listens for any incoming UDP packets 
    //on the defined port. If a packet is received then it is sent to the
    //processRecievedMSG() fucntion that adds all the information from the packet
    //into a "theMsg" object. Depending on the type of packet the corresponding
    //action is carried out.
    Runnable udpThreadRunnable = new Runnable(){
        @Override
        public void run(){
            InetAddress ia = null;
            try {
                ia = InetAddress.getByName(mcIP);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                ds = new MulticastSocket(udpPort);
                ds.joinGroup(ia);
            } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }            
            System.out.println("Receiver Thread Started");
            while(running){
                // once a packet has been received the while loop returns to here
                try{
                    buffer = new byte[50];
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    ds.receive(dp);
                    String rMsg = new String(buffer);
                    theMsg rm = processReceivedMSG(rMsg);
                    // If the message is a join message request then add the IP
                    // of requester to the list of subscribed IPs and send the 
                    // init package to the joinee
                    if ("join".equals(rm.type) && !rm.ip.equalsIgnoreCase(p.peerIP)){
                        joinerIP = rm.ip;
                        joinReq = true;
                        System.out.println("Join request from: " + joinerIP);
                        p.addPeer(rm.ip,rm.id); //add the peer to our list of peers
                        sendInitPack(joinerIP, tcpPort, p.theGUI.GuiControl.allInst);
                    }
                    
                    //If the user wants to disconnect from the session then check
                    //if the ID is our own, if so set running to false so the 
                    //program disconnects, if not then remove the user from the 
                    //list of subscribees
                    else if ("disc".equals(rm.type)) {
                        System.out.println("Disconnect Request Received");
                        p.removePeer(rm.ip, rm.id);
                    }
                    
                    //If the message contains any draw instructions then handle 
                    //these and add them to the whiteboard.
                    else if("draw".equals(rm.type)){
                            System.out.println("Draw request");
                            Point np;
                            np = new Point(rm.x,rm.y);
                            p.theGUI.GuiControl.drawLine(rm.type, np, rm.col);
                    }else if("newline".equals(rm.type)){
                            System.out.println("New line request");
                             p.theGUI.GuiControl.setThisPoint();                             
                             Point np = new Point(rm.x,rm.y);
                             p.theGUI.GuiControl.drawLine(rm.type, np, rm.col);
                    }else if("text".equals(rm.type)){
                            System.out.println("Text request");
                            Point np = new Point(rm.x, rm.y);
                            p.theGUI.GuiControl.drawString
                                    (rm.payload, np, rm.font, rm.col, rm.size); 
                    }else {
                        System.err.println("Instructions received in "
                                + "unknown format!");
                    }
                }catch(Exception e){
                    System.err.println("Receiver Thread: "
                            + "Message receive error!");
                    e.printStackTrace();
                }                
            }//if we get here we have disconected and must leave the group!
            
            try {
                ds.leaveGroup(ia);
            } catch (IOException ex) {
                Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
    
    
    //This thread listens on the defined port for any incoming TCP packets, if 
    //a packet is received then the function processTCPObj() is called to unpack
    //the TCP packet and carry out the instructions that are contained within it, 
    //the socket then closes as it has completed it's job.
    Runnable tcpThreadRunnable = new Runnable(){
        @Override
        public void run(){
            try {
                System.out.println("tcpReceiver has begun");
                ServerSocket serv = new ServerSocket(tcpPort);
                while(receive){
                    tcpSocket = serv.accept();
                    peers t = new peers();
                    String ts = tcpSocket.getInetAddress().getHostAddress();
                    t.IP = ts;
                    System.out.println("Successfully connected to: "
                            + t.IP);
                    p.peerList.add(t);
                    bReader = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
                    objInput = new ObjectInputStream(tcpSocket.getInputStream());
                    try {
                        ArrayList<String> rec = (ArrayList<String>)objInput.readObject();
                        System.out.println("Received this object: "+rec);
                        processTCPObj(rec);
                        tcpSocket.close();
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException e) {
                System.err.println("ERROR: could not initTCP()");
                e.printStackTrace();
            }

        }
    };

    //This function takes a String as an argument and returns a "theMsg" object 
    //with the relevant fields set dependent on what "type" the received string 
    //is. 
     public theMsg processReceivedMSG(String rm) {
        theMsg temp = new theMsg();
        temp.set();
        System.out.println(rm);
        String[] ts = rm.split("-");
       if(ts[0].equals("join")){
            temp.type   = ts[0];
            temp.ip     = ts[1];
            temp.id     = ts[4];
        }
        else if (ts[0].equals("draw")){
            temp.type = ts[0];
            String[] sp = ts[3].split("/");
            Color nc = new Color(Integer.parseInt(sp[0]), 
                                 Integer.parseInt(sp[1]), 
                                 Integer.parseInt(sp[2]));
            temp.col = nc;
            float fx = Float.parseFloat(ts[1]);
            float fy = Float.parseFloat(ts[2]);
            temp.x = (int)fx;
            temp.y = (int)fy;
        }
        else if(ts[0].equals("newline")){
            temp.type = ts[0];
            String[] sp = ts[3].split("/");
            Color nc = new Color(Integer.parseInt(sp[0]), 
                                 Integer.parseInt(sp[1]), 
                                 Integer.parseInt(sp[2]));
            temp.col = nc;
            float fx = Float.parseFloat(ts[1]);
            float fy = Float.parseFloat(ts[2]);
            temp.x = (int)fx;
            temp.y = (int)fy;
        }
        else if(ts[0].equals("text")){
            temp.type = ts[0];
            temp.payload = ts[1];
            float fx = Float.parseFloat(ts[2]);
            float fy = Float.parseFloat(ts[3]);
            temp.x = (int)fx;
            temp.y = (int)fy;
            String[] sp = ts[4].split("/");
            temp.col = new Color(Integer.parseInt(sp[0]), 
                                 Integer.parseInt(sp[1]), 
                                 Integer.parseInt(sp[2]));
            temp.font = ts[5];
            int sz = Integer.parseInt(ts[6]);
            temp.size = sz;
        }
        else if(ts[0].equals("disc")){
            temp.type   = ts[0];
            temp.id     = ts[1];
            temp.ip     = ts[2];
        }
        return temp;
    }//end of processReceivedMSG()
 
     //This function is called with 3 arguments that are used to define what is
     //send and where it is sent. A TCP connection is attempted using the IP and
     //the port that is passed, and if successful then the ArrayList that has 
     //been passed to the function is sent over the connection. 
     public void sendInitPack(String ip, int port, ArrayList al){
        try {
            System.out.println("Attempting to send Init Package");
            Socket s = new Socket();
            System.out.println("IP: "+ip+" & port: "+port);
            s.connect(new InetSocketAddress(ip, port)); // (5 second timeout)
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
            out.writeObject(al);
            s.close();
            System.out.println("Successfully sent ArrayList over TCP to: "+ip+", on port: "+port);
            p.theGUI.GuiControl.initcommit = false;
        } catch (IOException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
     }

     //This function is called to process an ArrayList that has been recieved 
     //over a TCP connection, the List is iterated over and each String element
     //is split using the "-" regex and the type of instruction is analysed, 
     //depending on the type the function then sends the instructions to the 
     //GUI panel to be displayed to the user. 
     public void processTCPObj(ArrayList<String> al){
         for(int i = 0 ; i < al.size(); i++){
             String temp = al.get(i);
             String[] t = temp.split("-");
             if(t[0].equals("draw")){
                 String[] sp = t[3].split("/");
                 Color nc = new Color(Integer.parseInt(sp[0]), 
                                      Integer.parseInt(sp[1]), 
                                      Integer.parseInt(sp[2]));
                 float fx = Float.parseFloat(t[1]);
                 float fy = Float.parseFloat(t[2]);
                 int x = (int)fx;
                 int y = (int)fy;
                 Point np = new Point(x,y);
                 p.theGUI.GuiControl.drawLine(t[0], np, nc);
             }
             if(t[0].equals("text")){
                 String text = t[1];
                 float fx = Float.parseFloat(t[2]);
                 float fy = Float.parseFloat(t[3]);
                 int x = (int)fx;
                 int y = (int)fy;
                 Point np = new Point(x, y);
                 String[] sp = t[4].split("/");
                 Color nc = new Color(Integer.parseInt(sp[0]), 
                                      Integer.parseInt(sp[1]), 
                                      Integer.parseInt(sp[2]));
                 String fn = t[5];
                 int sz = Integer.parseInt(t[6]);
                 p.theGUI.GuiControl.drawString(text, np, fn, nc, sz);
             }
            if(t[0].equals("newline")){
                String[] sp = t[3].split("/");
                Color nc = new Color(Integer.parseInt(sp[0]), 
                                     Integer.parseInt(sp[1]), 
                                     Integer.parseInt(sp[2]));
                float fx = Float.parseFloat(t[1]);
                float fy = Float.parseFloat(t[2]);
                int x = (int)fx;
                int y = (int)fy;
                p.theGUI.GuiControl.setThisPoint();                             
                Point np = new Point(x,y);
                p.theGUI.GuiControl.drawLine(t[0], np, nc);
            }
         }
        p.theGUI.GuiControl.initcommit = false;
     }//end of processTCPObj()
}//end of Listener Class














//     public boolean checkIfConn(String ID){
//         for(peers tp: p.peerList){
//             if(tp.ID.equals(ID)){
//                 return true;
//             }
//         }
//         return false;
//     }




//
//     
//     
//     
//    public void receiveMCMsg() throws Exception{
//        buffer                  = new byte[40];
//        DatagramPacket dp       = new DatagramPacket(buffer, buffer.length);
//        try{
//            System.out.println("Ready to receive");
//            mcSocket.receive(dp);
//            System.out.println("Recevied Message");
//            String rMsg = new String(buffer);
//            theMsg rm   = processReceivedMSG(rMsg);
//            
//            //If the message type is a join message && the message does 
//            //not originate from my ID
//            System.out.println("ID1 = "+rm.id+" & ID2 = "+peerID);
//            if("join".equals(rm.type) && !rm.id.equalsIgnoreCase(peerID)){
//                joinerIP    = rm.ip;
//                joinReq    = true;
//                System.out.println("Join request from: "+ joinerIP);
//            }else{System.err.println("Ignoring message originating from me");}
//            
//            //if the message type is an accepter messager && the message
//            //does not originate from my ID
//            if("accept".equals(rm.type) && !rm.id.equalsIgnoreCase(peerID)){
//                accIP         = rm.ip;
//                isAcc    = true;
//                System.out.println("Join request Accepted by: "+accIP);
//                p.isJoined = true;
//            }else{System.err.println("Ignoring message originating from me");}
//            
//        }catch(SocketTimeoutException e){
//            System.err.println("Timed out!");
//            e.printStackTrace();
//        }
//    }//end of receiveMCMsg()
//    
