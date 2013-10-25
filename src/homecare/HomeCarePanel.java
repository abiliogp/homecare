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
		jSplitPane.setDividerLocation(250);
        jSplitPane.setEnabled(false);
        window = new JFrame("HomeCare - UFPel");
        window.add(jSplitPane);
        //window.setExtendedState(JFrame.MAXIMIZED_BOTH); //Iniciar maximizado da maneira correta!
        window.setMinimumSize(new Dimension(1000, 600));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.setLocationRelativeTo(null);
        telaMenu();
        telaGraph();
    }
	
	private void telaMenu(){
		menu.setLayout(new GridLayout(14, 0, 5, 5));
		
		JComboBox paciente = new JComboBox();  
        // Incluindo item no combobox tipo de animal  
        paciente.addItem("Paciente 1");
        paciente.addItem("Paciente 2");  
        
		menu.add(paciente);
		
		menu.add(new JLabel(" Paciente: "));
		menu.add(new JLabel(" Idade: "));

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
		
		//leitura dos dados
		int[] upDataCard = { 10, 0, 31, 13, 6, 8, 4, 7, 54, 17, 21, 0, 21, 03, 86, 88, 74, 87, 54};
		int[] upDataTemp = { 34, 35, 34, 35,34, 33, 35, 36, 34, 38, 39, 40, 39, 38, 40, 38, 40, 39, 38};

		int[] upDataPress = { 14, 15, 14, 15,14, 13, 15, 16, 14, 14, 15, 16, 14, 15, 14, 15, 16, 17, 16};
		int[] upDataPress2 = { 8, 8, 7, 8,7, 9,8, 7, 8, 8, 7, 8, 8, 8, 8, 9, 8, 7, 8};
		
		int[] dataCard = new int[20];
		int[] dataTemp = new int[20];
		
		int[] dataPress = new int[20];
		int[] dataPress2 = new int[20];
		
		for(int i=0; i<20; i++){
			gpCard.setDatas(dataCard);
			gpCard.updateUI();
			
			gpTemp.setDatas(dataTemp);
			gpTemp.updateUI();
			
			gpPress.setDatas(dataPress, dataPress2);
			gpPress.updateUI();
			
			try {
				Thread.sleep(1000);
				for(int l = 0; l < i; l++){
					dataCard[20-i+l] = upDataCard[l];
					dataTemp[20-i+l] = upDataTemp[l];
					
					dataPress[20-i+l] = upDataPress[l];
					dataPress2[20-i+l] = upDataPress2[l];
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}  
		}
		
		graph.updateUI();
	}
	
	
	private class GraphingData extends JPanel{
		private String label;
		private boolean press;
		private Color cor, cor2;
		private int[] data = new int[20];
		private int[] data2 = new int[20];
		private final int PAD = 20;

		public GraphingData(String tipo) {
			if(tipo.equals("card")){
				this.cor = Color.red.darker();
				label = "Batimento atual: ";
			}
			if(tipo.equals("temp")){
				this.cor = Color.blue.darker();
				label = "Temperatura atual: ";
			}
			if(tipo.equals("press")){
				this.cor2 = Color.green.darker();
				this.cor = Color.pink.darker();
				label = "Pressão Sistólica atual: ";
				this.press = true;
			}
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth();
			int h = (int) (getHeight()*0.85);
			
			if(this.press){
				h = (int) (getHeight()*0.75);
			} 
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
			int max = getMax(data);
			int min = getMin(data);
			int value = data[data.length-1];
			float ave = getAverage(data);
			s = s + value + " | máximo: " + max + " | mínimo: " + min + " | média: " + ave;
			float sy = h - PAD + (PAD - sh)  + lm.getAscent() + 10;
			float sw = (float) font.getStringBounds(s, frc).getWidth();
			float sx = (w - sw) / 2;
			g2.drawString(s, sx, sy);
			
			if(this.press){
				s = "              Diastólica atual: ";
				int max2 = getMax(data2);
				int min2 = getMin(data2);
				int value2 = data2[data2.length-1];
				float ave2 = getAverage(data2);
				s = s + value2 + " | máximo: " + max2 + " | mínimo: " + min2 + " | média: " + ave2;
				float sy2 = h - PAD + (PAD - sh)  + lm.getAscent() + 30;
				float sw2 = (float) font.getStringBounds(s, frc).getWidth();
				float sx2 = (w - sw) / 2;
				g2.drawString(s, sx2, sy2);	
			}
			
			
			// Draw lines.
			double xInc = (double) (w - 2 * PAD) / (data.length - 1);
			double scale = (double) (h - 2 * PAD) / max;
			for (int i = 0; i < data.length - 1; i++) {
				g2.setPaint(cor);
				double x1 = PAD + i * xInc;
				double y1 = h - PAD - scale * data[i];
				double x2 = PAD + (i + 1) * xInc;
				double y2 = h - PAD - scale * data[i + 1];
				g2.draw(new Line2D.Double(x1, y1, x2, y2));
				if(this.press){
					g2.setPaint(cor2);
					double x12 = PAD + i * xInc;
					double y12 = h - PAD - scale * data2[i];
					double x22 = PAD + (i + 1) * xInc;
					double y22 = h - PAD - scale * data2[i + 1];
					g2.draw(new Line2D.Double(x12, y12, x22, y22));
				}
			}
		}

		private int getMax(int[] data) {
			int max = -Integer.MAX_VALUE;
			for (int i = 0; i < data.length; i++) {
				if (data[i] > max)
					max = data[i];
			}
			return max;
		}
		
		private int getMin(int[] data) {
			int min = 0;
			for (int i = 0; i < data.length; i++) {
				if (data[i] < min)
					min = data[i];
			}
			return min;
		}
		
		private float getAverage(int[] data){
			int ave = 0;
			for (int i = 0; i < data.length; i++) {
				ave += data[i];
			}
			return ave/data.length;
		}
		
		public void setDatas(int[] data){
			this.data = data;
		}

		public void setDatas(int[] data, int[] data2){
			this.data = data;
			this.data2 = data2;
		}
	}
	

	public static void main(String[] args) {
		WindowUtilities.setNativeLookAndFeel();
		homeCarePanel = new HomeCarePanel();
	}
}
