/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplewhiteboard;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import javax.swing.*;

enum mode
{
  LINE, TEXT;
}

//This class acts as a struct would in C, holding all the data pertaining
//to when a line is drawn in the whiteboard. Each point is represented by
//a separate object and held within a vector
class lineInst{
    float x, y;     //x & y coordinates of each point
    Color color;    //color of each point
    String id;      //id to differentiate between draw modes
    //default contructor to set initialise all variables
};

//This class acts as a struct would in C, holding all the data pertaining
//to when text is input into the whiteboard. Each letter is represented by 
//a separate object and held within a vector
class textInst{
  String text, font, id;    //the text string, font style, and id 
  Color color;              //color of the text
  float x, y;               //x & y coordinates of the text
  int size;                 //the font size
  //default contructor to set initialise all variables
};
/**
 * @author tdw10kcu
 */
class guiControl extends JPanel implements ActionListener, MouseListener, KeyListener{
  private GUIPanel guiPanel;
  private JComboBox drawModeComboBox, fontComboBox, sizeComboBox;
  private JButton colorButton, clearButton;
  private static String[] drawModeName = {"line", "text"};
  private static String[] fontSelect = {"Set Font", "Verdana", "Arial", 
                        "Courier", "ComicSansMS", "Calibri"};
  private static String[] fontSizes = {"Font Size", "6", "8", "10", "12", "14", "16", "18", 
                                                "20", "22", "24", "26"};
  private mode drawMode;
  private Color color;
  private Point point;
  private String fontname;
  private int fontsize;
  public ArrayList<String> drawInstructions = new ArrayList(); //this ArrayList holds all the draw instructions
  public ArrayList<String> textInstructions = new ArrayList(); //this ArrayList holds all the text instructions
  public ArrayList<String> allInst = new ArrayList();
  public ArrayList<String> allInstCopy = new ArrayList();
  public boolean newInstruction, initcommit, clear;
  public String niString;
  public guiControl(GUIPanel guiPanel)
  {
    super();
    this.drawMode = mode.LINE;
    this.guiPanel = guiPanel;
    this.drawModeComboBox = new JComboBox(drawModeName);
    this.drawModeComboBox.addActionListener(this);
    this.add(this.drawModeComboBox);
    this.colorButton = new JButton("set colour");
    this.colorButton.addActionListener(this);
    this.add(this.colorButton);
    this.fontComboBox = new JComboBox(fontSelect);
    this.fontComboBox.addActionListener(this);
    this.add(this.fontComboBox);
    this.sizeComboBox = new JComboBox(fontSizes);
    this.sizeComboBox.addActionListener(this);
    this.add(this.sizeComboBox);
    this.syncState();
    this.guiPanel.addMouseListener(this);
    this.guiPanel.addKeyListener(this);
    this.color = Color.BLACK;
    this.fontname = "Monospaced";
    this.fontsize = 20;
    niString = new String();
    newInstruction =  false;
    initcommit = true;
  }

  public void setThisPoint()  {
      this.point = null;
  }
  public void drawLine(Point newPoint)
  {
    lineInst temp = new lineInst();
    
    if (this.point == null){
      this.point = newPoint;
      temp.x = this.point.x;
      temp.y = this.point.y;
      temp.color = this.color;
      String t = Integer.toString(temp.color.getRed())+"/"
                +Integer.toString(temp.color.getGreen())+"/"
                +Integer.toString(temp.color.getBlue())+"/";
      String st = "newline"+"-"+temp.x+"-"+temp.y+"-"+t;
      allInst.add(st);
       niString = st;
    }
    else{
      this.point = this.guiPanel.drawLine(this.point, newPoint, this.color);
      temp.x = this.point.x;
      temp.y = this.point.y;
      temp.color = this.color;
      String t = Integer.toString(temp.color.getRed())+"/"+
                 Integer.toString(temp.color.getGreen())+"/"+
                 Integer.toString(temp.color.getBlue())+"/";
      String st = "draw"+"-"+temp.x+"-"+temp.y+"-"+t;
      allInst.add(st);
      niString = st;
    }
    newInstruction = true;
  }
  public void drawLine(String type, Point p, Color c){
    lineInst temp = new lineInst();
    if (this.point == null){
      this.point = p;
    }else
      this.point  = this.guiPanel.drawLine(this.point, p, c);
      temp.x = this.point.x;
      temp.y = this.point.y;
      String t = Integer.toString(c.getRed())+"/"+
                 Integer.toString(c.getGreen())+"/"+
                 Integer.toString(c.getBlue())+"/";
      if("draw".equals(type)){
          String st = "draw"+"-"+temp.x+"-"+temp.y+"-"+t;
          allInst.add(st);
      }
      else if("newline".equals(type)){
          String st = "newline"+"-"+temp.x+"-"+temp.y+"-"+t;
          allInst.add(st);
      }
  }

