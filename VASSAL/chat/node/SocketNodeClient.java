package VASSAL.chat.node;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import VASSAL.chat.CgiServerStatus;
import VASSAL.chat.WelcomeMessageServer;
import VASSAL.chat.messageboard.MessageBoard;
import VASSAL.chat.peer2peer.PeerPoolInfo;
import VASSAL.command.CommandEncoder;

public class SocketNodeClient extends NodeClient implements SocketWatcher {
  private SocketHandler sender;
  protected NodeServerInfo serverInfo;

  public SocketNodeClient(CommandEncoder encoder, PeerPoolInfo info, NodeServerInfo serverInfo, MessageBoard msgSvr, WelcomeMessageServer welcomer) {
    super(encoder, info, msgSvr, welcomer);
    this.serverInfo = serverInfo;
    serverStatus = new CgiServerStatus();
  }

  public SocketNodeClient(CommandEncoder encoder, PeerPoolInfo info, final String host, final int port, MessageBoard msgSvr, WelcomeMessageServer welcomer) {
    this(encoder, info, new NodeServerInfo() {

      public String getHostName() {
        return host;
      }

      public int getPort() {
        return port;
      }

    }, msgSvr, welcomer);

  }

  public void send(String command) {
    sender.writeLine(command);
  }

  protected void initializeConnection() throws UnknownHostException, IOException {
    Socket s = new Socket(serverInfo.getHostName(), serverInfo.getPort());
    sender = new BufferedSocketHandler(s, this);
    sender.start();

  }

  protected void closeConnection() {
    SocketHandler s = sender;
    sender = null;
    s.close();
  }

  public boolean isConnected() {
    return sender != null;
  }

  public void socketClosed(SocketHandler handler) {
    if (sender != null) {
      propSupport.firePropertyChange(STATUS, null, "Lost connection to server");
      propSupport.firePropertyChange(CONNECTED, null, Boolean.FALSE);
      sender = null;
    }
  }

  public void handleMessage(String msg) {
    handleMessageFromServer(msg);
  }
}
