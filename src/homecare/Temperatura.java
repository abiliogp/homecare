package homecare;

public class Temperatura extends Sensor {

	@Override
	public void receive() {
		this.dado = new Dado(19.0, "c");
	}

	private void dadosArqui(){
		
	}
	

	
}
