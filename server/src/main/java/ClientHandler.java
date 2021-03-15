import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import commands.Commands;

public class ClientHandler {
  private Server server;
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;
  private String nickNme;
  private String login;

  public ClientHandler(Server server, Socket socket) {
    try {
      this.server = server;
      this.socket = socket;
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());

      new Thread(() -> {
        try {

          //Timeout
          socket.setSoTimeout(5000);

          while (true) {
            String message = in.readUTF();

            //Disconnection
            if (message.equals(Commands.END)) {
              out.writeUTF(Commands.END);
              throw new RuntimeException("Client is disconnecting");
            }

            //Authentication
            if (message.startsWith(Commands.AUTH)) {
              String[] token = message.split("\\s");
              if (token.length < 3) {
                continue;
              }
              String tempNickName = server.getAuthService().getNickNameByLoginAndPassword(token[1], token[2]);
              login = token[1];
              if (tempNickName != null) {
                if (!server.isLoginAuthorized(login)) {
                  nickNme = tempNickName;
                  sendMessage(Commands.AUTH_OK + " " + nickNme);
                  System.out.println("Client: " + socket.getRemoteSocketAddress() + " connected with nick: " + nickNme);
                  server.subscribe(this);
                  socket.setSoTimeout(0);
                  break;
                } else {
                  sendMessage("User with this login has been authorized");
                }
              } else {
                sendMessage("Auth Faild");
              }
            }

            //Registration
            if (message.startsWith(Commands.Reg)) {
              String[] token = message.split("\\s", 4);
              if (token.length < 4) {
                continue;
              }

              boolean regSuccess = server.getAuthService().registration(token[1], token[2], token[3]);

              if(regSuccess) {
                sendMessage(Commands.RegOK);
              } else {
                sendMessage(Commands.RegNO);
              }
            }

            //Update nickname
            if(message.startsWith(Commands.UPDNICK)) {
              String[] token = message.split("\\s", 2);
              if (token.length < 2) {
                continue;
              }
              boolean nickIsUpdated = server.getAuthService().updateNickname(this.login, token[1]);
              if(nickIsUpdated) {
                sendMessage(Commands.UPDNICKOK);
              } else {
                sendMessage(Commands.UPDNICKNO);
              }
            }
          }

          //Chatting
          while (true) {
            String message = in.readUTF();

            if (message.equals(Commands.END)) {
              out.writeUTF(Commands.END);
              break;
            }

            if (message.startsWith(Commands.PRIVATE_MESSAGE)) {
              String[] token = message.split("\\s", 3);

              if (token.length < 3) {
                continue;
              }

              server.privateMessage(this, token[1], token[2]);

            } else {
              server.broadcastMessage(this, message);
            }

          }
        }
        catch (SocketTimeoutException e) {
          try {
            out.writeUTF(Commands.END);
          }
          catch (IOException ioException) {
            ioException.printStackTrace();
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
          System.out.println("Client with nick [" + nickNme + "] disconnected");
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

  public String getLogin() {
    return login;
  }
}
