/*
 * Copyright (c) 2000-2007 by Rodney Kinney
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
package VASSAL.launch;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import VASSAL.build.GameModule;
import VASSAL.build.module.Documentation;
import VASSAL.configure.ShowHelpAction;
import VASSAL.i18n.Resources;
import VASSAL.tools.DataArchive;

public class FirstTimeUserPanel {
  private JPanel panel;
  private File tourModule;
  private File tourLogFile;
  private ConsoleWindow console;
  
  public FirstTimeUserPanel(ConsoleWindow console) {
    this.console = console;
    tourModule = new File(Documentation.getDocumentationBaseDir(), "tour.mod");  //$NON-NLS-1$
    tourLogFile = new File(Documentation.getDocumentationBaseDir(), "tour.log");  //$NON-NLS-1$
    initComponents();
  }

  protected void initComponents() {
    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JLabel l = new JLabel();
    l.setFont(new Font("SansSerif", 1, 40));  //$NON-NLS-1$
    l.setText(Resources.getString("Main.welcome"));  //$NON-NLS-1$
    l.setForeground(Color.black);
    l.setAlignmentX(0.5F);
    panel.add(l);
    Box b = Box.createHorizontalBox();
    JButton tour = new JButton(Resources.getString("Main.tour"));  //$NON-NLS-1$
    JButton jump = new JButton(Resources.getString("Main.jump_right_in"));  //$NON-NLS-1$
    JButton help = new JButton(Resources.getString(Resources.HELP));
    b.add(tour);
    b.add(jump);
    b.add(help);
    JPanel p = new JPanel();
    p.add(b);
    panel.add(p);
    tour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        try {
          console.getFrame().setVisible(false);
          GameModule.init(new BasicModule(new DataArchive(tourModule.getPath())));
          GameModule.getGameModule().getFrame().setVisible(true);
          GameModule.getGameModule().getGameState().loadGameInBackground(tourLogFile);
          console.getFrame().dispose();
        }
        catch (Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog
              (null,
               e.getMessage(),
               Resources.getString("Main.open_error"),  //$NON-NLS-1$
               JOptionPane.ERROR_MESSAGE);
          console.setControls(new ConsoleControls(console).getControls());
          console.getFrame().setVisible(true);
        }
      }
    });
    jump.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        console.setControls(new ConsoleControls(console).getControls());
        console.getFrame().setVisible(true);
      }
    });
    try {
      help.addActionListener(new ShowHelpAction(new URL("http://www.vassalengine.org/wiki/doku.php?id=getting_started:getting_started"), null));
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }



  public JComponent getControls() {
    return panel;
  }
}
