package homecare;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

import javax.swing.*;

public class HomeCarePanel {
	private static HomeCarePanel homeCarePanel;
	private static JPanel menu;
	private static JPanel graph;
	private static JSplitPane jSplitPane;
	private static JFrame window;
	
	public HomeCarePanel(){
		menu = new JPanel();
		graph = new JPanel();
		jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                menu, graph);
		jSplitPane.setDividerLocation(100);
        jSplitPane.setEnabled(false);
        window = new JFrame("HomeCare - UFPel");
        window.add(jSplitPane);
        //window.setExtendedState(JFrame.MAXIMIZED_BOTH); //Iniciar maximizado da maneira correta!
        window.setMinimumSize(new Dimension(800, 600));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        telaMenu();
        telaGraph();
    }
	
	private void telaMenu(){
		menu.setLayout(new GridLayout(12, 0, 5, 5));
		JButton paciente = new JButton("Paciente");
		menu.add(paciente);
	}
	
	private void telaGraph(){
		GraphingData gpCard = new GraphingData("card");
		gpCard.setBorder(BorderFactory.createTitledBorder("Batimentos Cardiáco"));
		
		GraphingData gpTemp = new GraphingData("temp");
		gpTemp.setBorder(BorderFactory.createTitledBorder("Monitor de Temperatura"));
		
		GraphingData gpPress = new GraphingData("press");
		gpPress.setBorder(BorderFactory.createTitledBorder("Pressão Arterial"));
		
		graph.setLayout(new GridLayout(3, 0, 5, 5));
		graph.add(gpCard);
		graph.add(gpTemp);
		graph.add(gpPress);
		
		int[] upData = { 10, 0, 31, 13, 6, 8, 4, 7, 54, 17, 21, 0, 21, 03, 86, 88, 74, 87, 54};
		int[] data = new int[20];
		int last = 0;
		for(int i=0; i<20; i++){
			gpCard.setDatas(data);
			gpCard.updateUI();
			
			try {
				Thread.sleep(1000);
				
				for(int l = 0; l < i; l++){
					data[20-i+l] = upData[l]; 
				}
				
				
				for(int j = 0; j<20; j++){
					System.out.print(data[j]+ " ");
				}
				System.out.println(" ");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		
		graph.updateUI();
	}
	
	
	private class GraphingData extends JPanel{
		private String label;
		private Color cor;
		private int[] data = new int[20];
		private final int PAD = 20;

		public GraphingData(String tipo) {
			if(tipo.equals("card")){
				this.cor = Color.red.darker();
				label = "batimento médio: ";
			}
			if(tipo.equals("temp")){
				this.cor = Color.blue.darker();
				label = "temperatura média: ";
			}
			if(tipo.equals("press")){
				this.cor = Color.green.darker();
				label = "pressão média: ";
			}
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth();
	        int h = (int) (getHeight()*0.85);
			// Draw ordinate.
			g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));
			// Draw abcissa.
			g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));
			// Draw labels.
			Font font = g2.getFont();
			FontRenderContext frc = g2.getFontRenderContext();
			LineMetrics lm = font.getLineMetrics("0", frc);
			float sh = lm.getAscent() + lm.getDescent();
			
			// Ordinate label.
			String s = label;
			float sy = h - PAD + (PAD - sh)  + lm.getAscent() + 10;
			float sw = (float) font.getStringBounds(s, frc).getWidth();
			float sx = (w - sw) / 2;
			g2.drawString(s, sx, sy);
			
			
			// Draw lines.
			double xInc = (double) (w - 2 * PAD) / (data.length - 1);
			double scale = (double) (h - 2 * PAD) / getMax();
			g2.setPaint(cor);
			for (int i = 0; i < data.length - 1; i++) {
				double x1 = PAD + i * xInc;
				double y1 = h - PAD - scale * data[i];
				double x2 = PAD + (i + 1) * xInc;
				double y2 = h - PAD - scale * data[i + 1];
				g2.draw(new Line2D.Double(x1, y1, x2, y2));
			}
		}

		private int getMax() {
			int max = -Integer.MAX_VALUE;
			for (int i = 0; i < data.length; i++) {
				if (data[i] > max)
					max = data[i];
			}
			return max;
		}
		
		public void setDatas(int[] data){
			this.data = data;
		}

	}
	

	public static void main(String[] args) {
		WindowUtilities.setNativeLookAndFeel();
		homeCarePanel = new HomeCarePanel();
	}
}
