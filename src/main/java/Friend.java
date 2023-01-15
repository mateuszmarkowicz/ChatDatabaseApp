
public class Friend {
    //klasa odzwierciedlajaca dane znajomego
    //czyli uzytkownika z ktorym mamy chociaz jedna wiadomosc
    private String friend;
    private int is_online;
    private int is_all_read;

    public Friend() {
    }

    public Friend(String friend, int is_online, int is_all_read) {
        this.friend = friend;
        this.is_online = is_online;
        this.is_all_read = is_all_read;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }

    public int getIs_online() {
        return is_online;
    }

    public void setIs_online(int is_online) {
        this.is_online = is_online;
    }

    public int getIs_all_read() {
        return is_all_read;
    }

    public void setIs_all_read(int is_all_read) {
        this.is_all_read = is_all_read;
    }
}
