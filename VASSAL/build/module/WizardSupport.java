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
package VASSAL.build.module;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.modules.wizard.InstructionsPanel;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardObserver;
import org.netbeans.spi.wizard.WizardPage;
import org.netbeans.spi.wizard.WizardPanelProvider;
import org.netbeans.spi.wizard.WizardPage.WizardResultProducer;

import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.Tutorial;
import VASSAL.chat.ui.ChatServerControls;
import VASSAL.command.Command;
import VASSAL.command.CommandFilter;
import VASSAL.configure.FileConfigurer;
import VASSAL.configure.PasswordConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.launch.BasicModule;
import VASSAL.tools.SplashScreen;

/**
 * Provides support for two different wizards. The WelcomeWizard is the initial screen shown to the user when loading a
 * module in play mode. The GameSetupWizard is shown whenever the user starts a new game. on- or off-line
 * 
 * @author rkinney
 */
public class WizardSupport {
  public static final String POST_INITIAL_STEPS_WIZARD = "postInitialStepsWizard"; //$NON-NLS-1$
  public static final String POST_LOAD_GAME_WIZARD = "postLoadGameWizard"; //$NON-NLS-1$
  public static final String POST_PLAY_OFFLINE_WIZARD = "postPlayOfflineWizard"; //$NON-NLS-1$
  public static final String WELCOME_WIZARD_KEY = "welcomeWizard"; //$NON-NLS-1$
  public static final String SETUP_KEY = "setup"; //$NON-NLS-1$
  public static final String ACTION_KEY = "action"; //$NON-NLS-1$
  public static final String LOAD_TUTORIAL_ACTION = "tutorial"; //$NON-NLS-1$
  public static final String PLAY_ONLINE_ACTION = "online"; //$NON-NLS-1$
  public static final String PLAY_OFFLINE_ACTION = "offline"; //$NON-NLS-1$
  public static final String LOAD_GAME_ACTION = "loadGame"; //$NON-NLS-1$
  protected Dimension logoSize = new Dimension(200,200);
  protected List setups = new ArrayList();
  protected Tutorial tutorial;

  public WizardSupport() {
  }

  /**
   * Add a {@link PredefinedSetup} to the wizard page for starting a new game offline
   * 
   * @param setup
   */
  public void addPredefinedSetup(PredefinedSetup setup) {
    setups.add(setup);
  }

  public void removePredefinedSetup(PredefinedSetup setup) {
    setups.remove(setup);
  }

  /**
   * Specify a {@link Tutorial} that the user may load from the {@link InitialWelcomeSteps}
   * 
   * @param tutorial
   */
  public void setTutorial(Tutorial tutorial) {
    this.tutorial = tutorial;
  }

  /**
   * Show the "Welcome" wizard, shown when loading a module in play mode
   * 
   */
  public void showWelcomeWizard() {
    WizardBranchController c = createWelcomeWizard();
    Wizard welcomeWizard = c.createWizard();
    Map props = new HashMap();
    props.put(WELCOME_WIZARD_KEY, welcomeWizard);
    Object result = WizardDisplayer.showWizard(welcomeWizard, new Rectangle(0, 0, logoSize.width+400, logoSize.height), null, props);
    if (result instanceof Map) {
      Map m = (Map) result;
      Object action = m.get(ACTION_KEY);
      if (PLAY_ONLINE_ACTION.equals(action)) {
        final ChatServerControls controls = ((BasicModule) GameModule.getGameModule()).getServerControls();
        GameModule.getGameModule().getFrame().setVisible(true);
        controls.toggleVisible();
        controls.getClient().setConnected(true);
      }
      else {
        GameModule.getGameModule().getGameState().setup(true);
        GameModule.getGameModule().getFrame().setVisible(true);
      }
    }
    else {
      GameModule.getGameModule().getFrame().setVisible(true);
    }
  }

  protected BranchingWizard createWelcomeWizard() {
    InitialWelcomeSteps info = createInitialWelcomeSteps();
    info.setTutorial(tutorial);
    return new BranchingWizard(info, POST_INITIAL_STEPS_WIZARD);
  }

