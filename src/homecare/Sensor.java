package homecare;

public abstract class Sensor {

	protected Dado dado;
	
	public Dado getDado(){
		return dado;
	}
	
	/*
	 * Dados coletados...
	 * criar atributos para os dados
	 */
	public abstract void receive();
	
	/*
	 * Envia dados para o HomeCare
	 */
	public void send() {
		
	}
}
