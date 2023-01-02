import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ChatPage extends JFrame implements Runnable{

    String JwtToken;
    String enteredLogin;
    JPanel panel;

    JLabel loggedUser;
    JButton sendMessageButton, getMessagesButton;
    JTextField messageField, receiverField;
    JTextArea messagesArea;

    Thread thread;

    public ChatPage(String JwtToken, String enteredLogin) {
        super("Czat");
        this.JwtToken=JwtToken;
        this.enteredLogin=enteredLogin;
        this.thread = new Thread(this);
        setBounds(200, 100, 1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(null);

        //przykladowe zapytanie http get test2
        try {
            String getUrl = "http://localhost:8080/users/test2";
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(getUrl);
            get.setHeader("Authorization", JwtToken);
            CloseableHttpResponse response = httpClient.execute(get);

            String responseString = new BasicResponseHandler().handleResponse(response);
            System.out.println(responseString);
        }catch (Exception e){
            e.printStackTrace();
        }

        messageField = new JTextField();
        messagesArea = new JTextArea();
        receiverField = new JTextField();
        loggedUser = new JLabel("Zalogowany: "+enteredLogin);

        sendMessageButton = new JButton("Wyślij");
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //pobranie danych z pól tekstowych
                try {
                    String content = messageField.getText();
                    String receiver =  receiverField.getText();
                    Message message  = new Message(enteredLogin, receiver, content);

                    //wysylanie zapytania post(wyslanie wiadomosci)
                    String postUrl = "http://localhost:8080/messages";
                    Gson gson = new Gson();
                    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                    HttpPost post = new HttpPost(postUrl);
                    StringEntity postingString = new StringEntity(gson.toJson(message));//gson.tojson() converts your pojo to json
                    post.setEntity(postingString);
                    post.setHeader("Content-type", "application/json");
                    post.setHeader("Authorization", JwtToken);
                    CloseableHttpResponse response = httpClient.execute(post);

                    System.out.println(response.getStatusLine().getStatusCode());

                    if(response.getStatusLine().getStatusCode() != 200){
                        JOptionPane.showMessageDialog(panel, "Nie udało się wysłać wiadomości!");
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        getMessagesButton = new JButton("Odbiorca");
        getMessagesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    thread.start();
            }

        });

        messagesArea.setBounds(300, 130, 800, 440);
        messageField.setBounds(300, 580, 800,80);
        receiverField.setBounds(300, 80, 200,40);

        sendMessageButton.setBounds(170,600,120,40);
        getMessagesButton.setBounds(170, 80, 120,40);

        loggedUser.setBounds(300, 20, 300,40);
        loggedUser.setFont(new Font("Serif", Font.PLAIN, 20));

        panel.add(messageField);
        panel.add(messagesArea);
        panel.add(receiverField);
        panel.add(sendMessageButton);
        panel.add(getMessagesButton);
        panel.add(loggedUser);

        setContentPane(panel);
        setVisible(true);
        System.out.println(this.JwtToken);
    }

    @Override
    public void run() {
        while (true){
            try {
                List<Message> messages = new ArrayList<Message>();
                String receiver =  receiverField.getText();
                Gson gson = new Gson();
                String getUrl = "http://localhost:8080/messages?username="+receiver;
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet get = new HttpGet(getUrl);
                get.setHeader("Authorization", JwtToken);
                CloseableHttpResponse response = httpClient.execute(get);

                String responseString = new BasicResponseHandler().handleResponse(response);
                messages = gson.fromJson(responseString,  new TypeToken<List<Message>>(){}.getType());
                messagesArea.setText("");
                for (Message m: messages) {
                    messagesArea.setText(messagesArea.getText()+m.getSender()+" <"+m.getPost_date()+">: " + m.getContent()+"\n");
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }catch (Exception er){
                er.printStackTrace();
            }
        }

    }
}
