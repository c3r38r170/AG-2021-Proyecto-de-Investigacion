//mock API
if(!app)
	var app={
		siguienteGeneracion:function(){
			proximaGeneracion(
				new Array(10).fill().map((el,i)=>({
					recorrido:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0,16,17,18,19,20,21,22,23].sort(()=>Math.random()-.5)
					,longitud:Math.round(Math.random()*5419669)
				}))
			);
		}
		,iniciarSimulacion:function(){
			proximaGeneracion(
				new Array(10).fill().map((el,i)=>({
					recorrido:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0,16,17,18,19,20,21,22,23].sort(()=>Math.random()-.5)
					,longitud:Math.round(Math.random()*5419669)
				}))
			);
		}
		,algoritmoHeuristico:function(){
			recibirResultadoHeuristico([20,18,6,17,5,13,21,14,16,15,11,2,3,9,8,19,1,0,4,23,10,7,12,22],8129)
		}
		,algoritmoHeuristicoPorTodos:function(){
			recibirResultadoHeuristico([20,18,6,17,5,13,21,14,16,15,11,2,3,9,8,19,1,0,4,23,10,7,12,22],8129)
		}
	};

var nombresProvincias=[
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
];

var coordenadas=[
	[-58.3712, -34.6083], //Bs.AS
	[-64.183, -31.417], // Cordoba
	[-58.8403, -27.4711], //Corrientes
	[-58.1781, -26.1775], //Formosa
	[-57.9544, -34.9214], //La plata
	[-66.8558, -29.4131], //La rioja
	[-68.8472, -32.8903], //Mendoza
	[-68.0744, -38.9516], //Neuquen
	[-60.5300, -31.7331], // Entre rios
	[-54.5333, -26.8666], // Misiones
	[-65.1000, -43.3000], //Chubut
	[-58.9866, -27.4513], //Chaco
	[-68.815, -48.8238], //Santa cruz
	[-65.7792, -28.4686], //Catamarca 
	[-65.2169, -26.8198], //Tucuman
	[-65.2945, -24.1894], //Jujuy 
	[-64.5, -24.55], //Salta
	[-68.5253, -31.5372], //San Juan
	[-66.35, -33.3], //San Luis
	[-60.6917, -31.6406], //Santa fe
	[-64.2999, -36.6333], //La Pampa
	[-64.2614, -27.7951], //Stgo. Del Estero
	[-68.3044, -54.8072], //Tierra del fuego
	[-62.9973, -40.8133] //Rio Negro
];

/*
TODO crear objeto mapa con mapa y layers
{
	mapa:Map
	,layers:{
		unica: {
			color: 'color'
			,layer:Layer
		}
		,mejors: {
			color: 'color'
			,layer:Layer
		}
		...
	}
} */
var layer;

// manipulación de mapas
function mostrarIndividuo(individuo){
	gEt('individuo-recorrido-valor').innerText=individuo.longitud;

	cambiarRecorrido(mapaIndividuo,'unico',individuo.recorrido);

	let listaHolder=gEt('individuo-recorrido');
	listaHolder.innerHTML=
		individuo.recorrido.reduce((acc,el)=>acc+`<li>${nombresProvincias[el]}</li>`,'');
}

function cambiarRecorrido(mapa,layerName,recorrido) {
	let layerObj=mapa.layers[layerName];
	mapa.mapa.removeLayer(layerObj.layer);
	
	recorrido =recorrido.map(el=>ol.proj.fromLonLat(coordenadas[el]));
	let layer = new ol.layer.Vector({
		source: new ol.source.Vector({
			features: [
				new ol.Feature({
					geometry: new ol.geom.LineString([...recorrido,recorrido[0]]),
					name: "Line",
				}),
			],
		})
		,style:new ol.style.Style({
			fill: new ol.style.Fill({
					color: 'white'
			}),
			stroke: new ol.style.Stroke({
					color: layerObj.color,
					width: 1
			})
		})
	});

	mapa.mapa.addLayer(layer);
	mapa.layers[layerName].layer=layer;
}

//important methods
var gEt=id=>document.getElementById(id);
var qS=selector=>document.querySelectorAll(selector);
var mejorLongitudHastaAhora=Infinity;
var generaciones=[];
/*formato de la generación:
{
	individuos:[
		{
			
		}
	]
	,peor:
	,med:
	,mejor:
}
*/

// Genético
function proximaGeneracion(generacion){
	generaciones.push(generacion);

	if(generacion[0].longitud<mejorLongitudHastaAhora || generaciones.length==1)
		actualizarMejorHastaAhora(generacion[0]);

	// Tabla de generaciones
	let nuevaGeneracion=document.createElement('DIV');
	nuevaGeneracion.classList.add('generaciones-n');
	nuevaGeneracion.dataset.id=generaciones.length-1;
	nuevaGeneracion.innerHTML=`
<div class="generaciones-n-individuos">
	<span class="generaciones-n-individuos-header">Individuo</span>
	<span class="generaciones-n-individuos-header">Longitud del recorrido (km)</span>
	${generacion.reduce((acc,el,i)=>acc+='<div class=generaciones-n-individuo>'+[
			i+1
			,el.longitud
		].reduce((ac,e)=>ac+`<span>${e}</span>`,'')+'</div>','')}
</div>
<div class="generaciones-n-resumen">
	<span>${generaciones.length}</span>
	<span class="kilometros">${generacion[generacion.length - 1].longitud}</span>
	<span class="kilometros">${generacion.length%2==0? //Mediano
		(generacion[generacion.length/2].longitud+generacion[generacion.length/2+1].longitud)/2
		:generacion[Math.floor(generacion.length/2)].longitud}</span>
	<span class="kilometros">${generacion[0].longitud}</span>
</div>
`;
	gEt('generaciones').append(nuevaGeneracion);

	seleccionarGeneracion(nuevaGeneracion.children[1]);
}

