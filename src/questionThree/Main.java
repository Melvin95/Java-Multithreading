package questionThree;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame{
	
	public Main(){
		setLayout(new BorderLayout());
		Harbor harbor = new Harbor();
		add(harbor);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	  	JFrame frame = new Main();
	  	frame.setTitle("Durban Harbor");
	  	frame.setSize(1024, 800);
	    frame.setLocationRelativeTo(null); // Center the frame
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
	    frame.setVisible(true);
	    
	}

}
