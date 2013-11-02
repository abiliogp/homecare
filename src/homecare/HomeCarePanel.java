package homecare;

import homecare.Coleta.ClientConnection;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.*;
import java.awt.geom.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;

public class HomeCarePanel {

	private static HomeCarePanel homeCarePanel;
	private static JPanel menu;
	private static JPanel graph;
	private static JSplitPane jSplitPane;
	private static JFrame window;
	private JComboBox pacienteBox;
	private Thread graphThread;

	private Coleta coleta;
	private TreeMap<String, Paciente> triePaciente = new TreeMap<String, Paciente>();
	private CopyOnWriteArrayList<Dado> dados = new CopyOnWriteArrayList<Dado>();

	private String cpfHomeCare;
	private String cpfSelected;
	private Paciente pac;

	private static double[] upDataCard = new double[20];
	private static double[] upDataTemp = new double[20];
	private static double[] upDataPress = new double[20];
	private static double[] upDataPress2 = new double[20];

	private enum Msg {
		temp, press, presd, card;
	}

	public HomeCarePanel() throws IOException {
		coleta = new Coleta("192.168.25.9");
		Socket client = new Socket("192.168.25.155", 12345);
		coleta.myServers.add(client);
		new Thread(coleta).start();

		this.cpfHomeCare = coleta.getHomeCare().getCpf();
		this.cpfSelected = this.cpfHomeCare;
		Paciente john = new Paciente(" Johnny Cash", "male", "26/02/1932",
				"111.222.333-00", "71");

		this.triePaciente.put(cpfHomeCare, coleta.getHomeCare().getPaciente());
		this.triePaciente.put("111.222.333-00", john);

		menu = new JPanel();
		graph = new JPanel();
		jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menu, graph);
		jSplitPane.setDividerLocation(250);
		jSplitPane.setEnabled(false);
		window = new JFrame("HomeCare - UFPel");
		window.add(jSplitPane);
		// window.setExtendedState(JFrame.MAXIMIZED_BOTH); //Iniciar maximizado
		// da maneira correta!
		window.setMinimumSize(new Dimension(1000, 600));
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setLocationRelativeTo(null);
		telaMenu();
		telaGraph(false);

	}

	private void telaMenu() {
		menu.setLayout(new GridLayout(14, 0, 5, 5));

		pacienteBox = new JComboBox();
		pacienteBox.addItem(" select the patient");
		for (String str : this.triePaciente.keySet()) {
			pacienteBox.addItem(str);
		}
		menu.add(pacienteBox);

		pacienteBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nomes();
			}
		});

	}

	private void nomes() {
		String cpfCombo = (String) this.pacienteBox.getSelectedItem();
		if (!coleta.getTrieDatas().containsKey(cpfCombo)) {
			JOptionPane.showMessageDialog(window,
							"Sorry... this patient doesn't have access \n Check the connection!",
							"Check the connection", JOptionPane.WARNING_MESSAGE);
			pac = this.triePaciente.get(this.cpfHomeCare);
			pacienteBox.setSelectedItem(this.cpfHomeCare);
			pacienteBox.updateUI();
		} else {
			pac = this.triePaciente.get(cpfCombo);
		}
		menu.removeAll();
		menu.add(pacienteBox);
		menu.add(new JLabel(" Patient: " + pac.getNome()));
		menu.add(new JLabel(" gender: " + pac.getGenero()));
		menu.add(new JLabel(" age: " + pac.getIdade()));
		menu.add(new JLabel(" birth date: " + pac.getDataNasc()));
		if (cpfCombo.equals(cpfHomeCare)) {
			menu.add(new JLabel(" HCM: local "));
		} else {
			menu.add(new JLabel(" HCM: distributed "));
		}
		this.cpfSelected = pac.getCpf();
		telaGraph(true);
		graph.updateUI();
		menu.updateUI();
	}

	private void telaGraph(boolean update) {

		if (update) {
			graph.removeAll();
			graphThread = null;
		}
		TelaGraph telaGraph = new TelaGraph();
		graphThread = new Thread(telaGraph, "telaGraph");
		graphThread.start();
		graph.updateUI();
	}

	private class TelaGraph implements Runnable {

		public void run() {
			GraphingData gpCard = new GraphingData("card");
			gpCard.setBorder(BorderFactory.createTitledBorder("Heart Rate"));

			GraphingData gpTemp = new GraphingData("temp");
			gpTemp.setBorder(BorderFactory.createTitledBorder("Temperature"));

			GraphingData gpPress = new GraphingData("press");
			gpPress.setBorder(BorderFactory
					.createTitledBorder("Blood Pressure"));

			graph.setLayout(new GridLayout(3, 0, 5, 5));
			graph.add(gpCard);
			graph.add(gpTemp);
			graph.add(gpPress);

			double[] dataCard = new double[20];
			double[] dataTemp = new double[20];

			double[] dataPress = new double[20];
			double[] dataPress2 = new double[20];
			// leitura dos dados
			Thread myThread = Thread.currentThread();

			while (myThread == graphThread) {
				dados = (CopyOnWriteArrayList<Dado>) Coleta
						.getLastDatasOfCpf(cpfSelected);
				if (dados == null) {
					return;
				}
				separaDados();

				for (int i = 0; i < 20; i++) {

					gpCard.setDatas(dataCard);
					gpCard.updateUI();

					gpTemp.setDatas(dataTemp);
					gpTemp.updateUI();

					gpPress.setDatas(dataPress, dataPress2);
					gpPress.updateUI();

					try {
						Thread.sleep(1000);
						for (int l = 0; l < i; l++) {
							dataCard[20 - i + l] = upDataCard[l];
							dataTemp[20 - i + l] = upDataTemp[l];
							dataPress[20 - i + l] = upDataPress[l];
							dataPress2[20 - i + l] = upDataPress2[l];
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				dataCard = upDataCard;
				dataTemp = upDataTemp;
				dataPress = upDataPress;
				dataPress2 = upDataPress2;
			}

		}
	}

	private void separaDados() {
		String tipo;

		CopyOnWriteArrayList<Dado> listDados = new CopyOnWriteArrayList<Dado>(
				dados);

		int contTemp = 0, contPress = 0, contPress2 = 0, contCard = 0;
		for (Dado dado : listDados) {
			tipo = dado.getTipo();
			switch (Msg.valueOf(tipo)) {
			case temp:
				upDataTemp[contTemp] = dado.getValor();
				contTemp++;
				break;
			case press:
				upDataPress[contPress] = dado.getValor();
				contPress++;
				break;
			case presd:
				upDataPress2[contPress2] = dado.getValor();
				contPress2++;
				break;
			case card:
				upDataCard[contCard] = dado.getValor();
				contCard++;
				break;
			}
		}
		// this.dados.clear();
	}

	private class GraphingData extends JPanel {
		private String label;
		private boolean press;
		private Color cor, cor2;
		private double[] data = new double[20];
		private double[] data2 = new double[20];
		private final int PAD = 20;
		private double med;

		public GraphingData(String tipo) {
			if (tipo.equals("card")) {
				this.cor = Color.red.darker();
				label = "Pulse current: ";
				this.med = 180;
			}
			if (tipo.equals("temp")) {
				this.cor = Color.blue.darker();
				label = "Temperature current: ";
				this.med = 60;
			}
			if (tipo.equals("press")) {
				this.cor2 = Color.green.darker();
				this.cor = Color.pink.darker();
				label = "Pressure Systolic current: ";
				this.press = true;
				this.med = 23;
			}
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth();
			int h = (int) (getHeight() * 0.85);

			if (this.press) {
				h = (int) (getHeight() * 0.75);
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
			double max = getMax(data);
			double min = getMin(data);
			double value = data[data.length - 1];
			double ave = getAverage(data);
			s = s + String.format("%.2f", value) + " | maximum: "
					+ String.format("%.2f", max) + " | minimum: "
					+ String.format("%.2f", min) + " | average: "
					+ String.format("%.2f", ave);

			float sy = h - PAD + (PAD - sh) + lm.getAscent() + 10;
			float sw = (float) font.getStringBounds(s, frc).getWidth();
			float sx = (w - sw) / 2;
			g2.drawString(s, sx, sy);

			if (this.press) {
				s = "              Diastolic current: ";
				double max2 = getMax(data2);
				double min2 = getMin(data2);
				double value2 = data2[data2.length - 1];
				double ave2 = getAverage(data2);
				s = s + String.format("%.2f", value2) + " | maximum: "
						+ String.format("%.2f", max2) + " | minimum: "
						+ String.format("%.2f", min2) + " | average: "
						+ String.format("%.2f", ave2);

				float sy2 = h - PAD + (PAD - sh) + lm.getAscent() + 30;
				float sw2 = (float) font.getStringBounds(s, frc).getWidth();
				float sx2 = (w - sw) / 2;
				g2.drawString(s, sx2, sy2);
			}

			double xInc = (double) (w - 2 * PAD) / (data.length - 1);
			double scale = (double) (h - 2 * PAD) / this.med;
			for (int i = 0; i < data.length - 1; i++) {
				g2.setPaint(cor);
				double x1 = PAD + i * xInc;
				double y1 = h - PAD - scale * data[i];
				double x2 = PAD + (i + 1) * xInc;
				double y2 = h - PAD - scale * data[i + 1];
				g2.draw(new Line2D.Double(x1, y1, x2, y2));
				if (this.press) {
					g2.setPaint(cor2);
					double x12 = PAD + i * xInc;
					double y12 = h - PAD - scale * data2[i];
					double x22 = PAD + (i + 1) * xInc;
					double y22 = h - PAD - scale * data2[i + 1];
					g2.draw(new Line2D.Double(x12, y12, x22, y22));
				}
			}
		}

		private double getMax(double[] data) {
			double max = -Integer.MAX_VALUE;
			for (int i = 0; i < data.length; i++) {
				if (data[i] > max)
					max = data[i];
			}
			return max;
		}

		private double getMin(double[] data) {
			double min = data[0];
			for (int i = 0; i < data.length; i++) {
				if (data[i] < min)
					min = data[i];
			}
			return min;
		}

		private double getAverage(double[] data) {
			double ave = 0;
			for (int i = 0; i < data.length; i++) {
				ave += data[i];
			}
			return ave / data.length;
		}

		public void setDatas(double[] data) {
			this.data = data;
		}

		public void setDatas(double[] data, double[] data2) {
			this.data = data;
			this.data2 = data2;
		}
	}

	public static void main(String[] args) throws IOException {
		WindowUtilities.setNativeLookAndFeel();
		homeCarePanel = new HomeCarePanel();
	}
}