function actualizarMejorHastaAhora(individuo){
	mejorLongitudHastaAhora=individuo.longitud;
	let individuoMejor=gEt('individuo-mejor');
	individuoMejor.dataset.generacionID=generaciones.length-1;
	individuoMejor.children[0].innerText=mejorLongitudHastaAhora;
}

function seleccionarIndividuo(individuo){
	if(individuo.classList.contains('generaciones-n-individuo-selected'))
		return;
	let selected=qS('.generaciones-n-individuo-selected');
	if(selected.length)
		selected[0].classList.remove('generaciones-n-individuo-selected');
	individuo.classList.add('generaciones-n-individuo-selected');
	mostrarIndividuo(
		generaciones
			[+individuo.closest('.generaciones-n').dataset.id]
			[individuo.firstElementChild.innerText-1]
	);
}

function seleccionarGeneracion(generacion,poblacion=generaciones[generacion.firstElementChild.innerText-1]) { //generacion es un '.generaciones-n-resumen'
	let showing=qS('.generaciones-n-individuos-shown');
	if(showing.length && showing[0]!=generacion.previousElementSibling)
		showing[0].classList.remove('generaciones-n-individuos-shown');
	let thisResumenCL=generacion.previousElementSibling.classList;
	if(thisResumenCL.contains('generaciones-n-individuos-shown'))
		thisResumenCL.remove('generaciones-n-individuos-shown');
	else{
		thisResumenCL.add('generaciones-n-individuos-shown');

		let mejor=poblacion[0]
			,peor=poblacion[poblacion.length - 1];
		// Mapa
		cambiarRecorrido(mapaGeneraciones,'mejor',mejor.recorrido);
		cambiarRecorrido(mapaGeneraciones,'peor',peor.recorrido);
		gEt('generaciones-datos-mejor-longitud').innerText=mejor.longitud;
		gEt('generaciones-datos-peor-longitud').innerText=peor.longitud;
	}
}

// Heuristica
function recibirResultadoHeuristico(recorrido,longitud) {
	gEt('individuo-mejor').children[0].innerText=longitud;
	mostrarIndividuo({recorrido,longitud});
}

//onload
addEventListener('DOMContentLoaded',()=>{
	gEt('modal-abrir').click();

	gEt('cabeceraOrigen').innerHTML+=nombresProvincias.reduce((acc,el,i)=>acc+`<option value=${i}>${el}</option>`,'');

	gEt('modal-iniciar').onclick=()=>{
		let container =gEt('container-principal')
		if(gEt('tipoAlgoritmoHeuristica').checked){
			container.classList.add('heuristico');

			if(gEt('cabeceraOrigenRadio').checked){
				let cabeceraOrigen=gEt('cabeceraOrigen').value;
				app.algoritmoHeuristicoDesde(!!cabeceraOrigen?+cabeceraOrigen:19);
			}else{
				app.algoritmoHeuristicoPorTodos();
			}
		}else{
			container.classList.remove('heuristico');

			generaciones=[];
			for(let generacion of [...qS('.generaciones-n')])
				generacion.remove();
			app.iniciarSimulacion(
				+gEt('modal-individuos').value||10
				,+gEt('modal-corridas').value||1
				,gEt('elitismo').checked
			);

			mapaIndividuo.mapa.removeLayer(mapaIndividuo.layers.unico.layer);
		}
	};

	gEt('controles-siguiente').onclick=()=>{
		app.siguienteGeneracion(+gEt('controles-pasos').value);
	}

	gEt('generaciones').onclick=e=>{
		let t=e.target;
		let resumen=t.closest('.generaciones-n-resumen');
		if(resumen){
			seleccionarGeneracion(resumen);
		}else{
			let individuo=t.closest('.generaciones-n-individuo');
			if(individuo)
				seleccionarIndividuo(individuo);
		}
	}

	// This shouldn't break, but be prepared for it.
	gEt('individuo-mejor').onclick=e=>{
		let generacionDiv=gEt('generaciones').children[1+(+e.target.closest('#individuo-mejor').dataset.generacionID)];
		let [individuosDiv,resumenDiv]=generacionDiv.children;
		seleccionarIndividuo(individuosDiv.children[2])
		seleccionarGeneracion(resumenDiv);
		generacionDiv.scrollIntoView(false);
	};

//TODO Hacer bloqueos inteligentes (condicionales) en el modal.

});