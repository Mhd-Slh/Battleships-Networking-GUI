import java.io.*;
import java.net.Socket;

public class Network
{
    public static Network connection;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private MessageHandler handler;

    public Network(Socket socket, MessageHandler handler) throws IOException
    {
        this.socket = socket;
        this.handler = handler;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        startListener();
    }

    private void startListener()
    {
        new Thread(() -> {
            try
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    handler.onMessage(line);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }

    public void send(String msg)
    {
        out.println(msg);
    }

    public interface MessageHandler
    {
        void onMessage(String msg);
    }
}
