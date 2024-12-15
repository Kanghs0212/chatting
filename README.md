# Java만을 이용하여 간단한 채팅방 프로그램 구현
## Talkify
- 강의시간에 프로젝트로 진행한 간단한 채팅방 구현 프로그램입니다.
- 프로젝트 명은 Talkify로 Talk와 Notify의 합성어입니다.
- java의 소켓 프로그래밍, JDBC, Swing 등등의 클래스들을 이용하여 채팅방을 구현하였습니다.


&nbsp;&nbsp;
![image](https://github.com/user-attachments/assets/1d1137ae-ee25-4a06-ad8b-a446ef6667ee)


### 특징

1. 온라인 채팅과 그림판 기능이 들어가 있는 자바 프로그램입니다. 사용자는 서버에 등록하고 로그인하면 다양한 사용자들이 로비에 있으며, 이 사용자들을 추가하여 채팅방을 신설하고 대화를 나눌 수 있습니다.
2. 채팅방 안에서는 채팅을 만든 호스트가 그림판을 통해 그림을 그려 전송하고, 간단한 장애물 피하기 미니게임도 즐길 수 있습니다.
3. 사용자의 비밀번호는 SHA-256 암호화 알고리즘을 이용하여 Encrypt한 뒤 DB에 저장합니다.
4. 채팅방 안에서도 특정 인원과 일명 '귓속말'을 이용하여 1대1 개인 대화가 가능합니다.


### 클래스 다이어그램
![image](https://github.com/user-attachments/assets/e38e7ea9-015f-4060-8149-b609b5b187fe)

&nbsp;&nbsp;

#### DTO 클래스 및 프로토콜
![image](https://github.com/user-attachments/assets/365fab1d-b8dd-452d-a5d7-961ae4059f70)


&nbsp;&nbsp;


### 실행화면
![image](https://github.com/user-attachments/assets/e17e9416-0d73-4026-a864-9e73b1ebf966)
※ 초기 실행시 로그인 화면
- 로그인 성공시 성공했다고 알람
- 계정이 없을 경우 가입하기 버튼으로 가입

&nbsp;
![image](https://github.com/user-attachments/assets/f49b4d21-ae95-479c-8c5f-182446e30179)
※ 접속 후 로비 화면 및 초대 후 방 생성 화면
- 접속한 사용자의 이름을 입력, 추가, 생성
- 잘못 초대한 인원은 리스트 클리어 버튼으로 삭제

  
&nbsp;

![image](https://github.com/user-attachments/assets/ccdc054e-1498-4e9b-b1a4-6d09450a4fbd)
※ 채팅방 화면
- 채팅방 인원끼리 대화, 귓속말 가능
- 그림판 기능을 통해 호스트는 그림을 그려 전달

&nbsp;

![image](https://github.com/user-attachments/assets/a00d319c-d03d-4f7a-a8f0-57e03b70c50b)
※ 채팅방 내 미니게임 화면
- 채팅방에서 간단한 미니게임, 최고 기록은 저장
