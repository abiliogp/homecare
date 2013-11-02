package homecare;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class HomeCare {

	private Paciente paciente;
	
	private Sensor sensor;
	private CopyOnWriteArrayList<Dado> dadosLidos = new CopyOnWriteArrayList<Dado>();
	private double[] temp, sist, dias, pulse;
	
	public HomeCare (){
		paciente = new Paciente(" June Carter", "female", "23/06/1929",
				"250.300.100-88","73");
		temp = new double[20];
		sist = new double[20];
		dias = new double[20];
		pulse = new double[20];
		sensor = new Sensor();
		new Thread(sensor).start();
	}
	
	public String getCpf(){
		return paciente.getCpf();
	}
	
	public Paciente getPaciente(){
		return this.paciente;
	}
	
	public CopyOnWriteArrayList<Dado> getDados(){
		if(!dadosLidos.isEmpty()){
			dadosLidos.clear();
		}
		lerSensores();
		return dadosLidos;
	}
	
	private void lerSensores(){
		try {
			Thread.sleep(1000000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		temp = sensor.getTemperatura();
		sist = sensor.getSistolica();
		dias = sensor.getDiastolica();
		pulse = sensor.getPulso();
		for(int i = 0; i < 20; i++){
			dadosLidos.add(new Dado(temp[i],"temp"));
			dadosLidos.add(new Dado(sist[i],"press"));
			dadosLidos.add(new Dado(dias[i],"presd"));
			dadosLidos.add(new Dado(pulse[i],"card"));
		}
	}
	
	private void lerSensoresRandom(){
		Random rad = new Random();
		for(int i = 0; i < 20; i++){
			dadosLidos.add(new Dado(10.5 + rad.nextDouble() * 2,"temp"));
			dadosLidos.add(new Dado(12.5 +  rad.nextDouble() * 2,"press"));
			dadosLidos.add(new Dado(6.7 + rad.nextDouble() * 3,"presd"));
			dadosLidos.add(new Dado(80.20 + rad.nextDouble() * 10,"card"));
		}
	}
	
	
	
	

}
