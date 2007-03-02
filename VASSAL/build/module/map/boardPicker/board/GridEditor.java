/*
 * $Id$
 *
 * Copyright (c) 2005 by Rodney Kinney
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available 
 * at http://www.opensource.org.
 */

package VASSAL.build.module.map.boardPicker.board;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import VASSAL.build.GameModule;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.mapgrid.GridContainer;
import VASSAL.build.module.map.boardPicker.board.mapgrid.GridNumbering;
import VASSAL.build.module.map.boardPicker.board.mapgrid.RegularGridNumbering;
import VASSAL.tools.AdjustableSpeedScrollPane;

public abstract class GridEditor extends JDialog implements MouseListener, KeyListener {
  
  protected static final String SET = "Set Grid Shape";
  protected static final String CANCEL = "Cancel";
  protected static final String CANCEL_SET = "Cancel Set";
  protected static final String OK = "Save";
  protected static final String NUMBERING = "Numbering";
  
  protected EditableGrid grid;
  protected Board board;

  protected JPanel view;
  protected JScrollPane scroll;
  
  protected boolean setMode;
  protected Point hp1, hp2, hp3;
  
  protected JButton okButton, canSetButton, setButton, numberingButton;
  
  protected boolean saveGridVisible, saveNumberingVisible;
  protected double saveDx, saveDy;
  protected Point saveOrigin;
  
  public GridEditor(EditableGrid grid) {
    super();
    setTitle("Edit " + grid.getGridName());
    setModal(true);
    this.grid = grid;
    GridContainer container = grid.getContainer();
    if (container != null) {
      board = container.getBoard();
    }
    saveGridVisible = grid.isVisible();
    if (grid.getGridNumbering() != null) {
      saveNumberingVisible = grid.getGridNumbering().isVisible();
      // if (saveGridVisible) {
      //  ((RegularGridNumbering) grid.getGridNumbering()).setAttribute(RegularGridNumbering.VISIBLE, Boolean.FALSE);
      // }
    }
    
    saveDx = grid.getDx();
    saveDy = grid.getDy();
    saveOrigin = grid.getOrigin();
    
    initComponents();
  }
  
