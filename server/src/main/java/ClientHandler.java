import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import commands.Commands;

public class ClientHandler {
  private Server server;
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;
  private String nickNme;

  public ClientHandler(Server server, Socket socket) {
    try {
      this.server = server;
      this.socket = socket;
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());

      new Thread(() -> {
        try {
          //Auth loop
          while (true) {
            String message = in.readUTF();

            if (message.equals(Commands.END)) {
              out.writeUTF(Commands.END);
              throw new RuntimeException("Client is disconnecting");
            }

            if (message.startsWith(Commands.AUTH)) {
              String[] token = message.split("\\s");
              if(token.length <3) {
                continue;
              }
              String tempNickName = server.getAuthService().getNickNameByLoginAndPassword(token[1], token[2]);
              if (tempNickName != null) {
                nickNme = tempNickName;
                sendMessage(Commands.AUTH_OK + " " + nickNme);
                System.out.println("Client: " + socket.getRemoteSocketAddress() + " connected with nick: " + nickNme);
                server.subscribe(this);
                break;
              } else {
                sendMessage("Auth Faild");
              }

            }
          }

          //Chat loop
          while (true) {
            String message = in.readUTF();

            if (message.equals(Commands.END)) {
              out.writeUTF(Commands.END);
              break;
            }
            server.broadcastMessage(this, message);
          }
        }
        catch (RuntimeException e) {
          System.out.println(e.getMessage());
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        finally {
          server.unsubscribe(this);
          System.out.println("Client with nick: " + nickNme + "disconnected");
          try {
            socket.close();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }).start();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void sendMessage(String message) {
    try {
      out.writeUTF(message);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getNickNme() {
    return nickNme;
  }
}
