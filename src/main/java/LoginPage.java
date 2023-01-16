import com.google.gson.Gson;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LoginPage extends JFrame {
    JButton loginButton, registerButton;
    JTextField loginField;
    JPasswordField passwordField;
    JLabel loginLabel, passwordLabel, titleLabel;
    JPanel panel;

    public LoginPage(){
        super("Logowanie");
        setBounds(200,100,700,420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(null);

        //tworzenie interfejsu
        titleLabel = new JLabel("Logowanie");
        loginLabel = new JLabel("Login:");
        passwordLabel = new JLabel("Hasło:");

        loginField = new JTextField();
        passwordField = new JPasswordField();

        loginButton = new JButton("Zaloguj");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //pobranie danych z pól tekstowych
                try {

                    String enteredLogin = loginField.getText();
                    String enteredPassword = passwordField.getText();
                    User candidateUser = new User(enteredLogin, enteredPassword);

                    //wysylanie zapytania post(logowanie)
                    String postUrl = "http://localhost:8080/login";
                    Gson gson = new Gson();
                    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                    HttpPost post = new HttpPost(postUrl);
                    StringEntity postingString = new StringEntity(gson.toJson(candidateUser));//gson.tojson() converts your pojo to json
                    post.setEntity(postingString);
                    post.setHeader("Content-type", "application/json");
                    CloseableHttpResponse response = httpClient.execute(post);

                    //pobieranie status odpowiedzi oraz JWT
                    Header[] headers = response.getAllHeaders();
                    String jwToken = null;
                    headers = response.getHeaders("Authorization");
                    if (headers != null && headers.length > 0) {
                        jwToken = headers[0].getValue();
                    }
                    //jesli logowanie sie powiodlo zamknij to okno i otworz okno czatu
                    //jesli nie wyswietl odpowiedni komunikat
                    if(response.getStatusLine().getStatusCode() == 200){
                        dispose();
                        ChatPage chatPage = new ChatPage(jwToken, enteredLogin);
                    }else{
                        JOptionPane.showMessageDialog(panel, "Dane logowania sa nieprawidlowe!");
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        registerButton = new JButton("Zarejestruj");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //pobranie danych z pól tekstowych

                try {
                    String enteredLogin = loginField.getText();
                    String enteredPassword = passwordField.getText();
                    User candidateUser = new User(enteredLogin, enteredPassword);

                    //wysylanie zapytania post(rejestracja)
                    String postUrl = "http://localhost:8080/users";
                    Gson gson = new Gson();
                    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                    HttpPost post = new HttpPost(postUrl);
                    StringEntity postingString = new StringEntity(gson.toJson(candidateUser));
                    post.setEntity(postingString);
                    post.setHeader("Content-type", "application/json");
                    CloseableHttpResponse response = httpClient.execute(post);

                    System.out.println(response.getStatusLine().getStatusCode());
                    //wystwietlenie odpowiedniego komunikatu w zaleznosci od powodzenia rejestracji
                    if(response.getStatusLine().getStatusCode() == 200){
                        JOptionPane.showMessageDialog(panel, "Rejestracja udana!");
                    }else {
                        JOptionPane.showMessageDialog(panel, "Podana nazwa uzytkownika istnieje!");
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });

        //ustawiania interfejsu
        titleLabel.setFont(new Font("Serif", Font.PLAIN, 28));
        loginLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        passwordLabel.setFont(new Font("Serif", Font.PLAIN, 20));

        titleLabel.setBounds(280,20,200,100);
        loginLabel.setBounds(150, 100, 100,100);
        passwordLabel.setBounds(150, 160, 100,100);

        loginField.setBounds(250, 130, 250,40);
        passwordField.setBounds(250, 190, 250,40);

        loginButton.setBounds(120,270,200,40);
        registerButton.setBounds(370,270,200,40);

        panel.add(titleLabel);
        panel.add(loginLabel);
        panel.add(passwordLabel);
        panel.add(loginField);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        setContentPane(panel);
        //powiozanie przycisku zaloguj z klawiszem enter
        this.getRootPane().setDefaultButton(loginButton);
        setVisible(true);
    }

    public static void main(String[] args) {
        LoginPage loginPage = new LoginPage();
    }
}