  protected void initComponents() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
         cancel();
      }
    });

    view = new GridPanel(board);

    view.addMouseListener(this);
    view.addKeyListener(this);
    view.setFocusable(true);

    scroll = new AdjustableSpeedScrollPane(
      view,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
      JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    scroll.setPreferredSize(new Dimension(800, 600));
    getContentPane().add(scroll, BorderLayout.CENTER);

    Box textPanel = Box.createVerticalBox();
    textPanel.add(new JLabel("Arrow Keys - Move Grid"));
    textPanel.add(new JLabel("Control-Arrow Keys - Resize Grid"));
    textPanel.add(new JLabel("Shift Key - Increase speed of other keys"));
    
    JPanel buttonPanel = new JPanel();
    
    okButton = new JButton(OK);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelSetMode();
        setVisible(false);
        GameModule.getGameModule().getDataArchive().clearScaledImageCache();
      }
    });
    buttonPanel.add(okButton);
    
    JButton canButton = new JButton(CANCEL);
    canButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });
    buttonPanel.add(canButton);
    
    setButton = new JButton(SET);
    setButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startSetMode();
      }
    });
    setButton.setRequestFocusEnabled(false);
    buttonPanel.add(setButton);

    canSetButton = new JButton(CANCEL_SET);
    canSetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelSetMode();
      }
    });
    canSetButton.setVisible(false);
    canSetButton.setRequestFocusEnabled(false);
    buttonPanel.add(canSetButton);


    numberingButton = new JButton(NUMBERING);
    numberingButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ((RegularGridNumbering) grid.getGridNumbering()).setAttribute(RegularGridNumbering.VISIBLE, new Boolean(! grid.getGridNumbering().isVisible()));
        repaint();
      }
    });
    numberingButton.setEnabled(grid.getGridNumbering() != null);
    numberingButton.setVisible(true);
    numberingButton.setRequestFocusEnabled(false);
    buttonPanel.add(numberingButton);
    
    Box controlPanel = Box.createVerticalBox();
    controlPanel.add(textPanel);
    controlPanel.add(buttonPanel);
    
    getContentPane().add(controlPanel, BorderLayout.SOUTH);

    board.fixImage();
    scroll.revalidate();
    pack();
    repaint();
  }
 
  protected void cancel() {
    cancelSetMode();
    grid.setDx(saveDx);
    grid.setDy(saveDy);
    grid.setOrigin(saveOrigin);
    setVisible(false);
  }
 
  protected void cancelSetMode() {
    canSetButton.setVisible(false);
    setButton.setVisible(true);
    view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    setMode = false;
    grid.setVisible(saveGridVisible);
    if (grid.getGridNumbering() != null && saveNumberingVisible) {
      ((RegularGridNumbering) grid.getGridNumbering()).setAttribute(RegularGridNumbering.VISIBLE, new Boolean(saveNumberingVisible));
    }
    repaint();
  }

  protected void startSetMode() {
    hp1 = null;
    hp2 = null;
    hp3 = null;
    setMode = true;
    canSetButton.setVisible(true);
    setButton.setVisible(false);
    view.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    grid.setVisible(false);
    JOptionPane.showMessageDialog(null,
        "Click on 3 adjacent points around the edge of any map grid cell");
    repaint();
  }

  public void keyPressed(KeyEvent e) {  
    if (setMode) {
      return;
    }
    
    boolean sideways = grid.isSideways();
    
    switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:
        if (e.isControlDown()) {
          if (sideways) {
             adjustDx(-1, e);
          }
          else {
             adjustDy(-1, e);
          }
        }
        else {
          if (sideways) {
            adjustX0(-1, e);
          }
          else {
            adjustY0(-1, e);
          }
        }
        break;
      case KeyEvent.VK_DOWN:
        if (e.isControlDown()) {
          if (sideways) {
            adjustDx(1, e);
         }
         else {
            adjustDy(1, e);
         }
        }
        else {
          if (sideways) {
            adjustX0(1, e);
          }
          else {
            adjustY0(1, e);
          }
        }
        break;
      case KeyEvent.VK_LEFT:
        if (e.isControlDown()) {
          if (sideways) {
            adjustDy(-1, e);
         }
         else {
            adjustDx(-1, e);
         }
        }
        else {
          if (sideways) {
            adjustY0(-1, e);
          }
          else {
            adjustX0(-1, e);
          }
        }
        break;
      case KeyEvent.VK_RIGHT:
        if (e.isControlDown()) {
          if (sideways) {
            adjustDy(1, e);
         }
         else {
            adjustDx(1, e);
         }
        }
        else {
          if (sideways) {
            adjustY0(1, e);
          }
          else {
            adjustX0(1, e);
          }
        }
        break;
      default :
        return;
    }
    

    repaint();
    e.consume();

  }

  public void keyReleased(KeyEvent e) {    
  }

  public void keyTyped(KeyEvent e) {    
  }

  public void mouseClicked(MouseEvent e) {   
    if (setMode) {
      if (hp1 == null) {
        hp1 = e.getPoint();
      }
      else if (hp2 == null) {
        hp2 = e.getPoint();
      }
      else if (hp3 == null) {
        hp3 = e.getPoint();
        calculate();
        cancelSetMode();
      }
      repaint();
    }
  }
  
  public void mouseEntered(MouseEvent e) {    
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {    
  }

  public void mouseReleased(MouseEvent e) {
    
  }
  
  protected static final int DELTA = 1;
  protected static final double DDELTA = 0.1;
  protected static final int FAST = 5;
  protected static final int ERROR_MARGIN = 5;
  
  protected void adjustX0(int direction, KeyEvent e) {
    int delta = direction * DELTA;
    if (e.isShiftDown()) {
      delta *= FAST;
    }
    Point p = grid.getOrigin();
    setNewOrigin(new Point(p.x + delta, p.y));
  }
  
  protected void adjustY0(int direction, KeyEvent e) {
    int delta = direction * DELTA;
    if (e.isShiftDown()) {
      delta *= FAST;
    }
    Point p = grid.getOrigin();
    setNewOrigin(new Point(p.x, p.y + delta));
  }
  
  protected void adjustDx(int direction, KeyEvent e) {
    double delta = direction * DDELTA;
    if (e.isShiftDown()) {
      delta *= FAST;
    }
    grid.setDx(grid.getDx() + delta);
  }
  
  protected void adjustDy(int direction, KeyEvent e) {
    double delta = direction * DDELTA;
    if (e.isShiftDown()) {
      delta *= FAST;
    }
    grid.setDy(grid.getDy() + delta);
  }
  
  protected void setNewOrigin(Point p) {
    
    int width = (int) Math.round(grid.getDx());
    int height = (int) Math.round(grid.getDy());
    
    if (p.x < (-width)) {
      p.x += width;
    }
    else if (p.x > width) {
      p.x -= width;
    }
    
    if (p.y < (-height)) {
      p.y += height;
    }
    else if (p.y > height) {
      p.y -= height;
    }
    
    grid.setOrigin(p);
  }
  
  protected boolean isHorizontal(Point p1, Point p2) {
    return Math.abs(p2.y - p1.y) <= ERROR_MARGIN;
  }

  protected boolean isVertical(Point p1, Point p2) {
    return Math.abs(p2.x - p1.x) <= ERROR_MARGIN;
  }
  
  protected boolean isPerpendicular(Point p1, Point p2) {
    return isHorizontal(p1, p2) || isVertical(p1, p2);
  }
  
  protected void reportShapeError() {
    JOptionPane.showMessageDialog(null,
        "Doesn't look like a " + grid.getGridName() + "!",
        "Grid Shape Error",
        JOptionPane.ERROR_MESSAGE);

  }
  
  /*
   * Calculate and set the Origin and size of the grid
   * based on the the 3 selected points.
   */
  public abstract void calculate();
  
  /*
   * Panel to display the Grid Editor
   */
  protected class GridPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    protected Board board;

    public GridPanel() {
      super();
      setFocusTraversalKeysEnabled(false);
    }
    
    public GridPanel(Board b) {
      this();
      setBoard(b);
    }

    public void setBoard(Board b) {
      board = b;
      board.fixImage();
      setSize(board.getSize());
      setPreferredSize(board.getSize());
    }

    public Board getBoard() {
      return board;
    }

    public void paint(Graphics g) {
      if (board != null) {
        Rectangle b = getVisibleRect();
        g.clearRect(b.x, b.y, b.width, b.height);
        board.draw(g, 0, 0, 1.0, this);
        if (setMode) {
          highlight(g, hp1);
          highlight(g, hp2);
          highlight(g, hp3);
        }
      }
      else {
        super.paint(g);
      }
    }

    protected void highlight(Graphics g, Point p) {
      final int r1 = 3;
      final int r2 = 10;
      
      if (p != null) {
        g.setColor(Color.red);
        g.fillOval(p.x-r1/2, p.y-r1/2, r1, r1);
        g.drawOval(p.x-r2/2, p.y-r2/2, r2, r2);
      }
    }
    
    public boolean isFocusable() {
      return true;
    }
  }


  /*
   * Interface to be implemented by a class that wants to be edited
   * by RegularGridEditor
   */
  public interface EditableGrid {
    public double getDx();
    public double getDy();
    public Point getOrigin();
    
    public void setDx(double dx);
    public void setDy(double dy);
    public void setOrigin(Point p);
    
    public boolean isSideways();
    public void setSideways(boolean sideways);  
    
    public GridContainer getContainer();
    public GridNumbering getGridNumbering();
    
    public boolean isVisible();
    public void setVisible(boolean b);
    
    public String getGridName();
  }


  
}
