package chat.Lobby;

import chat.Protocol;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class LobbyServer{
    ServerBack sb = new ServerBack();
    public LobbyServer(){
        sb.ServerOpen(); // Server Back 스레드의 메소드 실행
        sb.start(); // run
    }


    public static void main(String[] args) {
        new LobbyServer();
    }


    class ServerBack extends Thread{
        List<ThreadList> T_list = Collections.synchronizedList(new ArrayList<>()); // 현재 참여자의 스레드 리스트
        List<String> idList = Collections.synchronizedList(new ArrayList<>()); // 현재 참여자의 아이디
        ServerSocket serversocket;
        Socket socket;

        //서버 열기함수
        public void ServerOpen(){
            try{
                // 해당 스레드에 대해, 여러 스레드가 접근할 수 있으므로 일관성 문제가 발생가능
                // Collections의 동기화 툴로 감싸준다.
                Collections.synchronizedList(T_list);
                serversocket = new ServerSocket(Protocol.SEVER_LOBBY_PORT);
                System.out.println("IP: " + InetAddress.getLocalHost()); // 현재 컴퓨터의 ip 출력
            } catch(IOException e){
                System.out.println("start error");
            }
        }
        // 클라이언트가 접근할 때 마다 새로운 thread 생성
        public void run(){
            try{
                while(true){
                    System.out.println("서버가 시작되었습니다");
                    socket = serversocket.accept();
                    ThreadList thread = new ThreadList();
                    thread.start();
                }
            } catch(IOException e){
                //e.printStackTrace();
            }
        }

        // 클라이언트와의 통신을 관리하는 ThreadList,
        // 각 클라이언트별로 하나의 ThreadList 객체를 생성
        class ThreadList extends Thread{
            private DataInputStream dis;
            private DataOutputStream dos;
            String level;
            String id;
            String msg;
            public ThreadList(){
                try{
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());
                    T_list.add(this);
                    id = dis.readUTF(); // 접속한 사용자의 id를 받음.
                    level = "user"; // 우선은 일반 유저로 입력
                    idList.add(id);
                } catch(IOException e){
                    //e.printStackTrace();
                }
            }

            public void run(){
                try { // 초기 입장시
                    // 모두에게 입장했다는 메시지 전송
                    SendAll(Protocol.USER_JOIN+id+"님이 입장했습니다");
                    Thread.sleep(100);
                    System.out.println("["+id+"] 님이 입장하셨습니다");
                    System.out.println("[로비 유저 수] :" + T_list.size()+"명");

                    // 해당 유저 id를 전송
                    SendAll(Protocol.LOBBY_USER_INFO + id);
                    Thread.sleep(100);
                    // 단, 방금 들어온 사용자는 이전 사용자들의 아이디 리스트가 없으므로 따로 더 보내준다.
                    for (int i = 0; i < idList.size(); i++) {
                        if(idList.get(i).equals(id))
                            continue;
                        this.SendMessage(Protocol.LOBBY_USER_INFO + idList.get(i));
                    }

                    while (true) {
                        msg = dis.readUTF();
                        if(msg.startsWith("/i")){ // 초대할 사용자 추가
                            // System.out.println("/i");
                            String[] parts = msg.split(" ", 2);
                            String recipient = parts[1];

                            if(recipient.equals(id)||!checkuser(recipient)){
                                this.SendMessage(Protocol.USER_NOT_FOUND+"존재하지 않는 유저입니다.");
                            }else{

                                this.SendMessage(Protocol.USER_VALID+recipient);
                            }
                        }else if(msg.startsWith("/newChat")){ // 채팅방 생성을 하고 싶다는 요청이 오면
                            String[] parts = msg.split(" ");

                            // System.out.println("새로운 채팅방 생성 명령어");
                            String host = parts[1];
//                            System.out.println(parts[0]);
//                            System.out.println(parts[1]);
                            SendAll(Protocol.CHAT_FRAME_CREATION + host);

                        }else if(msg.startsWith(Protocol.INVITE_PLAYERS)){ // 채팅방 생성이 완료 되고, 플레이어를 초대하고 싶다고 요청

                            String[] parts = msg.split("&"); // 전부 &로 묶여져있으므로, 토큰화
//                            for (int i = 0; i < parts.length; i++) {
//                                System.out.println(parts[i]);
//                            }
                            String messages = parts[parts.length-1];
                            int roomPort = Integer.parseInt(parts[parts.length-2]);
                            String IP = parts[parts.length-3];
                            String host = parts[parts.length-4];

                            // 해당 플레이어 목록에 자신의 이름이 있을경우, 해당 스레드에게만 해당 포트와 IP로 접속하라고 메시지 전송
                            synchronized (this) {
                                for (int i = 1; i < parts.length - 3; i++) {
                                    for (int j = 0; j < T_list.size(); j++) {
                                        if(T_list.get(j).id.equals(parts[i])){
                                            ThreadList t1 = T_list.get(j);
                                            t1.SendMessage("/playersInvite&" + host + "&" + IP + "&" + roomPort + "&" + messages);
                                        }
                                    }
                                }
                            }

                        }else if(msg.startsWith("/clear")){
                            this.SendMessage(Protocol.CLEAR_INVITES);
                        }
                    }
                } catch(IOException e){ // 사용자가 퇴장 시
                    T_list.remove(this);
                    idList.remove(id);
                    System.out.println("["+id+"] 님이 나갔습니다");
                    System.out.println("[로비 유저 수] :" + T_list.size()+"명");
                    SendAll(Protocol.USER_LEAVE+"["+id+"] 님이 나갔습니다\n");
                    for(int i=0; i<idList.size(); i++){
                        SendAll(Protocol.LOBBY_USER_INFO+idList.get(i));
                    }
                }catch (InterruptedException e){
                    return;
                }
            }
            public void SendMessage(String msg){
                try{
                    dos.writeUTF(msg);
                    dos.flush();
                } catch(IOException e){
                    //e.printStackTrace();
                }
            }
            public void SendAll(String msg){ //다른 클라이언트들에게 메세지를 전송
                for (ThreadList tl : T_list) {
                    tl.SendMessage(msg);
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