  public void drawString(String s)
  {
    textInst temp = new textInst();
    if (this.point != null)
    {
      this.point = this.guiPanel.drawString(s, this.point, this.fontname, this.fontsize, this.color);
      temp.text = s;
      temp.font = this.fontname;
      temp.size = this.fontsize;
      temp.color = this.color;
      temp.x = this.point.x;
      temp.y = this.point.y;
      String t = Integer.toString(temp.color.getRed())+"/"+
                 Integer.toString(temp.color.getGreen())+"/"+
                 Integer.toString(temp.color.getBlue())+"/";
      String st = "text"+"-"+temp.text+"-"+temp.x+"-"+temp.y+"-"+t+"-"+
                                                temp.font+"-"+temp.size+"-";
      allInst.add(st);
      niString = st;
    }
    newInstruction = true;
  }
  
  //overloaded function to take instructions sent over TCP to "catch" the peer
  //up with what is already on the whiteboard
  public void drawString(String s, Point p, String fn, Color c, int sz){
      this.point = this.guiPanel.drawString(s, p, fn, sz, c);
  }

  public void syncState()
  {
    switch (this.drawMode)
    {
    case LINE:
      this.drawModeComboBox.setSelectedIndex(0);
      break;
    case TEXT:
      this.drawModeComboBox.setSelectedIndex(1);
      break;
    default:
      throw new RuntimeException("unknown draw mode");
    }
  }

  private void drawModeActionPerformed(ActionEvent actionEvent)
  {
    String cmd = (String) this.drawModeComboBox.getSelectedItem();
    if (cmd.equals("line"))
    {
      this.drawMode = mode.LINE;
    }
    else if (cmd.equals("text"))
    {
      this.drawMode = mode.TEXT;
    }
    else
    {
      throw new RuntimeException(String.format("unknown command: %s", cmd));
    }
  }

  private void colorActionPerformed(ActionEvent actionEvent)
  {
    color = JColorChooser.showDialog(this.guiPanel, "choose colour", this.color);
    if (color != null)
    {
      this.color = color;
    }
  }
  
  private void fontActionPerformed(ActionEvent ae){
      String cmd = (String)this.fontComboBox.getSelectedItem();
      if(cmd.equals("Verdana")){
          this.fontname = "Verdana";
      }
      else if (cmd.equals("Arial")){
          this.fontname = "Arial";
      }
      else if (cmd.equals("Courier")){
          this.fontname = "Courier";
      }
      else if (cmd.equals("ComicSansMS")){
          this.fontname = "Comic Sans MS";
      }
      else if(cmd.equals("Calibri")){
          this.fontname = "Calibri";
      }
  }
  

  private void sizeActionPerformed(ActionEvent ae){
      String cmd = (String)this.sizeComboBox.getSelectedItem();
      int sz = Integer.parseInt(cmd);
      switch(sz){
          case 6:
              this.fontsize = 6; 
              break;
          case 8:
              this.fontsize = 8;
              break;
          case 10:
              this.fontsize = 10;
              break;
          case 12:
              this.fontsize = 12;
              break;
          case 14:
              this.fontsize = 14;
              break;
          case 16:
              this.fontsize = 16;
              break;
          case 18:
              this.fontsize = 18;
              break;
          case 20:
              this.fontsize = 20;
              break;
          case 22:
              this.fontsize = 22;
              break;
          case 24:
              this.fontsize = 24;
              break;
          case 26:
              this.fontsize = 26;
              break;                 
      }

  }

