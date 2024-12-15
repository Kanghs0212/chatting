package chat.chatroom;


import chat.Lobby.LobbyServer;
import chat.Protocol;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {
    ServerBack sb;
    String host; // 현 채팅방의 호스트가 누군지 알려준다.
    public ChatServer(int port, String host){
        this.host=host;
        sb = new ServerBack(port);
        sb.ServerOpen();
        sb.start();
    }

    class ServerBack extends Thread{
        List<ThreadList> T_list = Collections.synchronizedList(new ArrayList<>());
        List<String> idList = Collections.synchronizedList(new ArrayList<>());
        ServerSocket serversocket;
        Socket socket;
        int port;

        public ServerBack(int port){
            this.port=port;
        }

        //서버 열기함수
        public void ServerOpen(){
            try{
                serversocket = new ServerSocket(port);
            } catch(IOException e){
                System.out.println("start error");
            }
        }
        public void run(){
            try{
                while(true){
                    socket = serversocket.accept();
                    ThreadList thread = new ThreadList();
                    thread.start();
                }
            } catch(Exception e){
                System.out.println(e);
                //e.printStackTrace();
            }
        }


        class ThreadList extends Thread{
            private DataInputStream dis;
            private DataOutputStream dos;
            String id;
            String msg;
            public ThreadList(){
                try{
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());
                    T_list.add(this);
                    id = dis.readUTF();
                    idList.add(id);
                } catch(IOException e){
                    //e.printStackTrace();
                }
            }
            public void run(){
                try {
                    // 초기 입장시, 해당 플레이어의 닉네임을 리스트에 추가하라고 전송
                    SendAll(Protocol.USER_JOIN+id+"님이 입장했습니다");

                    Thread.sleep(100);

                    SendAll(Protocol.LOBBY_USER_INFO + id);
                    Thread.sleep(100);
                    for (int i = 0; i < idList.size(); i++) {
                        if(idList.get(i).equals(id)){
                            continue;
                        }
                        this.SendMessage(Protocol.LOBBY_USER_INFO + idList.get(i));
                    }

                    while (true) {
                        msg = dis.readUTF();

                        if (msg.equals("&@EXIT")) { // 클라이언트가 나갈 때 처리
                            throw new IOException(); // 예외를 발생시켜 아래 로직으로 넘어감
                        }
                        if(msg.startsWith("/w")){  // 귓속말일때
                            String[] parts = msg.split(" ", 3);
                            if(parts.length<3)
                                this.SendMessage(Protocol.USER_NOT_FOUND+"유저가 없거나 명령어가 잘못되었습니다");
                            else{
                                String recipient = parts[1];
                                String message = parts[2];

                                if(recipient.equals(id)||!checkuser(recipient)){
                                    this.SendMessage(Protocol.USER_NOT_FOUND+"유저가 없거나 명령어가 잘못되었습니다");
                                }
                                else {
                                    Whisper(message, id, recipient);
                                }
                            }
                        }
                        else { //일반 메세지
                            SendAll("[" + id + "] :" + msg);
                        }
                    }
                } catch(Exception e){
                    T_list.remove(this);
                    idList.remove(id);
//                    JOptionPane.showMessageDialog(null, idList.size()+"", "로그인", JOptionPane.ERROR_MESSAGE);
                    SendAll(Protocol.USER_LEAVE +"["+id+"] 님이 나갔습니다\n");

                    for(int i=0; i<idList.size(); i++){
                        SendAll(Protocol.LOBBY_USER_INFO+idList.get(i));
                    }

                    // id리스트가 0이면, 전부 나갔다는 의미이므로 소켓을 전부 닫아준다.
                    if(idList.size()==0){
                        try{
                            if(dis != null){
//                                JOptionPane.showMessageDialog(null, "dis close", "로그인", JOptionPane.ERROR_MESSAGE);
                                dis.close();
                            }
                            if(dos != null) {
//                                JOptionPane.showMessageDialog(null, "dos close", "로그인", JOptionPane.ERROR_MESSAGE);
                                dos.close();
                            }
                            if(socket != null){
//                                JOptionPane.showMessageDialog(null, "socket close", "로그인", JOptionPane.ERROR_MESSAGE);
                                socket.close();
                            }
                        }catch (IOException ex){
                            e.printStackTrace();
                        }
                    }
                }
            }
            // 특정 클라이언트에게만 메시지 전송
            public void SendMessage(String msg){
                try{
                    dos.writeUTF(msg);
                    dos.flush();
                } catch(IOException e){
                    //e.printStackTrace();
                }
            }
            public void SendAll(String msg){ //다른 클라이언트 전부에게 메세지를 전송
                for (ThreadList tl : T_list) {
                    tl.SendMessage(msg);
                }
            }
            //귓속말 기능, 보낸 사용자의 이름과, 보낸 사용자가 입력한 대상 이름을 통해 전송자, 수신자 결정
            public void Whisper(String msg, String from, String whisper){
                for(int i=0; i<T_list.size(); i++){
                    if(T_list.get(i).id.equals(whisper)){
                        ThreadList tl = T_list.get(i);
                        tl.SendMessage("!%#^&@$%!#"+ "["+from+"] 으로부터 귓속말 : " + msg);
                        for(int j=0; j<T_list.size(); j++){
                            if(T_list.get(j).id.equals(from)){
                                ThreadList tl2 = T_list.get(j);
                                tl2.SendMessage("!%#^&@$%!#"+ "["+whisper+"] 에게 귓속말 : " + msg);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            //현재 채팅방에 유저가 있는지 확인하는 함수
            public boolean checkuser(String cid){
                boolean check = false;
                for(int i=0; i<T_list.size(); i++){
                    if(T_list.get(i).id.equals(cid)){
                        check = true;
                        break;
                    }
                }
                return check;
            }
        }
    }

}
