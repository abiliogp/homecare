package homecare;

public abstract class Sensor {

	protected Dado dado;
	
	public Dado getDado(){
		receive();
		return dado;
	}
	
	/*
	 * Dados coletados...
	 * criar atributos para os dados
	 */
	protected abstract void receive();
	
	/*
	 * Envia dados para o HomeCare
	 */
	public void send() {
		
	}
}
