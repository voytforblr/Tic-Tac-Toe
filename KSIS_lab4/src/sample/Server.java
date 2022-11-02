package sample;

import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Server {
    private static InetAddress address = null;
    private static int port;
    private TCPConnection connection = null;

    Server(){}

    TCPConnection createServer(String portStr, TCPConnectionListener listener) {
        port = Integer.parseInt(portStr);
        System.out.println("Server running...");
        try {
            address = InetAddress.getLocalHost();
            System.out.println("Host IP: " + address.getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println("TCPConnection exception: " + e);
        }
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Active port: " + (port));

            Alert alert;
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Waiting");

            alert.setHeaderText(null);
            alert.setContentText("IP: " + address.getHostAddress() + "\nПорт: " + (port) + "\nЖдём подключения...");

            alert.show();
            try {
                connection = new TCPConnection(listener, serverSocket.accept());
                alert.setContentText("Подключение успешно установлено!");
                return connection;
            } catch (IOException e) {
                System.out.println("TCPConnection exception: " + e);
            }
        } catch (IOException e) {
            System.out.println("Port " + (port) + " is unavailable");
        }
        return connection;
    }
}
