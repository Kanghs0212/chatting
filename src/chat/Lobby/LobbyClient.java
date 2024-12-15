package chat.Lobby;

import chat.Login.LoginFrame;
import chat.Protocol;
import chat.chatroom.ChatClient;
import chat.chatroom.ChatFrame;
import chat.chatroom.DrawingClient;
import chat.chatroom.DrawingServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LobbyClient extends JFrame {

    public static DrawingClient client; // 로비클라이언트로 setvisible
    JTextArea textArea;
    JList P_list; // 현재 로비에서 온라인 상태인 사용자들의 JList
    JList I_List; // 초대한 사용자 J리스트
    JTextField MsgField; // 메시지 필드
    JButton sendbtn; // 전송 버튼
    JButton createbtn; // 채팅방 생성 버튼
    JButton clearbtn; // 클리어 버튼(초대한 인원 리스트)
    Socket socket;
    String name="";
    DataInputStream dis;
    DataOutputStream dos;
    DefaultListModel model = new DefaultListModel(); // 참가한 사용자들을 model에 저장함.
    DefaultListModel inviteModel = new DefaultListModel(); // 초대한 사용자들을 model에 저장함.
    List<String> nameList = Collections.synchronizedList(new ArrayList<>());// 현재 온라인 상태인 사용자들
    List<String> inviteList = Collections.synchronizedList(new ArrayList<>()); // 초대된 사용자들

    public LobbyClient() {
        setTitle("Lobby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(650, 400, 500, 350);

        textArea = new JTextArea();
        textArea.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // 사용자 리스트 패널
        P_list = new JList(model);
        inviteModel.addElement("초대한 사용자");
        I_List = new JList(inviteModel);

        JScrollPane participantpane = new JScrollPane(P_list);
        JScrollPane invite = new JScrollPane(I_List);

        add(participantpane, BorderLayout.EAST);
        add(invite, BorderLayout.WEST);

        // 리스트 패널 크기 설정
        invite.setPreferredSize(new Dimension(100, invite.getPreferredSize().height));
        participantpane.setPreferredSize(new Dimension(100, participantpane.getPreferredSize().height));

        // 하단 패널 및 버튼 정렬
        JPanel sendpanel = new JPanel();
        sendpanel.setLayout(new BorderLayout());

        MsgField = new JTextField();

        // 버튼들을 가로로 배치하는 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10)); // 1행, 3열로 정렬, 버튼 간 여백 추가
        sendbtn = new JButton("추가");
        createbtn = new JButton("채팅방 생성");
        clearbtn = new JButton("리스트 클리어");

        // 버튼 추가
        buttonPanel.add(sendbtn);
        buttonPanel.add(createbtn);
        buttonPanel.add(clearbtn);

        // 메시지 입력 필드와 버튼 패널 추가
        sendpanel.add(MsgField, BorderLayout.CENTER);
        sendpanel.add(buttonPanel, BorderLayout.SOUTH);

        add(sendpanel, BorderLayout.SOUTH);

        // 버튼 동작 설정 초대할 인원 메시지 전송
        sendbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = MsgField.getText().trim();
                if (!msg.isEmpty()) {
                    msg = "/i " + msg;
                    sendMessage(msg);
                }
            }
        });

        // 채팅방 생성 버튼
        createbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = "/newChat " + name;
                if (inviteModel.size() > 1) {
                    sendMessage(msg);
                }
            }
        });

        // 엔터로도 초대가 되도록 설정
        MsgField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                String msg = MsgField.getText().trim();
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER && !msg.isEmpty()) {
                    msg = "/i " + msg;
                    sendMessage(msg);
                }
            }
        });

        // 초대한 인원 클리어
        clearbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String msg = "/clear";
                if (!inviteModel.isEmpty()) {
                    sendMessage(msg);
                }
            }
        });

        // 클라이언트 스레드 시작
        ClientThread clientThread = new ClientThread();
        clientThread.setDaemon(true);
        clientThread.start();

        setVisible(true);
    }


    // 클라이언트 스레드
    class ClientThread extends Thread{
        @Override
        public void run(){
            try{
                String id = LoginFrame.userInfo.get(0).getId(); // 사용자 id를 가져온다.
                name = id;
                socket = new Socket(Protocol.SERVER_IP,Protocol.SEVER_LOBBY_PORT);
                textArea.append("서버에 접속되었습니다\n");
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                dis = new DataInputStream(is);
                dos = new DataOutputStream(os);
                dos.writeUTF(id);
                while(true){
                    String msg = dis.readUTF();
                    if(msg.substring(0,10).equals(Protocol.LOBBY_USER_INFO)){//유저 이름 리스트
                        String username = msg.substring(Protocol.LOBBY_USER_INFO.length());
                        synchronized (nameList) {
                            nameList.add(username);
                            model.addElement(username);
                        }
                    }
                    else if(msg.substring(0,10).equals(Protocol.USER_JOIN)){ // 유저 입장했을때
                        String notification = msg.substring(Protocol.USER_JOIN.length());
                        synchronized (nameList) {
                            textArea.append(notification + "\n");
                        }
                    }
                    else if(msg.substring(0,10).equals(Protocol.USER_LEAVE)){ // 유저 나갔을때
                        String notification = msg.substring(Protocol.USER_LEAVE.length());
                        synchronized (nameList) {
                            nameList.clear();
                            model.clear();
                            textArea.append(notification + "\n");

                        }
                    }else if(msg.substring(0,10).equals(Protocol.USER_VALID)){ // 사용자 초대
                        String invitedUser = msg.substring(Protocol.USER_VALID.length());
                        synchronized (inviteList) {
                            inviteList.add(invitedUser);
                            inviteModel.addElement(invitedUser);
                        }
                    }else if(msg.substring(0,10).equals(Protocol.CLEAR_INVITES)){ // 사용자 초대 목록 초기화
                        synchronized (inviteList) {
                            inviteList.clear();
                            inviteModel.clear();
                            inviteModel.addElement("초대한 사용자");
                        }
                    }else if(msg.substring(0,16).equals("/CreateChatFrame")){ // 채팅방 생성 
//                        System.out.println("createChatFrame");
                        String host = msg.substring(17);
                        boolean isCompleted=false;
                        int port=0;
                        String messages="";
//                        System.out.println(host);
//                        System.out.println(name);
                        // 챗프레임을 new 하여 생성 후 채팅방 정보 입력
                        if(host.equals(name)){
                            ChatFrame chatFrame = new ChatFrame(host);
                            isCompleted = chatFrame.waitForCompletion();
                            port = chatFrame.getPort();
                            messages = chatFrame.getMessages();
                        }
                        
                        // 만약 정상적으로 채팅방이 생성될 경우
                        if(isCompleted){
                            new DrawingServer(port+1);
                            String players ="";

                            // 왜 뒤에 &를 붙이냐면, 메시지를 전송받고 나서 split("&")를 통해 메시지를 토큰화 하기 때문이다.
                            // 만약 한명도 없는 경우라도 none&으로 하여 아무도 없다는걸 명시해준다.
                            System.out.println("성공적으로 채팅방 신설");
                            if(inviteList.size()==0)
                                players="none&";
                            else{
                                for (int i = 0; i < inviteList.size(); i++) {
                                    players += inviteList.get(i) + "&";
                                }
                            }
                            
                            // 위의 이유와 동일
                            if(messages.length()<4) // 메시지가 비어있음.
                                messages="none&";

                            String[] IP = String.valueOf(InetAddress.getLocalHost()).split("/");
//                            System.out.println("/invitePlayers " + players +" " + host+" " + port +" "+ messages);

                            // 서버에게 해당 명령어를 다시 전달해, 브로드캐스트하도록 함.
                            synchronized (this) {
                                sendMessage("/playersInvite&" + players + host + "&"+ IP[1] + "&" + port + "&" + messages);
                            }
                        }
                    }else if(msg.startsWith("/playersInvite")){ // 초대한 인원들을 생성한 채팅방으로 초대
                        String[] parts = msg.split("&");
                        //System.out.println("저는 " + id + "입니다.");
//                        for (int i = 0; i < parts.length; i++) {
//                            System.out.println(parts[i]);
//                        }
                        String messages = parts[4];
                        if(messages.equals("none"))
                            messages= "";
                        int roomPort = Integer.parseInt(parts[3]);
                        String IP = parts[2];
                        String host = parts[1];

                        synchronized (this) {
//                            System.out.println("아이피 주소와 포트는" + IP + roomPort);
                            // 해당 클라이언트 스레드의 이름이 호스트와 같다면, 권한을 host로 해서 챗클라이언트를 생성
                            if(name.equals(host)){
                                new ChatClient(roomPort,IP, name, "admin", host, messages);
                                SwingUtilities.invokeLater(() -> {
                                    client = new DrawingClient(roomPort+1,IP,true);
                                    client.setVisible(true);
                                });
                            }
                            // 일반 유저
                            else{
                                new ChatClient(roomPort,IP, name, "user", host, messages);
                                SwingUtilities.invokeLater(() -> {
                                    client = new DrawingClient(roomPort+1,IP,false);
                                    client.setVisible(true);
                                });
                            }
                        }
                    }
                    else{
                        textArea.append(msg.substring(10)+"\n");
                    }
                    textArea.setCaretPosition(textArea.getText().length());;
                }
            }catch(UnknownHostException e){
                textArea.append("서버 주소가 이상합니다");
            }catch(IOException e){
                textArea.append("서버와 연결이 끊겼습니다\n");
            }

        }
    }

    void sendMessage(String msg){
        MsgField.setText("");

        Thread t = new Thread(){
            @Override
            public void run(){
                try{
                    dos.writeUTF(msg);
                    dos.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

}