  public void actionPerformed(ActionEvent actionEvent)
  {
    if (actionEvent.getSource() == this.drawModeComboBox)
    {
      this.drawModeActionPerformed(actionEvent);
    }
    else if (actionEvent.getSource() == this.colorButton)
    {
      this.colorActionPerformed(actionEvent);
    }
    else if(actionEvent.getSource() == this.fontComboBox){
        this.fontActionPerformed(actionEvent);
    }
    else if(actionEvent.getSource() == this.sizeComboBox){
        this.sizeActionPerformed(actionEvent);
   }
  }

  @Override
  public void keyPressed(KeyEvent keyEvent)
  {
  }

  @Override
  public void keyReleased(KeyEvent keyEvent)
  {
  }

  @Override
  public void keyTyped(KeyEvent keyEvent)
  {
    switch (this.drawMode)
    {
    case TEXT:
      String s = Character.toString(keyEvent.getKeyChar());
      this.drawString(s);
      break;
    default:
      // ignore event if not in text mode
      break;
    }
  }

  @Override
  public void mouseEntered(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mouseExited(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mousePressed(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mouseReleased(MouseEvent mouseEvent)
  {
  }

  @Override
  public void mouseClicked(MouseEvent mouseEvent)
  {
    // make sure panel gets focus when clicked
    this.guiPanel.requestFocusInWindow();
    Point newPoint = mouseEvent.getPoint();
    switch (this.drawMode)
    {
    case TEXT:
      this.point = newPoint;
      break;
    case LINE:
      switch (mouseEvent.getButton())
      {
      case MouseEvent.BUTTON1:
	//System.err.println(mouseEvent);
	this.drawLine(newPoint);
	break;
      case MouseEvent.BUTTON3:
	this.point = null;
	break;
      default:
	System.err.println(String.format("got mouse button %d", mouseEvent.getButton()));
	break;
      }
      break;
    default:
      throw new RuntimeException("unknown drawing mode");
    }
  }
}

class menuAL implements ActionListener
{
  public boolean c = false, d = false;
  public void actionPerformed(ActionEvent actionEvent)
  {
    //System.err.println(String.format("menu action: %s", actionEvent.getActionCommand()));
    if("Connect".equals(actionEvent.getActionCommand()))
    {
        System.err.println("Connect pressed");
        c = true;
    }else {c = false;}
    if("Disconnect".equals(actionEvent.getActionCommand()))
    {
        System.err.println("Disconnect pressed");
        d = true; 
    }else {d = false;}
  }
}

public class GUI extends JFrame
{
  public JScrollPane scrollPane;
  public GUIPanel GuiPanel;
  public guiControl GuiControl;
  public JMenuBar menuBar;
  public menuAL MenuAL;
  public GUI(String nodename, int width, int height)
  {
    super(String.format("<4871898> whiteboard: %s", nodename));
    this.GuiPanel = new GUIPanel(width, height);
    this.scrollPane = new JScrollPane(this.GuiPanel);
    this.getContentPane().add(this.scrollPane);
    this.GuiControl = new guiControl(this.GuiPanel);
    this.getContentPane().add(this.GuiControl, BorderLayout.SOUTH);
    this.menuBar = new JMenuBar();
    this.MenuAL = new menuAL();
    JMenu networkMenu = new JMenu("Network");
    JMenuItem connectItem = new JMenuItem("Connect");
    connectItem.addActionListener(this.MenuAL);
    JMenuItem disconnectItem = new JMenuItem("Disconnect");
    disconnectItem.addActionListener(this.MenuAL);
    networkMenu.add(connectItem);
    networkMenu.add(disconnectItem);
    this.menuBar.add(networkMenu);
    this.setJMenuBar(this.menuBar);
    // this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  }

  public GUIPanel getWhiteboardPanel()
  {
    return (this.GuiPanel);
  }

}

