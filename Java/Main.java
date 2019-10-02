import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Main {
    static private int n = 100;
    static private int[][] pole = new int[n][n];
    static private int w, h;
    static private boolean isPause = false, slow = false;

    static public void resizePole(int _n) {
        n = _n;
        pole = new int[n][n];
        shufflePole();
    }

    static public void drawPole(GL2 gl2) {
        float r = 2.f / n;
        gl2.glColor3f(1, 1, 1);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (pole[i][j] == 1) {
                    gl2.glBegin(GL2ES3.GL_QUADS);
                    gl2.glVertex2f(2.f / n * i - 1, 2.f / n * j - 1);
                    gl2.glVertex2f(2.f / n * i + r - 1, 2.f / n * j - 1);
                    gl2.glVertex2f(2.f / n * i + r - 1, 2.f / n * j + r - 1);
                    gl2.glVertex2f(2.f / n * i - 1, 2.f / n * j + r - 1);
                }
            }
        }
    }

    static public int mmod(int a, int b) {
        return (a % b + b) % b;
    }

    static public void shufflePole() {
        final Random random = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                pole[i][j] = random.nextInt(2);
            }
        }
    }

    static public void nextGen() {
        int[][] nPole = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sm = 0;
                for (int c = -1; c < 2; c++) {
                    for (int k = -1; k < 2; k++) {
                        if (c == 0 && k == 0) continue;
                        sm += pole[mmod(i + c, n)][mmod(j + k, n)];
                    }
                }
                if (pole[i][j] == 1) {
                    if (sm != 2 && sm != 3) {
                        nPole[i][j] = 0;
                    } else nPole[i][j] = 1;
                } else if (sm == 3) nPole[i][j] = 1;
                else nPole[i][j] = 0;
            }
        }
        pole = nPole;
    }

    public static void main(String[] args) {
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities(glprofile);
        final GLCanvas glcanvas = new GLCanvas(glcapabilities);
        glcanvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {

                if (((int) (((float) e.getX()) / w * n) >= n) || ((int) (((float) (h - e.getY())) / h * n) >= n))
                    return;

                if (e.getModifiers() == InputEvent.BUTTON1_MASK)
                    pole[(int) (((float) e.getX()) / w * n)][(int) (((float) (h - e.getY())) / h * n)] = 1;
                else if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
                    pole[(int) (((float) e.getX()) / w * n)][(int) (((float) (h - e.getY())) / h * n)] = 0;
                }
            }

            @Override public void mouseMoved(MouseEvent e) {}
        });
        glcanvas.addKeyListener(new java.awt.event.KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'r':
                        shufflePole();
                        break;
                    case '+': {
                        int[][] nPole = new int[n + 2][n + 2];
                        for (int i = 1; i < n + 1; i++) {
                            for (int j = 1; j < n + 1; j++) {
                                nPole[i][j] = pole[i - 1][j - 1];
                            }
                        }
                        n += 2;
                        pole = nPole;
                        break;
                    }
                    case '-':
                        if (n > 3) {
                            int[][] nPole = new int[n - 2][n - 2];
                            for (int i = 0; i < n - 2; i++) {
                                for (int j = 0; j < n - 2; j++) {
                                    nPole[i][j] = pole[i + 1][j + 1];
                                }
                            }
                            n -= 2;
                            pole = nPole;
                        }
                        break;
                    case 'c':
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < n; j++) {
                                pole[i][j] = 0;
                            }
                        }
                        break;
                    case ' ':
                        isPause = !isPause;
                        break;
                    case 't':
                        slow = !slow;
                        break;
                    case 'q':
                        System.exit(0);
                        break;
                }
            }

            @Override public void keyReleased(KeyEvent e) {}
        });
        shufflePole();

        glcanvas.addGLEventListener(new GLEventListener() {

            @Override
            public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
                w = width;
                h = height;
                System.out.println(w + " " + h);
            }

            @Override public void init(GLAutoDrawable glautodrawable) {}

            @Override public void dispose(GLAutoDrawable glautodrawable) {}

            @Override
            public void display(GLAutoDrawable glautodrawable) {
                GL2 gl2 = glautodrawable.getGL().getGL2();
                gl2.glMatrixMode(GL2.GL_MODELVIEW);
                gl2.glLoadIdentity();
                gl2.glClearColor(0, 0, 0, 1);
                gl2.glClear(GL2ES3.GL_COLOR_BUFFER_BIT);

                gl2.glColor3f(0, 0, 0);
                gl2.glBegin(GL2ES3.GL_QUADS);
                gl2.glVertex2f(-1, -1);
                gl2.glVertex2f(1, -1);
                gl2.glVertex2f(1, 1);
                gl2.glVertex2f(-1, 1);
                gl2.glEnd();

                drawPole(glautodrawable.getGL().getGL2());
                if (!isPause) nextGen();
            }
        });

        final Frame frame = new Frame("The life game");
        frame.add(glcanvas);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowevent) {
                frame.remove(glcanvas);
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setSize(500, 500);
        frame.setVisible(true);
        while (true) {
            glcanvas.display();

            try {
                if (slow) Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }
}
