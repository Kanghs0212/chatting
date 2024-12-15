package chat.chatroom;
import chat.DBConnect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class SwingAvoidGame extends JPanel implements Runnable, KeyListener {
    DBConnect db = new DBConnect();
    private int playerX = 200; // 플레이어 위치
    private int playerY = 350;
    private Room room;
    public int score = 0; // 점수
    public int best; // 최고점수
    private boolean running = true;
    private int port;
    private boolean leftPressed = false;  // 왼쪽 키 상태
    private boolean rightPressed = false; // 오른쪽 키 상태
    private int ballSpeed = 5; // 공 속도
    private int difficultyTimer = 0; // 난이도 조절 타이머
    private ArrayList<Ball> balls = new ArrayList<>(); // 공 리스트
    private Random random = new Random();

    // 공 클래스
    class Ball {
        int x, y, size;

        public Ball(int x, int y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }
    }

    public SwingAvoidGame(int port) {
        setFocusable(true);
        addKeyListener(this);
        this.port=port;
        db.connect();
        room = db.getRoomBySocketNum(port);
        best = room.getBest();

        // JOptionPane.showMessageDialog(this, "최고 점수: " + best);

        // 초기 공 추가
        for (int i = 0; i < 3; i++) {
            balls.add(new Ball(random.nextInt(400), random.nextInt(100) - 100, 30 + random.nextInt(20)));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.fillRect(playerX, playerY, 50, 50); // 플레이어

        // 공 그리기
        g.setColor(Color.RED);
        for (Ball ball : balls) {
            g.fillOval(ball.x, ball.y, ball.size, ball.size);
        }

        g.setColor(Color.black);
        g.setFont(new Font("", Font.BOLD, 15));
        g.drawString("점수 : " + score, 10, 20);

        g.drawString("최고점수 : " + best,10, 50);
    }

    public void run() {
        while (running) {
            difficultyTimer++;
            // System.out.println(difficultyTimer);
            score+=5;
            // System.out.println(score);

            if (difficultyTimer % 200 == 0) {
                ballSpeed++;
                balls.add(new Ball(random.nextInt(400), -30, 30 + random.nextInt(20))); // 새로운 공 추가
            }

            // 공 내려오기
            for (Ball ball : balls) {
                ball.y += ballSpeed;
                if (ball.y > getHeight()) {
                    ball.y = -30; // 화면 위로 리셋
                    ball.x = random.nextInt(getWidth() - ball.size); // 랜덤 위치
                }

                // 충돌 체크
                if (ball.y + ball.size > playerY && ball.x + ball.size > playerX && ball.x < playerX + 50) {
                    running = false;
                    if(best<score){
                        db.updateBestBySocketNum(port, score);
                    }
                    db.disconnect();
                    JOptionPane.showMessageDialog(this, "최종 점수: " + score);
                    return; // 게임 오버 시 루프 종료
                }
            }

            // 플레이어 이동
            if (leftPressed && playerX > 0) {
                playerX -= 10;
            }
            if (rightPressed && playerX < getWidth() - 50) {
                playerX += 10;
            }

            repaint();
            try {
                Thread.sleep(30); // 프레임 조절
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 게임 종료 메소드
    public void stopGame() {
        running = false; // 루프를 중단
    }


    // 아래에는 키를 pressed를 하면 각각 leftPressed, rightPressed가 true가 되며, 스레드가 돌아가며
    // 해당 Pressed가 true일 경우 x축 값에 값을 더하고 빼서 움직인다.
    // 마찬가지로 키를 때면 Released가 되면서 false가 되기에 움직임을 멈춘다.
    // 이렇게 구현한 이유는 keyPressed에 바로 x축 값을 더할 경우, 초기에 한번 움직이고 한번 멈췄다가 그제서야 쭉 이동하는, 반응이 느리다는 단점을 보완하기 위함.
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = true; // 왼쪽 키 눌림 상태
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = true; // 오른쪽 키 눌림 상태
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = false; // 왼쪽 키 뗌
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = false; // 오른쪽 키 뗌
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}


}
