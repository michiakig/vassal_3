package VASSAL.chat.node;

import java.io.IOException;
import java.net.Socket;

/**
 * Watches for thread lock on a server.  Kills the runtime if unable to establish new connection
 * Copyright (c) 2003 by Rodney Kinney.  All rights reserved.
 * Date: Jul 20, 2003
 */
public class LockWatcher extends Thread {
  private long delay;
  private long timeout;
  private int port;

  /**
   *
   * @param delay Time in milliseconds between connection attempts
   * @param timeout Wait time in milliseconds to establish a new connection before terminating
   */
  public LockWatcher(long delay, long timeout, int port) {
    this.delay = delay;
    this.timeout = timeout;
    this.port = port;
  }

  public void run() {
    while (true) {
      try {
        sleep(delay);
        pingServer();
      }
      catch (InterruptedException e) {
        break;
      }
    }
  }

  private void pingServer() {
    try {
      final Socket s = new Socket("localhost", port);
      final Thread t = new Thread(new Timeout());
      SocketWatcher watcher = new SocketWatcher() {
        public void handleMessage(String msg) {
          t.interrupt();
        }

        public void socketClosed(SocketHandler handler) {
          System.err.println("Server closed socket");
        }
      };
      SocketHandler sender = new BufferedSocketHandler(s, watcher);
      sender.start();
      t.start();
      sender.writeLine(Protocol.encodeRegisterCommand("pinger", "ping/Main", ""));
      try {
        t.join();
      }
      catch (InterruptedException e) {
      }
      sender.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  private class Timeout implements Runnable {
    public void run() {
      try {
        sleep(timeout);
        System.err.println("No response from server in "+(timeout/1000.0)+" seconds.  Terminating process");
        System.exit(0);
      }
      catch (InterruptedException e) {
        System.err.println("Ping");
        // Interrupt means response received from server
      }
    }
  }
}
