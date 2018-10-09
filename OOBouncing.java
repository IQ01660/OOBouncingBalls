import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.util.Random;


//additional Spring class
class Spring {
	public final double LENGTH = 50;
	public final double CONSTANT = 20;
	public final int WIDTH = 10; 
	
	public double dampening = 1.0;
	public Pair initAcc = new Pair (0, 200);
	public double delta_X = 0;
	public double delta_Y = 0;
	
	int updateCount = 0;
	public void drawSpring(Sphere _firstBall, Sphere _secondBall, Graphics g) {
		if(updateCount == 0) {
			//initAcc = _firstBall.acceleration;
		}
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine((int)_firstBall.position.x + WIDTH, (int)_firstBall.position.y + WIDTH, (int)_secondBall.position.x + WIDTH, (int)_secondBall.position.y + WIDTH);
		g.drawLine((int)_firstBall.position.x - WIDTH, (int)_firstBall.position.y - WIDTH, (int)_secondBall.position.x - WIDTH, (int)_secondBall.position.y - WIDTH);
		updateCount++;
	}
	public Pair getForce() {
		double fX = (this.delta_X/2) * CONSTANT;
		double fY = (this.delta_Y/2) * CONSTANT;
		return new Pair(fX/dampening, fY/dampening); 
	}
	public void updateSpring(Sphere _firstBall, Sphere _secondBall) {
		this.delta_X = _firstBall.position.x - _secondBall.position.x;
		this.delta_Y = _firstBall.position.x - _secondBall.position.y;
		
		if (this.getSpringLength() > LENGTH) {
			_firstBall.acceleration = initAcc.add(new Pair(-1 * getForce().x/_firstBall.mass, getForce().y/_firstBall.mass));
			_secondBall.acceleration = initAcc.add(new Pair(getForce().x/_firstBall.mass, -1 * getForce().y/_firstBall.mass));
		}
		else if (this.getSpringLength() < LENGTH) {
			_firstBall.acceleration = initAcc.add(new Pair(getForce().x/_firstBall.mass, -1 * getForce().y/_firstBall.mass));
			_secondBall.acceleration = initAcc.add(new Pair(-1 * getForce().x/_firstBall.mass, getForce().y/_firstBall.mass));
		}
		//increase of dampening for the spring
		this.dampening += 0.01;
	}
	public double getSpringLength() {
		return Math.sqrt(this.delta_X * this.delta_X + this.delta_Y * this.delta_Y);
	}
}
class Pair{
	public double x;
	public double y;
    public Pair(double _x, double _y) {
    	this.x = _x;
    	this.y = _y;
    }
    public Pair times(double time) { // used to get area under some function of time
    	double delta_x = this.x * time; 
    	double delta_y = this.y * time;
    	return new Pair(delta_x, delta_y);
    }
    public Pair add(Pair _delta) { // used to add change to initial value
    	double final_x = this.x + _delta.x;
    	double final_y = this.y + _delta.y;
    	return new Pair(final_x, final_y);
    }
    public Pair divide(double _denom) {
    	double reduced_x = this.x / _denom;
    	double reduced_y = this.y / _denom;
    	return new Pair(reduced_x, reduced_y);
    }
    public void flipX() { // used to change the direction of vector over X
    	this.x = (-1) * this.x;
    }
    public void flipY() { // used to change the direction of vector over Y
    	this.y = (-1) * this.y;
    }  
}

class Sphere{
    Pair position;
    Pair velocity;
    Pair acceleration;
    double radius;
    double dampening;
    Color color;
    //adding mass to calculate acceleration for spring
    double mass = 5;
    public Sphere()
    {
        Random rand = new Random(); 
        position = new Pair(500.0, 500.0);
        velocity = new Pair((double)(rand.nextInt(1000) - 500), (double)(rand.nextInt(1000) - 500));
        acceleration = new Pair(0.0, 200.0);
        radius = 25;
        dampening = 1.1;
        color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
    }

    public void update(World w, double time){
        position = position.add(velocity.times(time));
        velocity = velocity.add(acceleration.times(time));
        bounce(w);
    }    

    public void setPosition(Pair p){
        position = p;
    }

    public void setVelocity(Pair v){
        velocity = v;
    }

    public void setAcceleration(Pair a){
        acceleration = a;
    } 

    public void draw(Graphics g){
        Color c = g.getColor();
        
        g.setColor(color);
        g.drawOval((int)(position.x - radius), (int)(position.y - radius), (int)(2*radius), (int)(2*radius));
        g.setColor(c);
    }
    //my code
    public void drawAttached(Graphics g){
    	Color c = g.getColor();
        
        g.setColor(color);
        g.fillOval((int)(position.x - radius), (int)(position.y - radius), (int)(2*radius), (int)(2*radius));
        g.setColor(c);
    }
    private void bounce(World w){
        Boolean bounced = false;
        if (position.x - radius < 0){
            velocity.flipX();
            position.x = radius;
            bounced = true;
        }
        else if (position.x + radius > w.width){
            velocity.flipX();
            position.x = w.width - radius;
            bounced = true;
        }
        if (position.y - radius < 0){
            velocity.flipY();
            position.y = radius;
            bounced = true;
        }
        else if(position.y + radius >  w.height){
            velocity.flipY();
            position.y = w.height - radius;
            bounced = true;
        }
        if (bounced){
            velocity = velocity.divide(dampening);
        }
    } 
}

class World{
    int height;
    int width;
    int numSpheres;
    Sphere spheres[];
    //creating a spring
    public Spring greatSpring = new Spring();

    public World(int initWidth, int initHeight, int initNumSpheres){
        width = initWidth;
        height = initHeight;

        numSpheres = initNumSpheres;
        spheres  = new Sphere[numSpheres];
        
        for (int i = 0; i < numSpheres; i ++)
            {
                spheres[i] = new Sphere();  
            }
    }
    
    public void drawSpheres(Graphics g){
        for (int i = 0; i < numSpheres; i++){
        	//attaching to spheres to each other by a spring
        	if(i == 0 || i == 1) {
        		spheres[i].drawAttached(g);
        		greatSpring.drawSpring(spheres[0], spheres[1], g);
        	}  
        	else {
        		spheres[i].draw(g);	 
        	}
        	          
        }
    }

    public void updateSpheres(double time){
        for (int i = 0; i < numSpheres; i ++) {
        	spheres[i].update(this, time);
        }
        greatSpring.updateSpring(spheres[0], spheres[1]);	
    }

}

public class OOBouncing extends JPanel{
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 768;
    public static final int FPS = 60;
    World world;

    class Runner implements Runnable{
        public void run(){
            while(true){
                world.updateSpheres(1.0 / (double)FPS);
                repaint();
                try{
                    Thread.sleep(1000/FPS);
                }
                catch(InterruptedException e){}
            }
        }    
    }
    
    public OOBouncing(){
        world = new World(WIDTH, HEIGHT, 50);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        Thread mainThread = new Thread(new Runner());
        mainThread.start();
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame("Physics!!!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        OOBouncing mainInstance = new OOBouncing();
        frame.setContentPane(mainInstance);
        frame.pack();
        frame.setVisible(true);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);            
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        world.drawSpheres(g);
    }
}