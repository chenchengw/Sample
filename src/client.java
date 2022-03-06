import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class client {

    // establish a connection by providing host and port
    // number
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Socket socket;
    private String server;	// server
     private int port;
     private final String username;

   client(String username) {
     this.username = username;
    }
    public boolean connect() {
      try {
        socket = new Socket("localhost", 1234);
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
      System.out.println("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
      try {
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
      }
      catch (IOException e) {
        e.printStackTrace();
        return false;
      }

      //listen from server
      ServerHandler sh = new ServerHandler(ois);
      new Thread(sh).start();

        //sending username to server
      try
      {
        oos.writeObject(username);
      }
      catch (IOException eIO) {
        System.out.println("Exception doing login : " + eIO);
        disconnect();
        return false;
      }

      return true;
    }


  private void disconnect() {
    try {
      if(ois != null) ois.close();
    }
    catch(Exception e) {}
    try {
      if(oos != null) oos.close();
    }
    catch(Exception e) {}
    try{
      if(socket != null) socket.close();
    }
    catch(Exception e) {}

  }
  void sendMessage(String msg) {
    try {
      oos.writeObject(msg);
    }
    catch(IOException e) {
      System.out.println("Exception writing to server: " + e);
    }
//    catch (ClassNotFoundException e2) {
//      System.out.println("Exception writing to server: " + e2);
//    }
  }

  public static void main(String[] args) {

    String userName = "Anonymous";
    switch(args.length) {
      case 1:
        // for > javac Client username
        userName = args[0];
      case 0:
        // for > java Client
        break;
      // if number of arguments are invalid
      default:
        System.out.println("Usage is: > java Client [username]");
        return;
    }
    // object of scanner class
    Scanner sc = new Scanner(System.in);
        String line = null;
        while (true) {

          client client = new client(userName);
          if (!client.connect()) break;

          // reading from user
          line = sc.nextLine();
          if (line.equalsIgnoreCase("LOGOUT")) {
//            Message msg = new Message(Message.logout, "");
            System.out.println("Receiving logout request");
            client.disconnect();
//            client.sendMessage(msg);
            break;
          } else {
            System.out.println("Sending message: " + line + "to server");
            client.sendMessage(line);
          }

          // displaying server reply
//          System.out.println("Server replied " + in.readLine());
        }
        sc.close();
        // closing the scanner object

    }

  private static class ServerHandler implements Runnable {
     ObjectInputStream ois;

     public ServerHandler(ObjectInputStream ois) {
       this.ois =ois;
     }
    public void run() {
      while(true) {
        try {
          // read the message form the input datastream
          String msg = (String) ois.readObject();
          // print the message
          System.out.println(msg);
          System.out.print("> ");
        }
        catch(IOException e) {
          System.out.println("Server has closed the connection: " + e);
          break;
        }
        catch(ClassNotFoundException e2) {
        }
      }
    }
  }

}
