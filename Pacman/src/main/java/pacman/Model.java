/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author mbarb
 */
public class Model extends JPanel implements ActionListener {

    private Dimension d;//size of board
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);//font for text
    private boolean inGame = false;
    private boolean dying = false;//player state

    private final int BLOCK_SIZE = 24; //size of blocks
    private final int N_BLOCKS = 15; //number of blocks used 15 x 15 = 225 positions
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;

    private int N_GHOSTS = 6;
    private int lives, score;
    private int[] dx, dy; //dx data x, dy data y
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;
    private int req_dx, req_dy;

    private final int validSpeed[] = {1, 2, 3, 4, 5, 6, 7, 8};
    private final int maxSpeed = 6;
    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    //level data
    private final short levelData[] = {// 0 = blue, 1 = left border, 2 = top boarder, 4 = right border, 8 = bottom border, 16 white dot. To make grid number add numbers together.
        19, 22, 0, 19, 26, 26, 30, 0, 19, 18, 18, 18, 18, 18, 22,
        25, 24, 26, 20, 0, 0, 0, 0, 17, 20, 0, 0, 0, 0, 21,
        0, 0, 0, 21, 0, 19, 26, 26, 16, 20, 0, 0, 0, 0, 21,
        19, 23, 0, 21, 0, 21, 0, 0, 17, 20, 0, 0, 0, 0, 21,
        17, 20, 0, 21, 0, 21, 0, 0, 17, 16, 26, 18, 18, 26, 20,
        17, 20, 0, 21, 0, 17, 18, 18, 16, 20, 0, 17, 20, 0, 21,
        25, 24, 24, 24, 24, 24, 24, 16, 16, 20, 0, 17, 20, 0, 21,
        0, 0, 0, 0, 0, 0, 0, 17, 16, 20, 0, 25, 24, 24, 20,
        19, 18, 18, 26, 26, 26, 26, 16, 16, 20, 0, 0, 0, 0, 21,
        17, 16, 20, 0, 0, 0, 0, 17, 16, 16, 18, 18, 26, 18, 20,
        17, 16, 20, 0, 0, 0, 19, 16, 16, 16, 16, 28, 0, 21, 20,
        17, 16, 20, 0, 0, 0, 17, 16, 16, 16, 28, 0, 23, 0, 25,
        17, 16, 20, 0, 0, 0, 25, 16, 16, 28, 0, 19, 16, 21, 0,
        17, 16, 20, 0, 0, 0, 0, 17, 20, 0, 19, 16, 16, 16, 22,
        25, 24, 24, 26, 26, 26, 26, 24, 24, 26, 24, 24, 24, 24, 28
    };//all obsticals must be surrounded by border to stop anything passing through

    public Model() {
        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }

    private void loadImages() {//Path will need to be changed for differnt computer use.
        down = new ImageIcon("\"C:\\Users\\mbarb\\Desktop\\Pacman\\src\\images\\down.gif\"").getImage();
        up = new ImageIcon("\"C:\\Users\\mbarb\\Desktop\\Pacman\\src\\images\\up.gif\"").getImage();
        right = new ImageIcon("\"C:\\Users\\mbarb\\Desktop\\Pacman\\src\\images\\right.gif\"").getImage();
        left = new ImageIcon("\"C:\\Users\\mbarb\\Desktop\\Pacman\\src\\images\\left.gif\"").getImage();
        ghost = new ImageIcon("\"C:\\Users\\mbarb\\Desktop\\Pacman\\src\\images\\ghost.gif\"").getImage();
        heart = new ImageIcon("\"C:\\Users\\mbarb\\Desktop\\Pacman\\src\\images\\heart.gif\"").getImage();
    }

    private void initVariables() {
        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(40, this);//miliseconds
        timer.restart();

    }

    private void initGame() {
        lives = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel() {
        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }
    }

    private void playGame(Graphics2D g2d) {
        if(dying){
            death();
        }else{
            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }
    
    public void movePacman(){
        int pos;
        short ch;
        
        //detmine pacman possition
        if(pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0){
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
                    ch = screenData[pos];
                if((ch & 16)!=0){
                    screenData[pos] = (short) (ch & 15);
                    score++;
                }
                if(req_dx != 0 || req_dy != 0){
                   if(!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                   ||(req_dy == 1 && req_dy == 0 && (ch & 4)!= 0)
                   ||(req_dx == 0 && req_dy == -1 && (ch & 2)!= 0)
                   ||(req_dx == 0 && req_dy == 1 && (ch & 8)!= 0)))
                   {
                        pacmand_x = req_dx;
                        pacmand_y = req_dy;
                    }
                }
                if((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                ||(pacmand_x == 1 && pacmand_y == 0 && (ch & 4)!= 0)
                ||(pacmand_x == 0 && pacmand_y == -1 && (ch & 2)!= 0)
                ||(pacmand_x == 0 && pacmand_y == 1 && (ch & 8)!= 0)){
                    pacmand_x = 0;
                    pacmand_y = 0;
                }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }
    
    //Checks which directional button is pressed
    public void drawPacman(Graphics2D g2d){
        if(req_dx == -1){
            g2d.drawImage(left, pacman_x +1, pacman_y +1, this);
        }else if(req_dx == 1){
            g2d.drawImage(right, pacman_x +1, pacman_y +1, this);
        }else if(req_dy == -1){
            g2d.drawImage(up, pacman_x +1, pacman_y +1, this);
        }else {
            g2d.drawImage(down, pacman_x +1, pacman_y +1, this);
        }
    }
    
    public void moveGhosts(Graphics2D g2d){
        int pos;
        int count;
        for (int i = 0; i < N_GHOSTS; i++){
            if(ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0){
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int)(ghost_y[i] / BLOCK_SIZE);
                
                count = 0;
                //border info
                if((screenData[pos] & 1) == 0 && ghost_dx[i] != 1){
                   dx[count] = -1;
                   dy[count] = 0;
                   count++;
                }
                if((screenData[pos] & 2) == 0 && ghost_dy[i] != 1){
                   dx[count] = 0;
                   dy[count] = -1;
                   count++;
                }
                if((screenData[pos] & 4) == 0 && ghost_dx[i] != -1){
                   dx[count] = 1;
                   dy[count] = 0;
                   count++;
                }
                if((screenData[pos] & 8) == 0 && ghost_dx[i] != -1){
                   dx[count] = 0;
                   dy[count] = 1;
                   count++;
                }
                
                if(count == 0){
                    if((screenData[pos] & 15) == 15){
                        ghost_dy[i] = 0;
                        ghost_dx[i] = 0;
                    }else{
                        ghost_dy[i] = -ghost_dy[i];
                        ghost_dx[i] = -ghost_dx[i];
                    }
                }else{
                    count = (int)(Math.random() * count);
                    
                    if(count > 3){
                        count = 3;
                    }
                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }
            }
            
            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);
            
            //if pacman touches ghost lose life
            if(pacman_x > (ghost_x[i] -12) && pacman_x < (ghost_x[i] + 12)
               && pacman_y > (ghost_y[i] -12) && pacman_y < (ghost_y[i] + 12)
               && inGame)
                {
                dying = true;
                }
        }
    }
    
    public void drawGhost(Graphics2D g2d, int x, int y){
        g2d.drawImage(ghost, x, y, this);
    }
    
    public void checkMaze(){//is level completed
        int i = 0;
        boolean finished = true;
        
        while(i < N_BLOCKS * N_BLOCKS && finished){
            if((screenData[i] & 48) != 0){
                finished = false;
            }
        }i++;
        
        //if level is completed increase speed of player and ghosts
        if(finished){
            score += 50;
            
            if(N_GHOSTS < MAX_GHOSTS){
                N_GHOSTS++;
            }
            if(currentSpeed < maxSpeed){
                currentSpeed++;
            }
        }   initLevel();
    }
    
    private void death(){
        lives--;
        if(lives == 0){
            inGame = false;
        }
        
        continueLevel();
    }

    private void continueLevel() {
        int dx = 1;
        int random;//for ghost speed

        for (int i = 0; i < N_GHOSTS; i++) {
            ghost_y[i] = 4 * BLOCK_SIZE;
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeed[random];
        }

        pacman_x = 1 * BLOCK_SIZE;
        pacman_y = 1 * BLOCK_SIZE;
        pacmand_x = 0;
        pacmand_y = 0;
        req_dx = 0;
        req_dy = 0;
        dying = false;
    }
    
    public void drawMaze(Graphics2D g2d){
        short i = 0;
        int x,y;
        
        for(y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE){
            for(x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE){
                g2d.setColor(new Color(170,7,219));
                g2d.setStroke(new BasicStroke(5));
                
                if((screenData[i] == 0)){
                    g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }
                if((screenData[i] & 1) != 0){//left border
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }
                if((screenData[i] & 2) != 0){//top border
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }
                if((screenData[i] & 4) != 0){//right border
                    g2d.drawLine(x + BLOCK_SIZE -1, y, x + BLOCK_SIZE -1, y + BLOCK_SIZE -1);
                }
                if((screenData[i] & 8) != 0){//bottom border
                    g2d.drawLine(x, y + BLOCK_SIZE -1, x + BLOCK_SIZE -1, y + BLOCK_SIZE -1);
                }
                if((screenData[i] & 16) != 0){//white dot
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x+10, y+10, 6, 6);
                }
                i++;
            }
        }
    }
    
    
    

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(21,219,7));
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    class TAdapter extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                }
                if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
