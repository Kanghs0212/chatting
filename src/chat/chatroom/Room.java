package chat.chatroom;

public class Room {
    private int socket;
    private String title;
    private String messages;
    private String host;
    private int best;

    // Getter와 Setter
    public int getSocket() {
        return socket;
    }

    public void setSocket(int socketNum) {
        this.socket = socketNum;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String roomName) {
        this.title = roomName;
    }

    public void setMessages(String msg){
        this.messages=msg;
    }

    public void addMessages(String msg){
        this.messages += msg;
    }

    public String getMessages(){
        return messages;
    }

    public void setHost(String host){
        this.host=host;
    }

    public int getBest(){return best;}
    public void setBest(int best){ this.best = best;}

    public String getHost(){
        return host;
    }

}
