/*
 *
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
package VASSAL.chat.ui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import VASSAL.chat.Player;
import VASSAL.chat.PlayerInfoWindow;
import VASSAL.chat.SimplePlayer;

/**
 * When invoked, will show profile information about another player
 */
public class ShowProfileAction extends AbstractAction {
  private static final long serialVersionUID = 1L;

  private SimplePlayer p;
  private java.awt.Frame f;

  public ShowProfileAction(SimplePlayer p, java.awt.Frame f) {
    super("Show Profile");
    this.p = p;
    this.f = f;
  }

  public void actionPerformed(ActionEvent evt) {
    new PlayerInfoWindow(f, p).setVisible(true);
  }

  public static PlayerActionFactory factory() {
    return new PlayerActionFactory() {
      public Action getAction(Player p, JTree tree) {
        return new ShowProfileAction((SimplePlayer) p, (java.awt.Frame) SwingUtilities.getAncestorOfClass(Frame.class, tree));
      }
    };
  }
}
