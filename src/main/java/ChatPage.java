import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatPage extends JFrame implements Runnable{

    //wartosci zmiennych JwtToken oraz enteredLogin zostal przekazane przez konstruktor
    //w klasie LoginPage po pomyslnym zalogowaniu
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

    List<Friend> myFriends = new ArrayList<Friend>();
    Thread thread;

    final String serverAddress = "http://localhost:8080";

    public ChatPage(String JwtToken, String enteredLogin) {
        super("Czat");
        this.JwtToken=JwtToken;
        this.enteredLogin=enteredLogin;
        this.thread = new Thread(this);
        setBounds(200, 100, 1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //zmiana statusu na online
        changeStatus(true);

        //budowanie interfejsu
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
                    //walidacja wiadomosci
                    if(receiver==null) JOptionPane.showMessageDialog(panel, "Brak odbiorcy!");
                    else if(content==null || content.isEmpty()) JOptionPane.showMessageDialog(panel, "Wiadomość nie może być pusta!");
                    else {
                        Message message = new Message(enteredLogin, receiver, content);

                        //wysylanie zapytania post(wyslanie wiadomosci)
                        String postUrl = serverAddress+"/messages";
                        Gson gson = new Gson();
                        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                        HttpPost post = new HttpPost(postUrl);
                        StringEntity postingString = new StringEntity(gson.toJson(message));
                        post.setEntity(postingString);
                        post.setHeader("Content-type", "application/json");
                        post.setHeader("Authorization", JwtToken);
                        CloseableHttpResponse response = httpClient.execute(post);

                        //wyczyszczenie pola tekstowego po wyslaniu wiadomosci
                        //lub wyswietlenie komunikatu o niepowodzeniu
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
                    //przekazanie nazwy odbiorcy z pola tekstowego do zmiennej
                    receiver = receiverField.getText();
            }

        });

        friendsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //pobranie nazwy zaznaczonego uzytkownika z listy
                //jesli przy uzytkowniku znajduja sie informacje o jego statusie
                //to potrzeba rozbic pobierany napis na elementy tablicy
                //i wskazac pierwszy jej element jako nazwa uzytkownika
                String receiverString = friendsList.getSelectedValue();
                String[] receiverTable = receiverString.split("\\s+");
                receiver = receiverTable[0];
            }
        });

        //ustawianie interfejsu
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

        //zmiana statusu na offline po klinknieciu w krzyzyk
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                changeStatus(false);
                e.getWindow().dispose();
            }
        });


        setContentPane(panel);
        //powiozanie przycisku wyslij z klawiszem enter
        this.getRootPane().setDefaultButton(sendMessageButton);
        setVisible(true);
        thread.start();
    }

    //funcja odpowiedzialna za zmiane statusu uzytkownika(online/offline)
    public void changeStatus(boolean isOnline){
        try {
            String patchUrl = serverAddress+"/users/"+isOnline;
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPatch patch = new HttpPatch(patchUrl);
            patch.setHeader("Content-type", "application/json");
            patch.setHeader("Authorization", JwtToken);
            CloseableHttpResponse response = httpClient.execute(patch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //funkcja uruchamiana po wystartowaniu watku
    @Override
    public void run() {
        while (true){
            try {
                //pobranie listy wszystkich widomosci wymieniony z danym uzytkownikiem
                //(poprzez wyslanie zapytania Http)
                List<Message> messages = new ArrayList<Message>();
                Gson gson = new Gson();
                String getUrl = serverAddress+"/messages?username="+receiver;
                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet get = new HttpGet(getUrl);
                get.setHeader("Authorization", JwtToken);
                CloseableHttpResponse response = httpClient.execute(get);
                String responseString = new BasicResponseHandler().handleResponse(response);
                //czysczenie pola tekstowego i dodawanie wiadomosci
                messages = gson.fromJson(responseString,  new TypeToken<List<Message>>(){}.getType());
                messagesArea.setText("");
                for (Message m: messages) {
                    messagesArea.setText(messagesArea.getText()+m.getSender()+" <"+m.getPost_date()+">: " + m.getContent()+"\n");
                }
                //wyslanie zapytanie typy patch:
                //ustawiajacego status nieprzeczytanych wiadomosci
                //z danym uzytkownikiem na przeczytane
                try {
                    String patchUrl = serverAddress+"/messages/"+receiver;
                    httpClient = HttpClientBuilder.create().build();
                    HttpPatch patch = new HttpPatch(patchUrl);
                    patch.setHeader("Content-type", "application/json");
                    patch.setHeader("Authorization", JwtToken);
                    response = httpClient.execute(patch);
                } catch (IOException er) {
                    throw new RuntimeException(er);
                }

                //wyslanie zapytania pobierajacego liste zawierajaco dane znajomych
                List<Friend> friends = new ArrayList<Friend>();
                getUrl = serverAddress+"/messages/friends";
                httpClient = HttpClientBuilder.create().build();
                get = new HttpGet(getUrl);
                get.setHeader("Authorization", JwtToken);
                response = httpClient.execute(get);

                responseString = new BasicResponseHandler().handleResponse(response);
                friends = gson.fromJson(responseString,  new TypeToken<List<Friend>>(){}.getType());

                //petla sprawdzajaca czy dany uzytkownik jest na już liscie
                // oraz jaki jest jego status oraz czy ostatnia wiadomosc od niego
                //zostala juz przez nas odczytana. Po sprawdzeniu tego funkcja ustawia
                //odpowiednie wartosci pol na liscie znajomych
                for(Friend f: friends) {
                    boolean isOnList = false;
                    for (int i = 0; i < defaultListModel.getSize(); i++) {
                        String listElementFullString = defaultListModel.getElementAt(i);
                        String[] listElementTable = listElementFullString.split("\\s+");
                        String listElement = listElementTable[0];
                        if(f.getFriend().equals(listElement)){
                            isOnList=true;
                            if(f.getIs_online()==1 && f.getIs_all_read()==0)defaultListModel.setElementAt(f.getFriend()+" - aktywny, nowa wiadomość", i);
                            else if(f.getIs_online()==1)defaultListModel.setElementAt(f.getFriend()+" - aktywny", i);
                            else if(f.getIs_all_read()==0)defaultListModel.setElementAt(f.getFriend()+" - nowa wiadomość", i);
                            else defaultListModel.setElementAt(f.getFriend(), i);

                        }
                    }
                    if(!isOnList && f.getIs_online()==1 && f.getIs_all_read()==0)defaultListModel.addElement(f.getFriend()+" - aktywny, nowa wiadomość");
                    else if(!isOnList && f.getIs_online()==1)defaultListModel.addElement(f.getFriend()+" - aktywny");
                    else if(!isOnList && f.getIs_all_read()==0)defaultListModel.addElement(f.getFriend()+" - nowa wiadomość");
                    else if(!isOnList)defaultListModel.addElement(f.getFriend());
                }
                //ustawienie wyswietlajacego sie odbiorcy
                if(receiver != null) receiverLabel.setText("Konwersacja z "+receiver);

                //uspienie watku na 2 sekundy
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }catch (Exception er){
                er.printStackTrace();
            }
        }

    }
}
