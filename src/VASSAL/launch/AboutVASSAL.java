package VASSAL.launch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import VASSAL.Info;
import VASSAL.i18n.Resources;

public class AboutVASSAL extends JWindow {
  public AboutVASSAL(Component c) {
    super(SwingUtilities.getWindowAncestor(c));

    getContentPane().setBackground(Color.black);
    setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    final ImageIcon icon =
      new ImageIcon(getClass().getResource("/images/Splash.png"));
    final JLabel l1 = new JLabel(icon);
    l1.setAlignmentX(0.5F);
    add(l1);

    final JLabel l2 = new JLabel(
      Resources.getString("AboutScreen.vassal_version", Info.getVersion()));
    l2.setBackground(Color.blue);
    l2.setForeground(Color.white);
    l2.setHorizontalAlignment(JLabel.CENTER);
    l2.setAlignmentX(0.5F);
    add(l2);

    pack();

    final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(d.width / 2 - getWidth() / 2,
                d.height / 2 -getHeight() / 2);

    addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        setVisible(false);
        dispose();
      }
    });
  }
}
