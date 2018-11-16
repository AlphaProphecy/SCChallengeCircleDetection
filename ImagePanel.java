import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.util.ArrayList;

public class ImagePanel extends Canvas{
	private static BufferedImage image;
	private static Integer width;
	private static Integer height;
	private Integer[][] arrayx;
	private Integer[][] arrayy;
	private ArrayList<Integer[]> marked;
	
	public static void main(String args[]) {
		ImagePanel i = new ImagePanel();
		JFrame f = new JFrame();
		f.add(i);
		f.setSize(image.getWidth(),image.getHeight());
		
		f.setVisible(true);
	}
	
	public ImagePanel() {
		
		try {
			//image = ImageIO.read(new File(".\\FILE.jpg"));
			image = ImageIO.read(new File(".\\FILE.jpg"));
			width = image.getWidth();
			height = image.getHeight();
			image = sobelOp(greyScale(image));
			image = houghTransform(image);
			
		} catch (Exception e) {
			System.err.println("Exception " + e);
		}
	}
	
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, this);
	}
	
	private BufferedImage greyScale (BufferedImage br) {
		for (int i = 0; i < height; i++) {
			for (int ii = 0; ii < width; ii++) {
				float r = new Color(br.getRGB(ii, i)).getRed();
				float g = new Color(br.getRGB(ii, i)).getGreen();
				float b = new Color(br.getRGB(ii, i)).getBlue();
				int gray = (int)(r+g+b)/3;
				br.setRGB(ii,i, new Color(gray,gray,gray).getRGB());
			}
		}
		return br;
	}
	
	
	private BufferedImage sobelOp (BufferedImage br) {
		marked = new ArrayList<Integer[]>();
		BufferedImage out = new BufferedImage(width, height, 1);
		Integer[][] arrayPoints = new Integer[height][width];
		arrayx = new Integer[height][width];
		arrayy = new Integer[height][width];
		Integer max = 0;
		for (int i = 0; i < height; i++) {
			for (int ii = 0; ii < width; ii++) {
				Integer x = 0;
				x += arrayOutProt(br, i-1,ii-1);
				x -= arrayOutProt(br, i-1,ii+1);
				x += 2*arrayOutProt(br, i,ii-1);
				x -= 2*arrayOutProt(br, i,ii+1);
				x += arrayOutProt(br, i+1,ii-1);
				x -= arrayOutProt(br, i+1,ii+1);
				//System.out.println(x);
				
				Integer y = 0;
				y += arrayOutProt(br, i-1,ii-1);
				y += 2*arrayOutProt(br, i-1,ii);
				y += arrayOutProt(br, i-1,ii+1);
				y -= arrayOutProt(br, i+1,ii-1);
				y -= 2*arrayOutProt(br, i+1,ii);
				y -= arrayOutProt(br, i+1,ii+1);
				
				//System.out.println(y);
				
				Integer col = (int) Math.sqrt(x*x + y*y);
				//System.out.println(col);
				arrayx[i][ii] = x;
				arrayy[i][ii] = y;
				arrayPoints[i][ii] = col;
				if (max < col) {
					max = col;
				}
				//array[i][ii] = 
			}
		}
		
		
		for (int i = 0; i < height; i++) {
			for (int ii = 0; ii < width; ii++) {
				Integer col = (arrayPoints[i][ii]* 255/max );
				//System.out.println(col);
				if (col > 20) {
					out.setRGB(ii, i, new Color(col,col,col).getRGB());
					marked.add(new Integer[] {i,ii});
				}
			}
		}
		
		return out;
	}
	
		
	
	private Integer arrayOutProt (BufferedImage br, int i, int ii){
		//System.out.println("x:" +i + " y"+ii);
		
		try {
			return new Color(br.getRGB(ii,i)).getRed();
		} catch (Exception e) {
			return 0;
		}
	}
	
	private BufferedImage houghTransform(BufferedImage br) {
		BufferedImage out = br;
		int[][][] houghSpace = new int[(int)width][width][height];
		System.out.println((int)width + " " +(width) +" " +(height));
		
		Integer max = 0;
		int mr =0;
		Integer mx = 0;
		Integer my = 0;
		for (Integer[] m: marked) {
			for (int i = -width/2; i < width/2; i++) { // radius
				//System.out.println(i);
				//System.out.println(arrayx[m[0]][m[1]] + " " + arrayy[m[0]][m[1]]);
				if (Math.abs(i) > 5) { // Filtering of tiny circles
					double angle;
					if (!arrayy[m[0]][m[1]].equals(0)) { // Removes / 0
						angle = Math.atan(arrayx[m[0]][m[1]]/arrayy[m[0]][m[1]]);
					} else {
						angle = Math.PI/2d;
					}
					
					for (double a = angle-5; a < angle+5.5; a += 0.5) { // Localised to a line instead of the whole circle
						
						int x = (int) (i * Math.cos(a)) + m[1];
						int y = (int) (i * Math.sin(a)) + m[0];
						if (x >= 0 && x < width-1 && y >=0 && y <height-1) {
							//System.out.println(x + " " + y + " " + ((int)(i+width/2)));
							
							houghSpace[(int)(i+width/2)][x][y] += 1;
							if (houghSpace[(int)(i+width/2)][x][y] > max) {
								max = houghSpace[(int)(i+width/2)][x][y];
								mr = i;
								mx = x;
								my = y;
							}
						}
					}
				}
			}
		}
		
		System.out.println(mr + " " + mx +" " + my );
		
		
		for (int a = 0; a < 360; a++) {
			out.setRGB((int)(mr*Math.cos(a)) + mx, (int)(mr*Math.sin(a)) + my, new Color(255,0,0).getRGB());
		}
		
		// Code to print a 2d representation of the ignoring the radius Houghspace
		/*System.out.println("P1");
		max = 0;
		
		int[][] hough = new int[width][height];
		
		for (int ii = 0; ii < width; ii++) {
			for (int iii = 0; iii < height; iii++) {
				for(int i = 0; i < (int)width/2; i++) {
					hough[ii][iii] += houghSpace[i][ii][iii];
				}
				if (hough[ii][iii] > max) {
					max = hough[ii][iii];
				}
			}
		}
		
		System.out.println(max);
		System.out.println("P2");
		
		System.out.println(hough[0][0]);
		
		for (int i = 0; i < width-1; i++) {
			for (int ii = 0; ii < height-1; ii++) {
				
				Integer col = (hough[i][ii]*255)/max;
			if (!col.equals(0)) {
				//System.out.println(col);
			}
				if (((float) hough[i][ii]) /(float) max > 0.65f) {
					out.setRGB(i, ii, new Color(col,col,col).getRGB());
				}
			}
		}*/
		
		return out;
	}
}
