package simplewhiteboard;
import java.util.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
/**
 * open ports in AGL:   64122, 64123, 64120, 64121
 * multi cast IP in AGL: 224.0.132.0
 * 
 * @author tdw10kcu
 *         thePeer.java: Each peer of the white board, unless it's a bot, is 
 *         controlled via this class, the "Listener" and "Broadcaster" objects
 *         are created and their associated threads started so the peer can send
 *         messages and listen for any incoming messages.
 */

//This small class imitates a struct, and holds the details pertaining to each 
//peer, that is the relevant IP and ID of each peer. 
class peers{
  String ID;
  String IP;
  peers(){
      ID = IP = "";
  }
};

public class thePeer implements Runnable {
    GUI theGUI;           
    String nodename;       
    public String peerID;
    public String peerIP;
    public ArrayList<peers> peerList = new ArrayList();
    public Listener        theListener;
    public Broadcaster     theSender;
    Thread          peerThread;
    boolean         isJoined, hasInit, shouldCont;
    
    public thePeer(String nodename){
        this.nodename = nodename;
        this.theGUI = new GUI(this.nodename, 1000, 600);
        peerThread = new Thread (peerThreadRunnable);
        isJoined = hasInit = false;
        peerID = String.valueOf(1 + (int)(Math.random() * ((100 - 1)+1)));
        theListener = new Listener(this);
        theListener.udpThread.start();
        theListener.tcpThread.start();
        theSender = new Broadcaster(this);
        hasInit = false;
        shouldCont = true;
    }

    
    public void start(){
        JFrame.setDefaultLookAndFeelDecorated(true);
        javax.swing.SwingUtilities.invokeLater(this);
        peerThread.start();
    }
    
    public void run(){
        this.theGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.theGUI.setPreferredSize(new Dimension(1000, 600));
        this.theGUI.pack();
        this.theGUI.setVisible(true);
    }
     
    //This is the main thread of the peer, it runs when a new peer is created 
    //and handles all functions that each peer has, i.e. connecting, disconnecting
    //and when a new draw instruction is detected
    Runnable peerThreadRunnable = new Runnable() {
        @Override
        public void run(){
            System.out.println("peerThreadRunnable Started");
            while(shouldCont){
                boolean c = theGUI.MenuAL.c;
                boolean d = theGUI.MenuAL.d;
                boolean ni = theGUI.GuiControl.newInstruction;
                if(c){//if the conneect button has been pressed
                    theSender.join();
                    hasInit = true;
                    theGUI.MenuAL.c = false;
                    }
                if(ni){//If there is a new drawing instruction
                    try {
                        theGUI.GuiControl.newInstruction = false;
                        String s = theGUI.GuiControl.niString;
                        theSender.sendToSubs(s);
                    } catch (Exception ex) {
                        Logger.getLogger(thePeer.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    }
                }
                if(d){//If the disconnect button is pressed
                    String dc = "disc-"+peerID+"-"+peerIP+"-";
                    theSender.sendToSubs(dc);
                    theGUI.MenuAL.d = false;
                    peerList.clear();
                }                
            }
        }
    };
   
    //This function adds a peer to the ArrayList using the details passed
    public void addPeer(String ip, String id){
            peers temp = new peers();
            temp.ID = id;
            temp.IP = ip;
            peerList.add(temp);
    }//end of addPeer()
    
    //This function iterates over the ArrayList of peers, if a match between the
    //current iteration and the passed variables is found then that iteration
    //is removed from the ArrayList.
    public void removePeer(String ip, String id){
        System.out.println("Removing Peer: "+ip);
        peerList.size();
        for(int i = 0; i < peerList.size(); i++){
            if(ip.equals(peerList.get(i).IP)){
                peerList.remove(i);
                System.err.println("Removed Peer: "+ip);
            }
        }
    }//end of removePeer()
}
