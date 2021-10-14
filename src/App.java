import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.DoubleStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class App extends JFrame {
	
	private static final long serialVersionUID = 1L;

	private boolean elitismo=false;
	
	private Individuo[] poblacionActual;
	private double[] vectorFitness;
	
	private double sumatoriaPuntuaciones=0;
	
	private int tamañoPoblacion=0;
	
	private WebEngine webEngine;

	public static final String[] PROVINCIAS={
		"Buenos Aires - Cdad. de Bs. As."
		,"Córdoba - Córdoba"
		,"Corrientes - Corrientes"
		,"Formosa - Formosa"
		,"Buenos Aires - La Plata"
		,"La Rioja - La Rioja"
		,"Mendoza - Mendoza"
		,"Neuquén - Neuquén"
		,"Entre Ríos - Paraná"
		,"Misiones - Posadas"
		,"Chubut - Rawson"
		,"Chaco - Resistencia"
		,"Santa Cruz - Río Gallegos"
		,"Catamarca - San Fernando del Valle de Catamarca"
		,"Tucumán - San Miguel de Tucumán"
		,"Jujuy - San Salvador de Jujuy"
		,"Salta - Salta"
		,"San Juan - San Juan"
		,"San Luis - San Luis"
		,"Santa Fe - Santa Fe"
		,"La Pampa - Santa Rosa"
		,"Santiago del Estero - Santiago del Estero"
		,"Tierra del Fuego, Antártida e Islas del Atlántico Sur - Ushuaia"
		,"Río Negro - Viedma"
	};
	public static final Integer[][] DISTANCIAS_RAW ={
			/*23	22	21... */
		/*1*/{799,2373,939,579,393,749,1005,1282,1334,1080,979,2082,794,1127,834,375,989,985,986,53,933,792,646}
		/*2*/,{1047,2618,401,577,330,293,412,745,809,517,362,2281,669,1321,919,348,907,466,340,698,824,677}
		/*3*/,{1527,3131,535,1136,498,969,1039,719,742,633,691,2819,13,1845,291,500,1534,1131,814,830,157}
		,{1681,3284,629,1293,654,1117,1169,741,750,703,793,2974,161,1999,263,656,1690,1269,927,968}
		,{789,2350,991,602,444,795,1053,1333,1385,1132,1030,2064,833,1116,857,427,1005,1029,1038}
		,{1311,2821,311,834,640,435,283,533,600,330,149,2473,802,1548,1098,659,1063,427}
		,{1019,2435,713,586,775,235,152,957,1023,756,569,2081,1121,1201,1384,790,676}
		,{479,1762,1286,422,1049,643,824,1591,1658,1370,1182,1410,1529,543,1709,1053}
		,{1030,2635,566,642,19,574,757,906,959,707,622,2320,498,1345,658}
		,{1624,3207,827,1293,664,1200,1306,992,1007,924,980,2914,305,1951}
		,{327,1300,1721,745,1349,1113,1340,2054,2120,1827,1647,975,1843}
		,{1526,3130,523,1132,495,961,1029,706,729,620,678,2818}
		,{1294,359,2677,1712,2325,2046,2231,2997,3063,2773,2587}
		,{1391,2931,166,915,602,540,430,410,477,189}
		,{1562,3116,141,1088,689,727,612,228,293}
		,{1855,3408,414,1382,942,1017,874,67}
		,{1790,3341,353,1316,889,950,808}
		,{1141,2585,583,686,740,284}
		,{882,2392,643,412,560}
		,{1035,2641,547,641}
		,{477,2044,977}
		,{1446,3016}
		,{1605}
	};
	public static HashMap<Set<Integer>,Integer> distancias = new HashMap<>();

	public static int obtenerDistanciaEntre(int a,int b){
		if(a==b)
			return 0;
		HashSet<Integer> set = new HashSet<>();
		set.add(a);
		set.add(b);
		return distancias.get(set);
	}

  // Función que sirve para evaluar el desempeño de cada individuo.
	private static double objetivo(Individuo individuo){
		// El inverso de la cantidad de miles de kilómetros.
		// Los recorridos tienen probabilidades relativas proporcionales.
		// (Si un recorrido es el doble de largo que el otro, tendrá la mitad del fitness.)
		return 1000.0/individuo.longitud;
	}
	
	public static void main(String[] args) throws Exception {
		// Ponemos las distancias en un mapa de conjuntos a enteros, donde los conjuntos son cada par de ciudades.
		// Usar conjuntos nos permite evitar cargar 2 veces las distancias e ignorar la dirección del viaje.
		int n=PROVINCIAS.length-1;
		for(int i=0;i<n;i++){
			for(int j=0,to=n-i;j<to;j++){
				Set<Integer> s = new HashSet<>();
				s.add(i);
				s.add(n-j);
				distancias.put(s,DISTANCIAS_RAW[i][j]);
			}
		}

		// launch(args);
		new App();
	}
	
	// @Override
	// public void start(Stage primaryStage) throws Exception {
	public App(){
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JFXPanel jfxPanel = new JFXPanel();
		add(jfxPanel);
		
		Platform.runLater(() -> {
			WebView webView = new WebView();
			jfxPanel.setScene(new Scene(webView));
			webEngine = webView.getEngine();
			webEngine.load(getClass().getResource("res/index.html").toExternalForm());
			
			App self=this;
			webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
        @Override
        public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
					if (newState == State.SUCCEEDED) {
						JSObject window = (JSObject) webEngine.executeScript("window");
						window.setMember("app", self);
					}
				}
      });
		});

		// Preparación de la ventana.
		setIconImage(new ImageIcon(getClass().getResource("/res/Logo AG.png")).getImage());
		setTitle("Algoritmos Genéticos - TP3");
		// Si no hacemos esto, cuando lo sacamos de pantalla completa la aplicación se achica a alto 0 y ancho mínimo.
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width*3/4,screenSize.height*3/4);
		setLocation(screenSize.width/8, screenSize.height/8);
		setVisible(true);
		setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
	}
	
  // Función para iniciar la simulación.
	private void reiniciar(){
		sumatoriaPuntuaciones=0;
		// Generación de la primera población aleatoria.
		poblacionActual=new Individuo[tamañoPoblacion];
		for(int i=0;i<tamañoPoblacion;i++){
			// ArrayList<Integer> cromosoma= new ArrayList<>();
			// for(int j=0;j<App.PROVINCIAS.length;j++)
			// 	cromosoma.add(j);
			// Collections.shuffle(cromosoma);
			poblacionActual[i]=new Individuo(
				Utils.randomIntBetween(1, 8), // Cantidad de líneas
				Utils.randomIntBetween(1, 5), // Cantidad de bordes
				Utils.randomDoubleBetween(0-Math.PI, Math.PI), // Angulo
				Math.random()>.5, // Reflexion
				Utils.randomIntBetween(1,3) // Iteraciones
			);
			System.out.println(poblacionActual[i]);

			System.exit(0);

			double puntuacion=objetivo(poblacionActual[i]);
			poblacionActual[i].valorFuncionObjetivo=puntuacion;
			sumatoriaPuntuaciones+=puntuacion;
		}
		
		ordenarPoblacion(poblacionActual);
		mandarGeneracionActual();
	}
	
  // Función para realizar cada generación de la simulación.
	private void nuevaGeneracion(){
		
		vectorFitness=new double[tamañoPoblacion];
		for(int j=0;j<tamañoPoblacion;j++)
			vectorFitness[j]=poblacionActual[j].valorFuncionObjetivo/sumatoriaPuntuaciones;
		
		sumatoriaPuntuaciones=0;

		int cantidadPares=tamañoPoblacion/2;
		Individuo[] nuevaPoblacion=new Individuo[tamañoPoblacion];

		if(elitismo){
			// Guardamos el 20% de la población total (redondeado hacia abajo), de ser impar se guarda uno menos.
			int tamañoReducido=tamañoPoblacion/5;
			if (tamañoReducido%2 == 1)
				tamañoReducido--;
			
			cantidadPares -= tamañoReducido/2;

			for (int i = 0; i < tamañoReducido; i++){
				nuevaPoblacion[tamañoPoblacion-i-1]=poblacionActual[i].crearClon();
				sumatoriaPuntuaciones+=poblacionActual[i].valorFuncionObjetivo;
			}
		}

		for(int j=0;j<cantidadPares;j++){
			
			// Aplicación de selección.
			int j1=j*2,j2=j1+1;
			Individuo individuo1=poblacionActual[elegirIndicePorRuleta(vectorFitness)].crearClon()
				,individuo2=poblacionActual[elegirIndicePorRuleta(vectorFitness)].crearClon();
	
			// Aplicación de crossover. (Se encarga la clase Individuo)
			if(individuo1.equals(individuo2) || Math.random()<.25){
				nuevaPoblacion[j1]=individuo1;
				nuevaPoblacion[j2]=individuo2;
			}else{
				Individuo[] hijos=individuo1.crossover(individuo2);
				nuevaPoblacion[j1]=hijos[0];
				nuevaPoblacion[j2]=hijos[1];
			}
			
			// Aplicación de mutación.
			nuevaPoblacion[j1].aplicarMutacion();
			nuevaPoblacion[j2].aplicarMutacion();
			
			// Cálculo del valor objetivo. (Ver método objetivo.)
			double valorObjetivo1=objetivo(nuevaPoblacion[j1])
				,valorObjetivo2=objetivo(nuevaPoblacion[j2]);
			
			nuevaPoblacion[j1].valorFuncionObjetivo=valorObjetivo1;
			nuevaPoblacion[j2].valorFuncionObjetivo=valorObjetivo2;
			
			// Sumatoria de todos los resultados de la función objetivo de la generación (sirve para el promedio y la próxima selección).
			sumatoriaPuntuaciones+=valorObjetivo1+valorObjetivo2;
		}

		ordenarPoblacion(nuevaPoblacion);
		poblacionActual=nuevaPoblacion;
	}

	private int elegirIndicePorRuleta(double[] vectorFitness){
		return elegirIndicePorRuleta(vectorFitness, DoubleStream.of(vectorFitness).sum());
	}

	private int elegirIndicePorRuleta(double[] vectorFitness, double totalSumaVector){
		double acc=0,selector=Math.random()*totalSumaVector;
		// No hay forma de que la probabilidad (selector) sea mayor a 1, y la suma (acc) va a llegar a 1 en algun momento.
		// Por lo que este for va a en algún momento terminar con un elegido.
		for(int l=0;l<vectorFitness.length;l++){
			acc+=vectorFitness[l];
			if(acc>selector)
				return l;
		}
		// Aún así, a veces por división de punto flotante, la suma no es exactamente igual a 1 y el número aleatorio puede entrar en ese margen de error.
		// Por lo que en ese caso, elegimos el último.
		// Técnicamente le estamos asignando el resto de la probabilidad a un cromosoma aleatorio, pero es una probabilidad insignificante.
		return vectorFitness.length-1;
	}

	// Ordenamos la población para facilitar la obtención del mejor y peor recorrido.
	private void ordenarPoblacion(Individuo[] poblacion){
		Arrays.sort(poblacion);
	}

	private Individuo algoritmoHeuristico(int cabeceraDeOrigen){
		int cantidadProvincias = App.PROVINCIAS.length;
		// Recordamos las ciudades visitadas para no pasar de vuelta.
		ArrayList<Integer> recorrido = new ArrayList<>();
		recorrido.add(cabeceraDeOrigen);
		int longitud = 0;

		int provinciaActual = cabeceraDeOrigen;
		// Por todas las ciudades que restan visitar, vamos paso a paso viendo cuál está más cerca de nuestra posición actual.
		for(int i=0,to=cantidadProvincias-1; i < to; i++){
			ArrayList<Integer[]> distanciasProvincias= new ArrayList<>();
			for(int j=0; j < cantidadProvincias; j++){
				if(!recorrido.contains(j)){ // Si no se pasó por esta ciudad, guardar el nombre y la distancia.
					int distancia = obtenerDistanciaEntre(provinciaActual, j);
					distanciasProvincias.add(new Integer[]{j,distancia});
					if(distanciasProvincias.size()==to-i) // Si solo faltan 3 ciudades y ya las encontramos, no tiene sentido seguir fijandose las demás.
						break;
				}
			}
			if(distanciasProvincias.size()>1) // Si hay una sola ciudad no tiene sentido ordenarlo.
				Collections.sort(distanciasProvincias,(Integer[] p1, Integer[] p2) -> p1[1].compareTo(p2[1])); // Ordenamos por distancias.
			// Obtenemos la más cercana.
			Integer[] provinciaMasCercana=distanciasProvincias.get(0);

			longitud += provinciaMasCercana[1];
			provinciaActual = provinciaMasCercana[0];
			recorrido.add(provinciaActual);
		}

		longitud+=obtenerDistanciaEntre(provinciaActual,cabeceraDeOrigen); // Volvemos a la ciudad de partida.
		
		return new Individuo(recorrido, longitud);
	}

	// API para el frontend.

	// Simulacion de Heuristica
	public void algoritmoHeuristicoDesde(int cabeceraDeOrigen){
		mandarHeuristico(algoritmoHeuristico(cabeceraDeOrigen));
	}

	public void algoritmoHeuristicoPorTodos(){
		ArrayList<Individuo> recorridosHeuristicos= new ArrayList<>();
		for(int i=0;i<App.PROVINCIAS.length;i++)
			recorridosHeuristicos.add(algoritmoHeuristico(i));
		Collections.sort(recorridosHeuristicos);
		mandarHeuristico(recorridosHeuristicos.get(0));
	}

	private void mandarHeuristico(Individuo ind){
		StringBuilder sb = new StringBuilder("recibirResultadoHeuristico([");
		for(int p : ind.cromosoma)
			sb.append(p+",");
		ejecutarJS(sb.toString()+"],"+ind.longitud+")");
	}
	
	// Simulacion de Genético
	public void iniciarSimulacion(int cantidadIndividuos, int cantidadCorridas,boolean conElitismo){
		tamañoPoblacion=cantidadIndividuos;
		elitismo=conElitismo;
		
		reiniciar();
		
		// La primera corrida es la primera generación aleatoria, por eso restamos uno.
		siguienteGeneracion(cantidadCorridas-1);
	}

	private void mandarGeneracionActual(){
		String[] poblacionAsJSON=new String[tamañoPoblacion];
		for (int i = 0; i < tamañoPoblacion; i++)
			poblacionAsJSON[i]=poblacionActual[i].toJSONObject();
		ejecutarJS("proximaGeneracion(["+String.join(",",poblacionAsJSON)+"]);");
	}

	public void siguienteGeneracion(int cantidadVeces){
		for(int i=0;i<cantidadVeces;i++){
			nuevaGeneracion();
			mandarGeneracionActual();
		}
	}

	// Común
	private void ejecutarJS(String comando){
		webEngine.executeScript(comando);
	}

}