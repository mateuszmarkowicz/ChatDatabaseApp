
public class Message {
    int id;
    private String sender;
    private String receiver;
    private String post_date;
    private String content;

    public Message(int id, String sender, String receiver, String post_date, String message) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.post_date = post_date;
        this.content = message;
    }

    public Message(String sender, String receiver, String post_date, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.post_date = post_date;
        this.content = message;
    }
    public Message(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = message;
    }

    public Message() {

    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getPost_date() {
        return post_date;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
