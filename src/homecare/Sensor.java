
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

public class Sensor implements SerialPortEventListener, Runnable {

	private double[] temperaturas;
	private double[] sistolica;
	private double[] diastolica;
	private double[] pulso;
	private boolean disponivel = false;

	
	SerialPort serialPort;
	/** The port we're normally going to use. */

	private static final String PORT_NAMES[] = { "/dev/tty.usbserial-A9007UX1", // Mac
																				// OS
																				// X
			"/dev/ttyS8", // Linux
			"COM35", // Windows
	};

	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;

	public Sensor() {
		temperaturas = new double[20];
		sistolica = new double[20];
		diastolica = new double[20];
		pulso = new double[20];
	}

	public void initialize() {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			for (String portName : PORT_NAMES) {
				System.out.println(portName);
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(
					serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine = null;
				String str, value;
				double data = 0;
				int t = 0, s = 0 , d = 0 , p = 0;
				if (input.ready()) {
					for(int i = 0; i < 80; i++){
					inputLine = input.readLine();
					str = manipulate(inputLine, "st", ":", "<");
					if (str.equals("t")) {
						value = manipulate(inputLine, "t", "<", ">");
						data = Double.parseDouble(value);
						if(data != 0){
							if(t < 20){
								this.temperaturas[t] = data;
								t++;
							}
						}
					} else if (str.equals("s")) {
						value = manipulate(inputLine, "s", "<", ">");
						data = Double.parseDouble(value);
						if(data != 0){
							if(s < 20){
								this.sistolica[s] = data/10;
								s++;
							}
						} 
					} else if (str.equals("d")) {
						value = manipulate(inputLine, "d", "<", ">");
						data = Double.parseDouble(value);
						if(data != 0){
							if(d < 20){
								this.diastolica[d] = data/10;
								d++;
							}
						}
					} else if (str.equals("p")) {
						value = manipulate(inputLine, "p", "<", ">");
						data = Double.parseDouble(value);
						if(data != 0){
							if(p < 20){
								this.pulso[p] = data;
								p++;
							}
						}
					}
					System.out.println("parou no " + i);
					}
					this.disponivel = true;
				}

			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}
	
	public boolean isDisponivel(){
		return this.disponivel;
	}

	public double[] getTemperatura() {
		return temperaturas;
	}

	public double[] getSistolica() {
		return sistolica;
	}

	public double[] getDiastolica() {
		return diastolica;
	}

	public double[] getPulso() {
		return pulso;
	}

	public static void main(String[] args) throws Exception {
		Sensor main = new Sensor();
		main.initialize();
		Thread t = new Thread() {
			public void run() {
				// the following line will keep this app alive for 1000 seconds,
				// waiting for events to occur and responding to them (printing
				// incoming messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
				}
			}
		};
		t.start();
		System.out.println("Started");
	}

	
	public void run() {
		this.initialize();
		
		Thread t = new Thread() {
			public void run() {
				// the following line will keep this app alive for 1000 seconds,
				// waiting for events to occur and responding to them (printing
				// incoming messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
				}
			}
		};
		t.start();
		System.out.println("Started");

	}

	private String manipulate(String str, String key, String first, String last) {
		int n = str.indexOf(key);
		if (n == -1) {
			return "";
		}
		int x = str.indexOf(first, n);
		int x1 = str.indexOf(last, x + 1);
		return str.substring(x + 1, x1);
	}

}
