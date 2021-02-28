import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import commands.Commands;

public class Server {
  private final int PORT = 8189;
  private ServerSocket server;
  private Socket socket;
  private List<ClientHandler> clients;
  private AuthService authService;

  public Server() {
    clients = new CopyOnWriteArrayList<>();
    authService = new SimpleAuthService();

    try {
      server = new ServerSocket(PORT);
      System.out.println("Server started");

      while (true) {
        socket = server.accept();
        new ClientHandler(this, socket);

      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        socket.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void broadcastMessage(ClientHandler sender, String msg) {
    String message = "";
    for (ClientHandler client : clients) {
      if (msg.startsWith(Commands.PRIVATE_MESSAGE)) {
        String[] text = msg.split("\\s", 3);
        if (client.getNickNme().equals(text[1])) {
          message = String.format("[%s] %s", sender.getNickNme(), text[2]);
          client.sendMessage(message);
          sender.sendMessage(message);
        }
      } else {
        message = String.format("[%s] %s", sender.getNickNme(), msg);
        client.sendMessage(message);
      }
    }
  }

  public void subscribe(ClientHandler client) {
    clients.add(client);
  }

  public void unsubscribe(ClientHandler client) {
    clients.remove(client);
  }

  public AuthService getAuthService() {
    return authService;
  }
}

