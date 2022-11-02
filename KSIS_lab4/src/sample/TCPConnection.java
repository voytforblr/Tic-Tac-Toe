package sample;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPConnection {
    private final Socket socket;
    private final Thread thread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;
   /* private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;*/


    public TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        /*objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());*/

      /*  objectInputStream=new ObjectInputStream (new FileInputStream("file.json"));
        objectOutputStream=new ObjectOutputStream(new FileOutputStream("file.json"));*/

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!thread.isInterrupted()) {
                        eventListener.onReceiveString(TCPConnection.this, getString());
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();

                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        thread.start();
    }

    public String[] getString() throws IOException, ParseException {

        String str = in.readLine();
        System.out.println("value=" + str);
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(str);
        JSONObject jsonObject = (JSONObject) obj;
        String value = (String) jsonObject.get("image");
        String time = (String) jsonObject.get("time");
        System.out.println("value" + value);
        String[] mas = {value, time};
        return mas;

    }

    public synchronized void sendString(String value, Timestamp timestamp) {

        JSONObject obj = new JSONObject();

        obj.put("image", value);
        obj.put("time", timestamp + "");
        System.out.println((String) obj.toJSONString());
        String newValue = (String) obj.toJSONString();
        try {
            out.write(newValue + "\r\n");
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }


    public synchronized void sendJson(String value, Timestamp timestamp) {

        //System.out.println(timestamp);
        //System.out.println(value);

        JSONObject obj = new JSONObject();

        obj.put("image", value);
        obj.put("time", timestamp + "");
        System.out.println(obj.toJSONString());

        try {
            out.write(obj.toString());
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        thread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
