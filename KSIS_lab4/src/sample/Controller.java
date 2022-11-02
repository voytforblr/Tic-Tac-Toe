package sample;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Controller implements TCPConnectionListener {

    @FXML
    private TextArea TextFieldLog;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private TextField ipTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private Button connectButton;

    @FXML
    private ImageView image1;

    @FXML
    private ImageView image2;

    @FXML
    private ImageView image3;

    @FXML
    private ImageView image4;

    @FXML
    private ImageView image5;

    @FXML
    private ImageView image6;

    @FXML
    private ImageView image7;

    @FXML
    private ImageView image8;

    @FXML
    private ImageView image9;

    @FXML
    public Label label;

    @FXML
    private Line line1;

    @FXML
    private Line line2;

    @FXML
    private Line line3;

    @FXML
    private Line line4;

    @FXML
    private Line line5;

    @FXML
    private Line line6;

    @FXML
    private Line line7;

    @FXML
    private Line line8;

    ImageView[] images = {image1, image2, image3, image4, image5, image6, image7, image8, image9};
    Line[] lines = {line1, line2, line3, line4, line5, line6, line7, line8};


    public boolean[] isMine = new boolean[9];
    public boolean[] isEnemys = new boolean[9];
    int dead = 0;

    private boolean imAserver = false;
    private boolean firstWasMine;
    private boolean myTurn = false;

    @FXML
    void changeTurn() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (myTurn)
                    label.setText("Ваш ход.");
                else label.setText("Ходит противник.");
            }
        });
    }

    @FXML
    void imageClick(MouseEvent event) {
        if (myTurn) {
            if (!isMine[Integer.parseInt(((ImageView) event.getSource()).getId().substring(5)) - 1] &&
                    !isEnemys[Integer.parseInt(((ImageView) event.getSource()).getId().substring(5)) - 1]) {
                ((ImageView) event.getSource()).setImage(new Image("cross.png"));

                Date date = new Date();
                Timestamp timestamp2 = new Timestamp(date.getTime());
                connection.sendString(((ImageView) event.getSource()).getId(), timestamp2);
                String value = ((ImageView) event.getSource()).getId();
                TextFieldLog.appendText("Вы: поле" + value.substring(5) + ",время:" + timestamp2 + "\n");
                //connection.sendJson(((ImageView) event.getSource()).getId(),timestamp2);
                isMine[Integer.parseInt(((ImageView) event.getSource()).getId().substring(5)) - 1] = true;
                dead++;
                if (check(isMine)) {
                    showAlert(1, "Вы победили!");
                    restart();
                } else if (dead == 9) {
                    showAlert(1, "Ничья.");
                    restart();
                }
                myTurn = false;
                changeTurn();
            }
        }
    }

    private TCPConnection connection;


    @FXML
    void initialize() {

        line1.setVisible(false);
        line2.setVisible(false);
        line3.setVisible(false);
        line4.setVisible(false);
        line5.setVisible(false);
        line6.setVisible(false);
        line7.setVisible(false);
        line8.setVisible(false);

        toggleButton.setText("Новое\nподключение");
        portTextField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([0-9]*)?")) {
                return change;
            }
            return null;
        }));

        toggleButton.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                ipTextField.setVisible(false);
                connectButton.setText("Создать\nподключение");
            } else {
                ipTextField.setVisible(true);
                connectButton.setText("Подключиться");
            }
        }));

        connectButton.setOnAction(event -> {
            if (toggleButton.isSelected()) {
                Server me = new Server();
                connection = me.createServer(portTextField.getText(), this);
                firstWasMine = true;
                myTurn = true;
                label.setText("Ваш ход.");
                imAserver = true;
                //7label.setText(me.getAddress());
            } else {
                try {
                    connection = new TCPConnection(this, ipTextField.getText(), Integer.parseInt(portTextField.getText()));
                    myTurn = false;
                    label.setText("Ходит противник.");
                    firstWasMine = false;
                    imAserver = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {

    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String[] mas) {
        //System.out.println("value="+value);
        String value = mas[0];
        String time = mas[1];
        TextFieldLog.appendText("Противник: поле" + value.substring(5) + ",время:" + time + "\n");
        ImageView tb = (ImageView) label.getScene().lookup("#" + value);
        tb.setImage(new Image("circle.png"));
        isEnemys[Integer.parseInt(value.substring(5)) - 1] = true;
        dead++;
        if (check(isEnemys)) {
            showAlert(1, "Вы в очередной раз проиграли.\nПора уже перестать пытаться.");
            restart();
        } else if (dead == 9) {
            showAlert(1, "Ничья.");
            restart();
        }
        myTurn = true;
        changeTurn();
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        showAlert(-1, "Соединение разорвано!");
        connection.disconnect();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                restart();
                toggleButton.setSelected(false);
            }
        });
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {

    }

    private synchronized boolean check(boolean[] array) {
        for (int i = 0; i < 3; i++) {
            if (array[3 * i] && array[3 * i + 1] && array[3 * i + 2]) {
                Line lin = (Line) line1.getScene().lookup("#line" + (i + 3));
                return true;
            }
            if (array[i] && array[i + 3] && array[i + 6]) {
                Line lin = (Line) line1.getScene().lookup("#line" + (i + 6));
                return true;
            }
        }
        if (array[0] && array[4] && array[8]) {
            line1.setVisible(true);
            return true;
        }
        if (array[2] && array[4] && array[6]) {
            line2.setVisible(true);
            return true;
        }
        return false;
    }

    private static synchronized void showAlert(int type, String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert;
                if (type == -1) {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                } else {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                }
                alert.setHeaderText(null);
                alert.setContentText(text);
                alert.show();
            }
        });
    }

    private void restart() {
        TextFieldLog.setText("");
        dead = 0;
        line1.setVisible(false);
        line2.setVisible(false);
        line3.setVisible(false);
        line4.setVisible(false);
        line5.setVisible(false);
        line6.setVisible(false);
        line7.setVisible(false);
        line8.setVisible(false);

        image1.setImage(null);
        image2.setImage(null);
        image3.setImage(null);
        image4.setImage(null);
        image5.setImage(null);
        image6.setImage(null);
        image7.setImage(null);
        image8.setImage(null);
        image9.setImage(null);

        isMine = new boolean[9];
        isEnemys = new boolean[9];

        myTurn = !firstWasMine;
        firstWasMine = myTurn;
        changeTurn();
    }
}
