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
package VASSAL.tools;

import VASSAL.i18n.Resources;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

// FIXME: Use java.awt.Desktop for this when we move to Java 1.6+.

/**
 * Utility class for displaying an external browser window.
 *
 * @author rkinney
 */
public class BrowserSupport {
  private static final BrowserLauncher browserLauncher;
 
  static {
    BrowserLauncher bl = null;
    try {
      bl = new BrowserLauncher();
    }
    catch (BrowserLaunchingInitializingException e) {
      ErrorDialog.bug(e);
    }
    catch (UnsupportedOperatingSystemException e) {
      ErrorDialog.bug(e);
    }

    browserLauncher = bl;
  }
 
  public static void openURL(String url) {
    if (browserLauncher != null) {
      browserLauncher.openURLinBrowser(url);
    }
    else {
      ErrorDialog.error(
        Resources.getString("BrowserSupport.unable_to_launch"),
        Resources.getString("BrowserSupport.unable_to_launch"),
        Resources.getString("BrowserSupport.open_browser", url)
      );
    }
  }
}