  public WizardPanelProvider createPlayOfflinePanels() {
    ArrayList l = new ArrayList(setups);
    for (int i = 0; i < l.size(); ++i) {
      if (((PredefinedSetup) l.get(i)).isMenu()) {
        l.remove(i--);
      }
    }
    if (l.size() == 0) {
      return GameSetupPanels.newInstance();
    }
    else {
      return new PlayOfflinePanels(
          Resources.getString("WizardSupport.WizardSupport.PlayOffline"), Resources.getString("WizardSupport.WizardSupport.SelectSetup"), l); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Show a wizard that prompts the user to specify information for unfinished {@link GameSetupStep}s
   * 
   */
  public void showGameSetupWizard() {
    GameSetupPanels panels = GameSetupPanels.newInstance();
    if (panels != null) {
      WizardDisplayer.showWizard(panels.newWizard(logoSize), new Rectangle(0, 0, logoSize.width+400, logoSize.height));
    }
  }
  /**
   * This is a hack to avoid stretching the image used as a background for the wizard step outline in the dialog
   * 
   * @author rkinney
   * 
   */
  protected static class InstructionsPanelLayoutFix implements HierarchyListener, WizardObserver {
    private final Component page;
    private JLabel proxy;
    private Wizard wizard;
    private Dimension logoSize;

    protected InstructionsPanelLayoutFix(Wizard wizard, Component page, Dimension logoSize) {
      this.page = page;
      this.wizard = wizard;
      this.logoSize = logoSize;
      page.addHierarchyListener(this);
    }

    public void hierarchyChanged(HierarchyEvent e) {
      page.removeHierarchyListener(this);
      Container parent = e.getChangedParent().getParent();
      if (parent.getComponentCount() > 0 && parent.getComponent(0) instanceof InstructionsPanel) {
        final InstructionsPanel panel = (InstructionsPanel) parent.getComponent(0);
        panel.setSize(panel.getPreferredSize());
        parent.remove(panel);
        proxy = new JLabel(new Icon() {
          public int getIconHeight() {
            return panel.getPreferredSize().height;
          }

          public int getIconWidth() {
            return panel.getPreferredSize().width;
          }

          public void paintIcon(Component c, Graphics g, int x, int y) {
            BufferedImage img = (BufferedImage) UIManager.get("wizard.sidebar.image"); //$NON-NLS-1$
            if (img == null) {
              try {
                img = ImageIO.read(InstructionsPanel.class.getResourceAsStream("defaultWizard.png")); //$NON-NLS-1$
              }
              catch (IOException e) {
                e.printStackTrace();
                img = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
              }
            }
            panel.setSize(img.getWidth(), img.getHeight());
            panel.paintComponent(g);
          }
        });
        proxy.setPreferredSize(new Dimension(Math.min(logoSize.width, proxy.getPreferredSize().width), proxy.getPreferredSize().height));
        parent.add(proxy, BorderLayout.WEST);
        wizard.addWizardObserver(this);
      }
    }

    public void navigabilityChanged(Wizard wizard) {
      proxy.repaint();
    }

    public void selectionChanged(Wizard wizard) {
      proxy.repaint();
    }

    public void stepsChanged(Wizard wizard) {
      proxy.repaint();
    }
  }

  public InitialWelcomeSteps createInitialWelcomeSteps() {
    Object realName = GameModule.getGameModule().getPrefs().getValue(GameModule.REAL_NAME);
    if (realName == null || realName.equals(Resources.getString("Prefs.newbie"))) {
      return new InitialWelcomeSteps(
          new String[]{InitialWelcomeSteps.NAME_STEP, ACTION_KEY}, new String[]{Resources.getString("WizardSupport.WizardSupport.EnterName"), Resources.getString("WizardSupport.WizardSupport.SelectPlayMode")}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    else {
      return new InitialWelcomeSteps(new String[]{ACTION_KEY}, new String[]{Resources.getString("WizardSupport.SelectPlayMode")}); //$NON-NLS-1$
    }
  }
  /**
   * Wizard pages for the welcome wizard (initial module load). Prompts for username/password if not yet specified, and
   * prompts to load a saved game or start a new one
   * 
   * @author rkinney
   * 
   */
  public class InitialWelcomeSteps extends WizardPanelProvider {
    public static final String NAME_STEP = "name"; //$NON-NLS-1$
    private JComponent nameControls;
    private JComponent actionControls;
    private Tutorial tutorial;

    public InitialWelcomeSteps(String[] steps, String[] stepDescriptions) {
      super(Resources.getString("WizardSupport.Welcome"), steps, stepDescriptions); //$NON-NLS-1$
    }

    protected JComponent createPanel(WizardController controller, String id, Map settings) {
      JComponent c = null;
      if (NAME_STEP.equals(id)) {
        c = getNameControls(controller, settings);
      }
      else if (ACTION_KEY.equals(id)) {
        c = getActionControls(controller, settings);
      }
      else {
        throw new IllegalArgumentException("Illegal step: " + id); //$NON-NLS-1$
      }
      new InstructionsPanelLayoutFix((Wizard) settings.get(WELCOME_WIZARD_KEY), c, logoSize);
      SplashScreen.disposeAll();
      return c;
    }

    private JComponent getActionControls(WizardController controller, final Map settings) {
      if (actionControls == null) {
        Box box = Box.createVerticalBox();
        ButtonGroup group = new ButtonGroup();
        final JRadioButton b = createPlayOfflineButton(controller, settings);
        b.doClick();
        addButton(b, group, box);
        settings.put(ACTION_KEY, PLAY_OFFLINE_ACTION);
        addButton(createPlayOnlineButton(controller, settings), group, box);
        addButton(createLoadSavedGameButton(controller, settings), group, box);
        if (tutorial != null) {
          addButton(createTutorialButton(controller, settings), group, box);
        }
        actionControls = box;
      }
      return actionControls;
    }

    private JRadioButton createTutorialButton(final WizardController controller, final Map settings) {
      JRadioButton b = new JRadioButton(Resources.getString("WizardSupport.LoadTutorial")); //$NON-NLS-1$
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          controller.setProblem(Resources.getString("WizardSupport.LoadingTutorial")); //$NON-NLS-1$
          try {
            new SavedGameLoader(controller, settings, tutorial.getTutorialContents(), POST_INITIAL_STEPS_WIZARD).start();
          }
          catch (IOException e1) {
            e1.printStackTrace();
            controller.setProblem(Resources.getString("WizardSupport.ErrorLoadingTutorial")); //$NON-NLS-1$
          }
        }
      });
      return b;
    }

    private JRadioButton createLoadSavedGameButton(final WizardController controller, final Map settings) {
      JRadioButton b = new JRadioButton(Resources.getString("WizardSupport.LoadSavedGame")); //$NON-NLS-1$
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          settings.put(WizardSupport.ACTION_KEY, LOAD_GAME_ACTION);
          Wizard wiz = new BranchingWizard(new LoadSavedGamePanels(), POST_LOAD_GAME_WIZARD).createWizard();
          settings.put(POST_INITIAL_STEPS_WIZARD, wiz);
          controller.setForwardNavigationMode(WizardController.MODE_CAN_CONTINUE);
          controller.setProblem(null);
        }
      });
      return b;
    }

    private JRadioButton createPlayOnlineButton(final WizardController controller, final Map settings) {
      JRadioButton b = new JRadioButton(Resources.getString("WizardSupport.PlayOnline")); //$NON-NLS-1$
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          settings.put(WizardSupport.ACTION_KEY, PLAY_ONLINE_ACTION);
          controller.setForwardNavigationMode(WizardController.MODE_CAN_FINISH);
          controller.setProblem(null);
        }
      });
      return b;
    }

    private JRadioButton createPlayOfflineButton(final WizardController controller, final Map settings) {
      JRadioButton b = new JRadioButton(Resources.getString("WizardSupport.PlayOffline")); //$NON-NLS-1$
      b.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GameModule.getGameModule().getGameState().setup(false);
          settings.put(WizardSupport.ACTION_KEY, PLAY_OFFLINE_ACTION);
          final WizardPanelProvider panels = createPlayOfflinePanels();
          Wizard wiz = new BranchingWizard(panels, POST_PLAY_OFFLINE_WIZARD).createWizard();
          settings.put(POST_INITIAL_STEPS_WIZARD, wiz);
          controller.setForwardNavigationMode(panels == null ? WizardController.MODE_CAN_FINISH : WizardController.MODE_CAN_CONTINUE);
        }
      });
      return b;
    }

    private void addButton(JRadioButton button, ButtonGroup group, Box box) {
      box.add(button);
      group.add(button);
    }

    private JComponent getNameControls(final WizardController controller, final Map settings) {
      if (nameControls == null) {
        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalGlue());
        controller.setProblem(Resources.getString("WizardSupport.EnterNameAndPassword")); //$NON-NLS-1$
        final StringConfigurer nameConfig = new StringConfigurer(null, Resources.getString("WizardSupport.RealName")); //$NON-NLS-1$
        final StringConfigurer pwd = new PasswordConfigurer(null, Resources.getString("WizardSupport.Password")); //$NON-NLS-1$
        final StringConfigurer pwd2 = new PasswordConfigurer(null, Resources.getString("WizardSupport.ConfirmPassword")); //$NON-NLS-1$
        PropertyChangeListener pl = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            settings.put(GameModule.REAL_NAME, nameConfig.getValue());
            settings.put(GameModule.SECRET_NAME, pwd.getValue());
            if (nameConfig.getValue() == null || "".equals(nameConfig.getValue())) { //$NON-NLS-1$
              controller.setProblem(Resources.getString("WizardSupport.EnterYourName")); //$NON-NLS-1$
            }
            else if (pwd.getValue() == null || "".equals(pwd.getValue())) { //$NON-NLS-1$
              controller.setProblem(Resources.getString("WizardSupport.EnterYourPassword")); //$NON-NLS-1$
            }
            else if (!pwd.getValue().equals(pwd2.getValue())) {
              controller.setProblem(Resources.getString("WizardSupport.PasswordsDontMatch")); //$NON-NLS-1$
            }
            else {
              GameModule.getGameModule().getPrefs().getOption(GameModule.REAL_NAME).setValue(nameConfig.getValueString());
              GameModule.getGameModule().getPrefs().getOption(GameModule.SECRET_NAME).setValue(pwd.getValueString());
              try {
                GameModule.getGameModule().getPrefs().write();
                controller.setProblem(null);
              }
              catch (IOException e) {
                String msg = e.getMessage();
                if (msg == null) {
                  msg = Resources.getString("Prefs.unable_to_save");
                }
                controller.setProblem(msg);
              }
            }
          }
        };
        nameConfig.addPropertyChangeListener(pl);
        pwd.addPropertyChangeListener(pl);
        pwd2.addPropertyChangeListener(pl);
        box.add(nameConfig.getControls());
        box.add(pwd.getControls());
        box.add(pwd2.getControls());
        JLabel l = new JLabel(Resources.getString("WizardSupport.NameAndPasswordDetails"));
        l.setAlignmentX(Box.CENTER_ALIGNMENT);
        box.add(l);
        box.add(Box.createVerticalGlue());
        nameControls = box;
      }
      return nameControls;
    }

    public void setTutorial(Tutorial tutorial) {
      this.tutorial = tutorial;
    }
  }
  /**
   * Wizard pages for starting a new game offline
   * 
   * @author rkinney
   * 
   */
  public static class PlayOfflinePanels extends WizardPanelProvider {
    private List setups;
    private String description;

    protected PlayOfflinePanels(String title, String singleDescription, List setups) {
      super(title, SETUP_KEY, singleDescription);
      this.setups = setups;
      this.description = singleDescription;
    }

    protected JComponent createPanel(final WizardController controller, String id, final Map settings) {
      final JComboBox setupSelection = new JComboBox(setups.toArray());
      ((DefaultComboBoxModel) setupSelection.getModel()).insertElementAt(description, 0);
      setupSelection.setSelectedIndex(0);
      setupSelection.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (setupSelection.getSelectedItem() instanceof PredefinedSetup) {
            PredefinedSetup setup = (PredefinedSetup) setupSelection.getSelectedItem();
            loadSetup(setup, controller, settings);
          }
          else {
            controller.setProblem(description);
          }
        }
      });
      setupSelection.setMaximumSize(new Dimension(setupSelection.getMaximumSize().width, setupSelection.getPreferredSize().height));
      setupSelection.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if (value instanceof PredefinedSetup) {
            PredefinedSetup pds = (PredefinedSetup) value;
            c.setText((pds).getConfigureName());
            if (pds.isMenu()) {
              c.setSize(0, 0);
            }
          }
          else {
            c.setText(value == null ? "" : value.toString());
          }
          return c;
        }
      });
      Box box = Box.createVerticalBox();
      box.add(Box.createVerticalGlue());
      box.add(setupSelection);
      box.add(Box.createVerticalGlue());
      controller.setProblem(description);
      return box;
    }

    protected void loadSetup(PredefinedSetup setup, final WizardController controller, final Map settings) {
      try {
        new SavedGameLoader(controller, settings, setup.getSavedGameContents(), POST_PLAY_OFFLINE_WIZARD).start();
      }
      catch (IOException e1) {
        controller.setProblem(Resources.getString("WizardSupport.UnableToLoad"));
      }
    }
  }
  /** Branches the wizard by forwarding to the Wizard stored in the wizard settings under a specified key */
  public static class BranchingWizard extends WizardBranchController {
    private String wizardKey;

    public BranchingWizard(WizardPanelProvider base, String key) {
      super(base);
      this.wizardKey = key;
    }

    protected WizardPanelProvider getPanelProviderForStep(String step, Map settings) {
      WizardPanelProvider w = (WizardPanelProvider) settings.get(wizardKey);
      return w;
    }

    protected Wizard getWizardForStep(String step, Map settings) {
      Wizard w = null;
      Object next = settings.get(wizardKey);
      if (next instanceof Wizard) {
        w = (Wizard) next;
      }
      else {
        w = super.getWizardForStep(step, settings);
      }
      return w;
    }
  }
  /**
   * Loads a saved game in the background. Add a branch to the wizard if the loaded game has unfinished
   * {@link GameSetupStep}s. Otherwise, enable the finish button
   * 
   * @author rkinney
   * 
   */
  public static class SavedGameLoader extends Thread {
    private WizardController controller;
    private Map settings;
    private InputStream in;
    private String wizardKey;

    public SavedGameLoader(WizardController controller, Map settings, InputStream in, String wizardKey) {
      super();
      this.controller = controller;
      this.settings = settings;
      this.in = in;
      this.wizardKey = wizardKey;
    }

    public void run() {
      try {
        controller.setProblem(Resources.getString("WizardSupport.LoadingGame")); //$NON-NLS-1$
        Command setupCommand = GameModule.getGameModule().getGameState().decodeSavedGame(in);
        if (setupCommand == null) {
          throw new IOException(Resources.getString("WizardSupport.InvalidSavefile")); //$NON-NLS-1$
        }
        setupCommand = new CommandFilter() {
          protected boolean accept(Command c) {
            return !(c instanceof GameState.SetupCommand) || !((GameState.SetupCommand) c).isGameStarting();
          }
        }.apply(setupCommand);
        setupCommand.execute();
        controller.setProblem(null);
        final GameSetupPanels panels = GameSetupPanels.newInstance();
        settings.put(wizardKey, panels);
        controller.setForwardNavigationMode(panels == null ? WizardController.MODE_CAN_FINISH : WizardController.MODE_CAN_CONTINUE);
      }
      catch (IOException e) {
        controller.setProblem(Resources.getString("WizardSupport.UnableToLoad")); //$NON-NLS-1$
      }
    }
  }
  /**
   * Wizard pages for loading a saved game
   * 
   * @author rkinney
   * 
   */
  public static class LoadSavedGamePanels extends WizardPanelProvider {
    private FileConfigurer fileConfig;

    public LoadSavedGamePanels() {
      super(Resources.getString("WizardSupport.LoadGame"), LOAD_GAME_ACTION, Resources.getString("WizardSupport.LoadSavedGame")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected JComponent createPanel(final WizardController controller, String id, final Map settings) {
      fileConfig = new FileConfigurer(null, Resources.getString("WizardSupport.SavedGame")); //$NON-NLS-1$
      fileConfig.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          final File f = (File) evt.getNewValue();
          if (f == null || !f.exists()) {
            controller.setProblem(Resources.getString("WizardSupport.NoSuchFile")); //$NON-NLS-1$
          }
          else if (f.isDirectory()) {
            controller.setProblem(""); //$NON-NLS-1$
          }
          else {
            try {
              new SavedGameLoader(controller, settings, new FileInputStream(f), POST_LOAD_GAME_WIZARD).start();
            }
            catch (IOException e) {
              controller.setProblem(Resources.getString("WizardSupport.UnableToLoad")); //$NON-NLS-1$
            }
          }
        }
      });
      controller.setProblem(Resources.getString("WizardSupport.SelectSavedGame")); //$NON-NLS-1$
      return (JComponent) fileConfig.getControls();
    }
  }
  /**
   * Wizard page for an unfinished {@link GameSetupStep}
   * 
   * @author rkinney
   * 
   */
  public static class SetupStepPage extends WizardPage {
    public SetupStepPage(GameSetupStep step) {
      super(step.getStepTitle());
      add(step.getControls());
      putWizardData(step, step);
    }
  }

  public void setBackgroundImage(Image image) {
    if (image != null) {
      ImageIcon icon = new ImageIcon(image);
      logoSize = new Dimension(icon.getIconWidth(), icon.getIconHeight());
      BufferedImage buffIm = new BufferedImage(logoSize.width, logoSize.height, BufferedImage.TYPE_4BYTE_ABGR);
      Graphics2D g = (Graphics2D) buffIm.getGraphics();
      g.setColor(Color.white);
      g.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
      icon.paintIcon(null, g, 0, 0);
      UIManager.put("wizard.sidebar.image", buffIm); //$NON-NLS-1$
    }
  }
  /**
   * Wizard pages for starting a new game. One page will be added for each unfinished {@link GameSetupStep}
   * 
   * @see GameState#getUnfinishedSetupSteps()
   * @author rkinney
   */
  public static class GameSetupPanels extends WizardPanelProvider implements WizardResultProducer {
    private WizardPage[] pages;
    private List setupSteps;

    private GameSetupPanels(String[] steps, String[] descriptions, WizardPage[] pages, List setupSteps) {
      super(steps, descriptions);
      this.pages = pages;
      this.setupSteps = setupSteps;
    }

    public static GameSetupPanels newInstance() {
      GameSetupPanels panels = null;
      List pages = new ArrayList();
      List setupSteps = new ArrayList();
      for (Iterator it = GameModule.getGameModule().getGameState().getUnfinishedSetupSteps(); it.hasNext();) {
        GameSetupStep step = (GameSetupStep) it.next();
        setupSteps.add(step);
        SetupStepPage page = new SetupStepPage(step);
        pages.add(page);
      }
      if (pages.size() > 0) {
        WizardPage[] wizardPages = (WizardPage[]) pages.toArray(new WizardPage[pages.size()]);
        String[] steps = new String[setupSteps.size()];
        String[] desc = new String[setupSteps.size()];
        for (int i = 0, n = setupSteps.size(); i < n; i++) {
          steps[i] = String.valueOf(i);
          desc[i] = ((GameSetupStep) setupSteps.get(i)).getStepTitle();
        }
        panels = new GameSetupPanels(steps, desc, wizardPages, setupSteps);
      }
      return panels;
    }

    protected JComponent createPanel(WizardController controller, String id, Map settings) {
      int index = indexOfStep(id);
      controller.setForwardNavigationMode(index == pages.length - 1 ? WizardController.MODE_CAN_FINISH : WizardController.MODE_CAN_CONTINUE);
      return pages[index];
    }

    public boolean cancel(Map settings) {
      GameModule.getGameModule().getGameState().setup(false);
      return true;
    }

    public Object finish(Map wizardData) throws WizardException {
      for (Iterator it = setupSteps.iterator(); it.hasNext();) {
        GameSetupStep step = (GameSetupStep) it.next();
        step.finish();
      }
      return wizardData;
    }

    public Wizard newWizard(Dimension logoSize) {
      Wizard w = createWizard();
      new InstructionsPanelLayoutFix(w, pages[0], logoSize);
      return w;
    }
  }
}
