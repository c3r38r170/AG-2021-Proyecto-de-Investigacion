import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Individuo implements Comparable<Individuo>,Cloneable {
	
	private static final double PROBABILIDAD_DE_MUTACIÓN=.1;
	
	int cantidadDeLineas;
	int cantidadDeBordes;
	double angulo;
	boolean reflexion;
	int iteraciones;

	ArrayList<Integer> cromosoma=new ArrayList<>();
	int longitudDelCromosoma;
	double valorFuncionObjetivo;
	int longitud=0;
	
	public Individuo(ArrayList<Integer> cromosoma,int longitud){
		guardarCromosomaArrayList(cromosoma);
		this.longitud=longitud;
	}

	public Individuo(int cantidadDeLineas, int cantidadDeBordes, double angulo, boolean reflexion, int iteraciones){
		this.cantidadDeLineas = cantidadDeLineas;
		this.cantidadDeBordes = cantidadDeBordes;
		this.angulo = angulo;
		this.reflexion = reflexion;
		this.iteraciones = iteraciones;
	}

	public Individuo(int[] cromosoma,int longitud){
		guardarCromosomaArray(cromosoma);
		this.longitud=longitud;
	}

	public Individuo(ArrayList<Integer> cromosoma){
		guardarCromosomaArrayList(cromosoma);
		calcularLongitud();
	}

	public Individuo(int[] cromosoma){
		guardarCromosomaArray(cromosoma);
		calcularLongitud();
	}

	private void guardarCromosomaArrayList(ArrayList<Integer> cromosoma){
		this.cromosoma =cromosoma;
		longitudDelCromosoma =cromosoma.size();
	}

	private void guardarCromosomaArray(int[] cromosoma){
		for(int i :cromosoma)
			this.cromosoma.add(i);
		longitudDelCromosoma = cromosoma.length;
	}

	private void calcularLongitud(){
		int ultimoDestino=cromosoma.get(cromosoma.size()-1);
		for(int provincia: cromosoma){
			longitud+=App.obtenerDistanciaEntre(ultimoDestino,provincia);
			ultimoDestino=provincia;
		}
	}
	
	public Individuo[] crossover(Individuo pareja){
		if(this.equals(pareja)){
			return new Individuo[]{
				this.crearClon()
				,this.crearClon()
			};
		}

		List<List<Integer>> ciclos=new ArrayList<>();
		// Guardamos una referencia sobre qué índices fueron visitados por ciclos anteriores.
		List<Boolean> indicesVisitados=new ArrayList<> (Collections.nCopies(longitudDelCromosoma, false));

		while(indicesVisitados.contains(false)){
			ciclos.add(new ArrayList<>());
			int cicloActual=ciclos.size()-1;
			int primerIndice=indicesVisitados.indexOf(false)
				,indiceActual=primerIndice;
			do{
				ciclos.get(cicloActual).add(indiceActual);
				indicesVisitados.set(indiceActual, true);
				// El próximo índice será el lugar donde esté en el segundo cromosoma, la ciudad que está en el índice actual en el primer cromosoma.
				indiceActual=pareja.cromosoma.indexOf(this.cromosoma.get(indiceActual));
			}while(indiceActual!=primerIndice);
			// Mientras no se vuelva a la posición inicial, seguir intercalando índices entre ambos cromosomas.
		}
		
    // Crear 2 nuevos cromosomas.
		int[] cromosomaNuevo1=new int[longitudDelCromosoma]
			,cromosomaNuevo2=new int[longitudDelCromosoma];
		
		Individuo p1=this;
		Individuo p2=pareja;
		for(List<Integer> ciclo:ciclos){
			for(Integer i:ciclo){
				cromosomaNuevo1[i]=p1.cromosoma.get(i);
				cromosomaNuevo2[i]=p2.cromosoma.get(i);
			}
			// En cada siguiente ciclo intercambiamos los padres.
			Individuo tmp=p1;
			p1=p2;
			p2=tmp;
		}
		
		return new Individuo[]{
			new Individuo(cromosomaNuevo1)
			,new Individuo(cromosomaNuevo2)
		};
	}
	
	public boolean aplicarMutacion(){
		if(Math.random()<Individuo.PROBABILIDAD_DE_MUTACIÓN){
			int gen1=Utils.randomIntBetween(0, this.longitudDelCromosoma-1);
			int gen2=Utils.randomIntBetween(0, this.longitudDelCromosoma-1);
			
			// Método de Mutación: swapping
			Collections.swap(cromosoma,gen1,gen2);
			
			calcularLongitud();

			return true;
		} else return false;
	}

	public Individuo crearClon(){
		Individuo clon=null;
		try {
			clon = (Individuo)this.clone();
			clon.valorFuncionObjetivo=valorFuncionObjetivo;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clon;
	}

	public String toJSONObject() {
		StringBuilder sb = new StringBuilder("{\"longitud\":"+longitud+",\"recorrido\":[");
		for(int valorGen : cromosoma){
			sb.append(valorGen+",");
		}
		return sb.toString()+"]}";
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("Recorrido de "+longitud+"km:");
		for(int valorGen : cromosoma){
			sb.append("\n\t"+App.PROVINCIAS[valorGen]);
		}
		return sb.toString();
	}
	
	// Este método se usa para ordenar de mejor a peor.
	@Override
	public int compareTo(Individuo otro) {
		double resta=this.longitud-otro.longitud;
		return (int)(resta/Math.abs(resta));
	}

	@Override
	public boolean equals(Object otro){
		//TODO hacer directamente otro=(Individuo)otro;
		Individuo otroComoIndividuo=((Individuo)otro);
		for (int i=0,to=otroComoIndividuo.longitudDelCromosoma;i<to;i++)
			if(otroComoIndividuo.cromosoma.get(i)!=this.cromosoma.get(i))
				return false;
		return true;
	}
}
