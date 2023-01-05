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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ChatPage extends JFrame implements Runnable{

    String JwtToken;
    String enteredLogin;

    String receiver;
    JPanel panel;
    JLabel loggedUserLabel, friendsLabel, receiverLabel;
    JButton sendMessageButton, getMessagesButton;
    JTextField messageField, receiverField;
    JTextArea messagesArea;

    JScrollPane messagesScrollPane;
    JList<String> friendsList;
    DefaultListModel<String> defaultListModel;
    Thread thread;

    public ChatPage(String JwtToken, String enteredLogin) {
        super("Czat");
        this.JwtToken=JwtToken;
        this.enteredLogin=enteredLogin;
        this.thread = new Thread(this);
        setBounds(200, 100, 1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(null);

        messageField = new JTextField();
        messagesArea = new JTextArea();
        messagesScrollPane = new JScrollPane(messagesArea);
        receiverField = new JTextField();
        loggedUserLabel = new JLabel("Zalogowany: "+enteredLogin);
        friendsLabel = new JLabel("Znajomi");
        receiverLabel = new JLabel("Konwersacja z ");

        friendsList = new JList<>();
        defaultListModel = new DefaultListModel<>();
        friendsList.setModel(defaultListModel);

        sendMessageButton = new JButton("Wyślij");
        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //pobranie danych z pól tekstowych
                try {
                    String content = messageField.getText();
                    if(receiver==null) JOptionPane.showMessageDialog(panel, "Brak odbiorcy!");
                    else if(content==null || content.isEmpty()) JOptionPane.showMessageDialog(panel, "Wiadomość nie może być pusta!");
                    else {
                        Message message = new Message(enteredLogin, receiver, content);

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
                        //System.out.println(response.getStatusLine().getStatusCode());

                        if (response.getStatusLine().getStatusCode() != 200) {
                            JOptionPane.showMessageDialog(panel, "Nie udało się wysłać wiadomości!");
                        } else {
                            messageField.setText("");
                        }
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        getMessagesButton = new JButton("Nowy odbiorca");
        getMessagesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    receiver = receiverField.getText();
                   // if(thread.getState().toString()=="NEW") thread.start();
            }

        });

        friendsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                receiver = friendsList.getSelectedValue();
                //if(thread.getState().toString()=="NEW") thread.start();
            }
        });


        messagesScrollPane.setBounds(300, 130, 800, 440);
        messageField.setBounds(300, 580, 800,80);
        receiverField.setBounds(300, 80, 200,40);

        sendMessageButton.setBounds(1105,580,70,80);
        getMessagesButton.setBounds(505, 80, 120,40);

        loggedUserLabel.setBounds(300, 20, 300,40);
        loggedUserLabel.setFont(new Font("Serif", Font.PLAIN, 25));
        friendsLabel.setBounds(20,80,250, 40);
        receiverLabel.setBounds(750, 80, 300,40);
        friendsLabel.setFont(new Font("Serif", Font.PLAIN, 25));
        receiverLabel.setFont(new Font("Serif", Font.PLAIN, 25));

        friendsList.setBounds(20,130,250, 530);

        panel.add(messageField);
        panel.add(messagesScrollPane);
        panel.add(receiverField);
        panel.add(sendMessageButton);
        panel.add(getMessagesButton);
        panel.add(loggedUserLabel);
        panel.add(friendsLabel);
        panel.add(receiverLabel);
        panel.add(friendsList);


        setContentPane(panel);
        this.getRootPane().setDefaultButton(sendMessageButton);
        setVisible(true);
        thread.start();
    }

    @Override
    public void run() {
        while (true){
            try {
                List<Message> messages = new ArrayList<Message>();
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


                List<String> friends = new ArrayList<String>();
                getUrl = "http://localhost:8080/messages/friends";
                httpClient = HttpClientBuilder.create().build();
                get = new HttpGet(getUrl);
                get.setHeader("Authorization", JwtToken);
                response = httpClient.execute(get);

                responseString = new BasicResponseHandler().handleResponse(response);
                friends = gson.fromJson(responseString,  new TypeToken<List<String>>(){}.getType());
                for(String f: friends) {
                    boolean isOnList = false;
                    for (int i = 0; i < defaultListModel.getSize(); i++) {
                        //defaultListModel.addElement(friends.get(i));
                        if(f.equals(defaultListModel.getElementAt(i)))isOnList=true;
                    }
                    if(!isOnList)defaultListModel.addElement(f);
                }

                if(receiver != null) receiverLabel.setText("Konwersacja z "+receiver);

                Thread.sleep(2000);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }catch (Exception er){
                er.printStackTrace();
            }
        }

    }
}
