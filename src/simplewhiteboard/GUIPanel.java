/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplewhiteboard;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

/**
 *
 * @author Shinobi
 */
public class GUIPanel extends JPanel{
  private BufferedImage image;
  private int width, height;
  public GUIPanel(int width, int height)
  {
    this.width = width;
    this.height = height;
    this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = this.image.getGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, image.getWidth(), image.getHeight());
    this.setFocusable(true);
  }

  public Dimension getPreferredSize()
  {
    return (new Dimension(this.image.getWidth(), this.image.getHeight()));
  }

  /**
   * Draw a line from {@code p1} to {@code p2} and  return {@code p2}.
   *
   * Returning  {@code p2} supports the client in drawing polylines.
   *
   * @param p1 point to draw from
   * @param p2 point to draw to
   * @param color color to use for drawing
   * @return {@code p2}
   */
  public Point drawLine(Point p1, Point p2, Color color)
  {
    Graphics g = this.image.getGraphics();
    if (color != null)
    {
      g.setColor(color);
    }
    g.drawLine(p1.x, p1.y, p2.x, p2.y);
    this.repaint();
    return (new Point(p2));
  }


  /**
   * Draw a string at a given point.
   *
   * The method returns the point where the next string should be
   * drawn to seamlessly continue the current string.
   *
   * @param s the string
   * @param point the position where to draw the string
   * @param fontname the font's name, should preferably be a logical name
   * @param fontsize the font size
   * @param color the color
   * @return the point where outputting string text should continue
   */
  public Point drawString(String s, Point point, String fontname, int fontsize, Color color)
  {
    Graphics g = this.image.getGraphics();
    g.setColor(color);
    g.setFont(new Font(fontname, Font.PLAIN, fontsize));
    g.drawString(s, point.x, point.y);
    FontMetrics f = g.getFontMetrics();
    Point newPoint = new Point(point);
    newPoint.x += f.stringWidth(s);
    this.repaint();
    return (newPoint);
  }

  public void paintComponent(Graphics graphics)
  {
    Graphics g = graphics.create();
    g.drawImage(this.image, 0, 0, null);
  }
}