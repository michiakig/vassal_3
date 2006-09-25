/*
 * $Id$
 *
 * Copyright (c) 2004 by Rodney Kinney
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.mapgrid.GridContainer;
import VASSAL.build.module.map.boardPicker.board.mapgrid.GridNumbering;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.configure.Configurer;

/**
 * Map Grid that contains any number of {@link VASSAL.build.module.map.boardPicker.board.mapgrid.Zone}s against a background {@link MapGrid}
 */
public class ZonedGrid extends AbstractConfigurable implements GeometricGrid, GridContainer {
  private ArrayList zones = new ArrayList();
  private MapGrid background;
  private GridContainer container;

  public String[] getAttributeDescriptions() {
    return new String[0];
  }

  public Class[] getAttributeTypes() {
    return new Class[0];
  }

  public String[] getAttributeNames() {
    return new String[0];
  }

  public String getAttributeValueString(String key) {
    return null;
  }

  public void setAttribute(String key, Object value) {
  }

  public Configurer getConfigurer() {
    return null;
  }

  public void addTo(Buildable parent) {
    container = (GridContainer) parent;
    container.setGrid(this);
  }

  public GridContainer getContainer() {
    return container;
  }

  public Dimension getSize() {
    return container.getSize();
  }

  public void removeGrid(MapGrid grid) {
    if (background == grid) {
      background = null;
    }
  }

  public Board getBoard() {
    return container != null ? container.getBoard() : null;
  }

  public void setGrid(MapGrid grid) {
    background = grid;
  }

  public Class[] getAllowableConfigureComponents() {
    return background == null ? new Class[]{Zone.class, HexGrid.class, SquareGrid.class, RegionGrid.class}
        : new Class[]{Zone.class};
  }

  public static String getConfigureTypeName() {
    return "Multi-zoned Grid";
  }

  public HelpFile getHelpFile() {
    File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
    dir = new File(dir, "ReferenceManual");
    try {
      return new HelpFile(null, new File(dir, "ZonedGrid.htm"));
    }
    catch (MalformedURLException ex) {
      return null;
    }
  }

  public void removeFrom(Buildable parent) {
    ((GridContainer) parent).removeGrid(this);
  }

  public void draw(Graphics g, Rectangle bounds, Rectangle visibleRect, double scale, boolean reversed) {
    Area allZones = null;
    AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
    transform.translate(bounds.x, bounds.y);
    for (Iterator it = zones.iterator(); it.hasNext();) {
      Zone zone = (Zone) it.next();
      if (allZones == null) {
        allZones = new Area(transform.createTransformedShape(zone.getShape()));
      }
      else {
        allZones.add(new Area(transform.createTransformedShape(zone.getShape())));
      }
    }
    if (background != null) {
      Graphics2D g2d = (Graphics2D) g;
      Shape oldClip = g2d.getClip();
      if (allZones != null && oldClip != null) {
        Area clipArea = new Area(oldClip);
        clipArea.subtract(allZones);
        g2d.setClip(clipArea);
      }
      background.draw(g, bounds, visibleRect, scale, reversed);
      g2d.setClip(oldClip);
    }
    for (Iterator it = zones.iterator(); it.hasNext();) {
      Zone zone = (Zone) it.next();
      zone.draw(g, bounds, visibleRect, scale, reversed);
    }
  }

  public GridNumbering getGridNumbering() {
    return background != null ? background.getGridNumbering() : null;
  }

  public Point getLocation(String location) throws MapGrid.BadCoords {
    if (background != null) {
      return background.getLocation(location);
    }
    else {
      throw new MapGrid.BadCoords("No naming scheme defined");
    }
  }

  public boolean isVisible() {
    return true;
  }

  public String locationName(Point p) {
    String name = null;
    for (Iterator it = zones.iterator(); it.hasNext();) {
      Zone zone = (Zone) it.next();
      if (zone.contains(p)) {
        name = zone.locationName(p);
        break;
      }
    }
    if (name == null
        && background != null) {
      name = background.locationName(p);
    }
    return name;
  }

  public int range(Point p1, Point p2) {
    MapGrid grid = background;
    Zone z1 = findZone(p1);
    Zone z2 = findZone(p2);
    if (z1 == z2
      && z1 != null
      && z1.getGrid() != null) {
      grid = z1.getGrid();
    }
    return grid != null ? grid.range(p1, p2) : (int)Math.round(p1.distance(p2));
  }

  public Area getGridShape(Point center, int range) {
    Area a = null;
    Zone z = findZone(center);
    if (z != null
      && z.getGrid() instanceof GeometricGrid) {
      a = ((GeometricGrid)z.getGrid()).getGridShape(center,range);
    }
    if (a == null
      && background instanceof GeometricGrid) {
      a = ((GeometricGrid)background).getGridShape(center,range);
    }
    if (a == null) {
      a = new Area(new Ellipse2D.Double(center.x-range, center.y-range, range * 2, range * 2));
    }
    return a;
  }

  public Zone findZone(Point p) {
    Zone z = null;
    for (Iterator it = zones.iterator(); it.hasNext();) {
      Zone zone = (Zone) it.next();
      if (zone.contains(p)) {
        z = zone;
        break;
      }
    }
    return z;
  }

  public Point snapTo(Point p) {
    Point snap = null;
    Zone z = findZone(p);
    if (z != null) {
      snap = z.snapTo(p);
    }
    if (snap == null) {
      snap = background != null ? background.snapTo(p) : p;
    }
    return snap;
  }

  public boolean isLocationRestricted(Point p) {
    for (Iterator it = zones.iterator(); it.hasNext();) {
      Zone zone = (Zone) it.next();
      if (zone.contains(p)) {
        return zone.getGrid() != null && zone.getGrid().isLocationRestricted(p);
      }
    }
    return background != null && background.isLocationRestricted(p);
  }

  public void addZone(Zone z) {
    zones.add(z);
  }

  public void removeZone(Zone z) {
    zones.remove(z);
  }

  public Iterator getZones() {
    return zones.iterator();
  }

  public MapGrid getBackgroundGrid() {
    return background;
  }

  public void setBackgroundGrid(MapGrid background) {
    this.background = background;
  }
}
