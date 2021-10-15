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
	
	private int tamañoPoblacion=10;
	
	private WebEngine webEngine;

  // Función que sirve para evaluar el desempeño de cada individuo.
	private static double objetivo(Individuo individuo){
		// El inverso de la cantidad de miles de kilómetros.
		// Los recorridos tienen probabilidades relativas proporcionales.
		// (Si un recorrido es el doble de largo que el otro, tendrá la mitad del fitness.)
		return 1000.0/individuo.longitud;
	}
	
	public static void main(String[] args) throws Exception {
		// launch(args);
		new App();
	}
	
	// @Override
	// public void start(Stage primaryStage) throws Exception {
	public App(){
		reiniciar();

		System.exit(0);

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

			// double puntuacion=objetivo(poblacionActual[i]);
			// poblacionActual[i].valorFuncionObjetivo=puntuacion;
			// sumatoriaPuntuaciones+=puntuacion;
		}
		
		// ordenarPoblacion(poblacionActual);
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

	// API para el frontend.
	public void iniciarSimulacion(int cantidadIndividuos, int cantidadCorridas,boolean conElitismo){
		tamañoPoblacion=cantidadIndividuos;
		elitismo=conElitismo;
		
		reiniciar();
		
		// La primera corrida es la primera generación aleatoria, por eso restamos uno.
		siguienteGeneracion(cantidadCorridas-1);
	}

	private void mandarGeneracionActual(){
		// TODO
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