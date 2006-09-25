/*
 * $Id$
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
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
package VASSAL.configure;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.documentation.HelpWindow;

/**
 * Action that displays a {@link HelpWindow}
 */
public class ShowHelpAction extends AbstractAction {
  private HelpWindow helpWindow;
  private URL contents;

  public ShowHelpAction(HelpWindow helpWindow, HelpFile contents, URL iconURL) {
    this(helpWindow, contents == null ? null : contents.getContents(), iconURL);
    setEnabled(contents != null);
  }

  public ShowHelpAction(HelpWindow helpWindow, URL contents, URL iconURL) {
    this.helpWindow = helpWindow;
    this.contents = contents;
    if (iconURL != null) {
      putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
    }
    else {
      putValue(Action.NAME, "Help");
    }
  }
  public void actionPerformed(ActionEvent e) {
    if (contents != null) {
      helpWindow.update(contents);
    }
    helpWindow.show();
  }
}
