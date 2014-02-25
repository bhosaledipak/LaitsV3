/* global define, Image */
define([
    "dojo/dom",
    'dojo/dom-geometry',
    "dojo/on",
    'dojo/aspect',
    "dojo/io-query",
    "dojo/ready",
    "./menu",
    "./load-save",
    "./model",
    "./RenderGraph", "./RenderTable", "./wraptext", 
    "./controller",
    "parser/parser",
    "./draw-model",
    "./pedagogical_module",
	"./calculations"
],function(dom, geometry, on, aspect, ioQuery, ready, menu, loadSave, model, 
	   Graph, Table, wrapText, controller, Parser, drawmodel, PM,calculations
	  ){ 

    console.log("load main.js");
	    
    // Get session parameters
    var query={};
    if(window.location.search){
	query = ioQuery.queryToObject(window.location.search.slice(1));
    } else {
        console.warn("Should have method for logging this to Apache log files.");
        console.warn("Dragoon log files won't work since we can't set up a session.");
	console.error("Function called without arguments");
    }
    
    // Start up new session and get model object from server
    var session = new loadSave(query);	   
    session.loadProblem(query).then(function(solutionGraph){

	console.info("Have solution: ", solutionGraph);

	var givenModel = new model(query.m);
	givenModel.loadModel(solutionGraph);
	
	/*
	@author: Deepak
	@brief: calling calculation class to get node values and passing parameters to rendergraph and rendertable
	*/
	var calc = new calculations(solutionGraph);
	var obj = calc.gerParametersForRendering(solutionGraph);
	
	/*
	 start up controller
	 */
	 
	ready(function(){

	    var drawModel = new drawmodel(givenModel);
	    var pm = new PM(query.u, givenModel);
	    var controllerObject  = new controller(givenModel);
	    
	    /* add to menu */
	    menu.add("createNodeButton", function(){
		var id = givenModel.addStudentNode();
		drawModel.addNode(givenModel.getStudentNode(id));
		controllerObject.showNodeEditor(id);
	    });
	    
	    /*
	     Connect node editor to "click with no move" events.
	     */
	    aspect.after(drawModel, "onClickNoMove", function(mover){
		controllerObject.showNodeEditor(mover.node.id);
	    }, true);
	    
	    /* 
	     After moving node, save coordinates to model 
	     */	     
	    aspect.after(drawModel, "onClickMoved", function(mover){
		var g = geometry.position(mover.node, true);  // take into account scrolling
		console.log("Update model coordinates for ", mover.node.id, g);
		console.warn("This should take into account scrolling, Bug #2300.");
		givenModel.setStudentNodeXY(mover.node.id, g.x, g.y);
		console.warn("Should autosave here.");
	    }, true);
	    
	    /*
	     It would make more sense to call initHandles for each node as it is created
             on the canvas.
	     
	     In AUTHOR mode, this will break, since we want the solution
	     graph in that case.  See trello card https://trello.com/c/TDWdq6q6
	     */
	    controllerObject.initHandles();
	    
	    /*
	     Make model solution plot using dummy data. 
	     This should be put in its own module.
	     */	
	
	    // instantiate graph object
	    var graph = new Graph(obj);
	    // show graph when button clicked
	    menu.add("graphButton",function(){
		console.debug("button clicked");	   
		graph.show();
	    }); 
	    
		var table = new Table(obj);
	
	    // show graph when button clicked
	    menu.add("tableButton", function(){        	
		console.debug("table button clicked");
		table.show();
	    });

	    var canvas = document.getElementById('myCanvas');
      	var context = canvas.getContext('2d');
      	var imageObj = new Image();
      	var desc_text = givenModel.getTaskDescription();

      	imageObj.onload = function() {
        	context.drawImage(imageObj, 69, 50);
        	wrapText(context, desc_text, 70, 400, 400, 20);
      	};
      	imageObj.src = givenModel.getURL();

	});
    });    
});


