import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient
{
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public GameClient(String ip) throws Exception
    {
        socket = new Socket(ip, 5000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void send(String msg)
    {
        out.println(msg);
    }

    public void listen(Runnable onMessage)
    {
        new Thread(() ->
        {
            try
            {
                String msg;
                while((msg = in.readLine()) != null)
                {
                    // NOTE(griffin): Game client needs to take this message and interpret it into the game logic
                    System.out.println("Received: " + msg);
                }
            }
            catch(Exception e)
            {
                System.out.println("Error: " + e.getMessage());
            }
        }).start();
    }
}