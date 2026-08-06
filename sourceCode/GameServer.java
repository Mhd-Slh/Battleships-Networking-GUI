import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer
{
    public static void main(String[] args) throws Exception
    {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Game server started. Waiting for connection...");

        Socket player1 = server.accept();
        System.out.println("Player 1 connected");

        Socket player2 = server.accept();
        System.out.println("Player 2 connected");

        new Thread(() -> relay(player1, player2)).start();
        new Thread(() -> relay(player2, player1)).start();
    }

    private static void relay(Socket from, Socket to)
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(from.getInputStream()));
            PrintWriter out = new PrintWriter(to.getOutputStream(), true);

            String msg;
            while((msg = in.readLine()) != null)
            {
                // We need to have it where the info is sent to the other player.
                out.println(msg);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
