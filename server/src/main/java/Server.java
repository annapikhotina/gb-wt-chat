import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
    String message = String.format("[%s] %s", sender.getNickNme(), msg);
    for (ClientHandler client : clients) {
      client.sendMessage(message);
    }
  }


  public void privateMessage(ClientHandler sender, String receiver, String msg) {
    String message = String.format("[%s] to [%s]: %s", sender.getNickNme(), receiver, msg);

    for (ClientHandler client : clients) {
      if (client.getNickNme().equals(receiver)) {
        client.sendMessage(message);
        if (!sender.getNickNme().equals(receiver)) {
          sender.sendMessage(message);
        }
        return;
      }
    }
    sender.sendMessage("User with the nickname: " + receiver + " was not found");
  }

  public void addHistory(File file, String string) {
    try {
      String message = string + "\n";
      FileOutputStream out = new FileOutputStream(file, true);
      out.write(message.getBytes(StandardCharsets.UTF_8));
      out.flush();

    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void subscribe(ClientHandler client) {
    clients.add(client);
    broadcastClientList();
  }

  public void unsubscribe(ClientHandler client) {
    clients.remove(client);
    broadcastClientList();
  }

  public AuthService getAuthService() {
    return authService;
  }

  public boolean isLoginAuthorized(String login) {
    for (ClientHandler client : clients) {
      if (client.getLogin().equals(login)) {
        return true;
      }
    }
    return false;
  }

  public void broadcastClientList() {
    StringBuilder sb = new StringBuilder(Commands.CLIENT_LIST);
    for (ClientHandler client : clients) {
      sb.append(" ").append(client.getNickNme());
    }
    String message = sb.toString();
    for (ClientHandler client : clients) {
      client.sendMessage(message);
    }
  }
}

