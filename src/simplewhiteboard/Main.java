package simplewhiteboard;
import java.util.Scanner;
/**
 * @author tdw10kcu
 *         Main.java: In this file the main object used to provide functionality 
 *         of the white board, "thePeer", is created
 */
public class Main {
    static boolean debug = false;
     public static void main(String[] args) throws Exception{
        if(!debug){
        System.err.println("Please enter your desired nodeName and press return!");
        Scanner nameInput = new Scanner(System.in);
        String name = "Node Name: "+nameInput.next();
        thePeer peer = new thePeer(name);
        peer.start();   
        }else{
            thePeer peer = new thePeer("DEFAULT_NODE");
            peer.start();   
        }
      
        
     }
}
