import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class server {
  // driver code
  private final List<ClientHandler> clientHandlers;
  private final int port;

  public server(int port) {
    this.port = port;
    this.clientHandlers = new ArrayList<>();
  }

  public void start() {
    try {
      // server is listening on port 1234
      ServerSocket server = new ServerSocket(1234);
      server.setReuseAddress(true);
      // client request
      while (true) {

        // socket object to receive incoming client
        // requests

        Socket clientSocket = server.accept();

        // Displaying that new client is connected
        System.out.println("New client connected" + clientSocket.getInetAddress().getHostAddress());

        // create a new thread object
        ClientHandler clientHandler = new ClientHandler(clientSocket, clientHandlers);

        clientHandlers.add(clientHandler);
        // This thread will handle the client
        // separately
        new Thread(clientHandler).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    server server = new server(1234);
    server.start();
  }

  // ClientHandler class
  private static class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String userName;
    private final List<ClientHandler> cls;
    private boolean keepGoing = true;
    // Constructor
    public ClientHandler(Socket socket, List<ClientHandler> cls) {
      this.clientSocket = socket;
      this.cls = cls;

      try {
        // get the outputstream of client
        oos = new ObjectOutputStream(clientSocket.getOutputStream());

        // get the inputstream of client
        ois = new ObjectInputStream(clientSocket.getInputStream());
        // read the username for this client
        this.userName = (String) ois.readObject();
        System.out.println("new client hander initiated for username " + userName);
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

    public void run() {

        while (keepGoing) {
          try {
            String msg = (String) ois.readObject();
            System.out.println("Received msg from client" + msg);
            //eg. hello@user2
            String[] strs = msg.split("@");
            if (msg.equalsIgnoreCase("logout")) {
              keepGoing = false;
              close();
            }
            else if (strs.length <2) {
              //broadcast to all servers
              for (ClientHandler cl: cls) {
                sendMessage(msg, cl.oos);
              }
            } else {
              //private message
              for (ClientHandler cl : cls) {
                if (cl.userName.equalsIgnoreCase(strs[1])) {
                  System.out.println("Sending msg back to user " + cl.userName);
                  sendMessage(strs[0], cl.oos);
                }
              }
            }
          } catch (IOException e) {
            System.out.println(" Exception reading Streams: " + e);
            break;
          } catch (ClassNotFoundException e2) {
            break;
          }
        }

    }

    public void sendMessage(String msg, ObjectOutputStream oos) {
      try {
        oos.writeObject(msg);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void close() {
      try {
        if (oos != null) {
          oos.close();
        }
        if (ois != null) {
          ois.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
