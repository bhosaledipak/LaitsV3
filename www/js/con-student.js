/* global define */
/*
 *                          student mode-specific handlers
 */
define([
    "dojo/_base/array", 'dojo/_base/declare', "dojo/_base/lang",
    "dojo/dom", "dojo/ready",
    'dijit/registry',
    './controller', "./pedagogical_module", "./equation",'dojo/dom-style'
], function(array, declare, lang, dom, ready, registry, controller, PM, expression,style){

    /*
     Methods in controller specific to the student modes
     */

    return declare(controller, {
        _PM: null,

        constructor: function(mode, subMode, model){
            console.log("++++++++ In student constructor");
            this._PM = new PM(mode, subMode, model);
            lang.mixin(this.widgetMap, this.controlMap);
	    ready(this, "populateSelections");
        },
        // A list of control map specific to students
        controlMap: {
            description: "selectDescription",
            units: "selectUnits",
            inputs: "nodeInputs"
        },

	populateSelections: function(){
	    /*
             Initialize select options in the node editor that are
             common to all nodes in a problem.
             
             In AUTHOR mode, this needs to be done when the
             node editor is opened.
             */
            // Add fields to Description box and inputs box
            // In author mode, the description control must be a text box
            var d = registry.byId(this.controlMap.description);
            // populate input field
            var t = registry.byId(this.controlMap.inputs);
            console.assert(t, "Can't find widget " + this.controlMap.inputs);
            var positiveInputs = registry.byId("positiveInputs");
            var negativeInputs = registry.byId("negativeInputs");
            console.log("description widget = ", d, this.controlMap.description);
            // d.removeOption(d.getOptions()); // Delete all options
            array.forEach(this._model.given.getDescriptions(), function(desc){
                d.addOption(desc);
                var name = this._model.given.getName(desc.value);
                var option = {label: desc.label + ' ' + ' | ' + ' ' + name, value: desc.value};
                t.addOption(option);
                positiveInputs.addOption(option);
                negativeInputs.addOption(option);
            }, this);
	},

        handleDescription: function(selectDescription){
            console.log("****** in handleDescription ", this.currentID, selectDescription);
            if(selectDescription == 'defaultSelect')
                return; // don't do anything if they choose default

            this._model.active.setDescriptionID(this.currentID, selectDescription);
            this.updateNodes();

	    // This is only needed if the type has already been set,
	    // something that is generally only possible in TEST mode.
            this.updateEquationLabels();

            this.applyDirectives(this._PM.processAnswer(this.currentID, 'description', selectDescription));
        },
	descriptionSet: function(value){
            // Update the model.
            this._model.student.setDescriptionID(this.currentID, value);
	    this.updateNodes();
	},

        handleType: function(type){
            console.log("****** Student has chosen type ", type, this);
            if(type == 'defaultSelect')
                return; // don't do anything if they choose default
            this.updateType(type);
            this.applyDirectives(this._PM.processAnswer(this.currentID, 'type', type));
        },
	typeSet: function(value){
	    this.updateType(value);
	},

        handleInitial: function(initial){

            console.log("****** Student has chosen initial value", initial, this.lastInitialValue);
	    /*
	     Evaluate only if the value is changed.

	     The controller modifies the initial value widget so that a "Change" event is
	     fired if the widget loses focus.  This may happen when the node editor is closed.
	     */
	    if(!initial || initial == this.lastInitialValue){
		return;
	    }
	    this.lastInitialValue = initial;

            // updating node editor and the model.
            this._model.active.setInitial(this.currentID, initial);
            this.applyDirectives(this._PM.processAnswer(this.currentID, 'initial', initial));
        },
        initialSet: function(value){
            this._model.active.setInitial(this.currentID, value);
	},

        /*
        *    handle event on inputs box
        * */
        handleInputs: function(id){
            /*if(id.MOUSEDOWN){
             if(this.lastHandleInputId){
             console.log('onclick event found onSelect, use old id '+this.lastHandleInputId);
             id=this.lastHandleInputId; //restore
             }else
             return;  //if last id is not defined return
             }else
             this.lastHandleInputId=id; //copy it for next onClick event*/

            //check if id is  not select else return

            console.log("*******Student has chosen input", id, this);
            // Should add name associated with id to equation
            // at position of cursor or at the end.
            var expr = this._model.given.getName(id);
            this.equationInsert(expr);
            //restore to default  - creating select input as stateless
            registry.byId(this.controlMap.inputs).set('value', 'defaultSelect', false);
        },
        handleUnits: function(unit){
            console.log("*******Student has chosen unit", unit, this);

            // updating node editor and the model.
            this._model.student.setUnits(this.currentID, unit);
            this.applyDirectives(this._PM.processAnswer(this.currentID, 'units', unit));
        },
        unitsSet: function(value){
            // Update the model.
            this._model.student.setUnits(this.currentID, value);
	},
        equationDoneHandler: function(){
            var directives = [];
            var parse = this.equationAnalysis(directives);
            if(parse){
                var dd = this._PM.processAnswer(this.currentID, 'equation', parse);
                directives = directives.concat(dd);
            }
	    this.applyDirectives(directives);
        },
	equationSet: function(value){
	    // applyDirectives updates equationBox, but not equationText:
	    dom.byId("equationText").innerHTML = value;

	    var directives = [];
	    // Parse and update model, connections, etc.
            this.equationAnalysis(directives);
	    // Generally, since this is the correct solution, there should be no directives
	    this.applyDirectives(directives);
	},
 
	/* 
         Settings for a new node, as supplied by the PM.
         These don't need to be recorded in the model, since they
         are applied each time the node editor is opened.
         */
        initialControlSettings: function(nodeid){
            // Apply settings from PM
	    this.applyDirectives(this._PM.newAction(), true);

            // Set the selected value in the description.
            var desc = this._model.student.getDescriptionID(nodeid);
            console.log('description is', desc || "not set");
            registry.byId(this.controlMap.description).set('value', desc || 'defaultSelect', false);

            /*
             Set color and enable/disable
             */
            array.forEach(this._model.student.getStatusDirectives(nodeid), function(directive){
                var w = registry.byId(this.controlMap[directive.id]);
                w.set(directive.attribute, directive.value);
		// The actual values should be in the model itself, not in status directives.
                if(directive.attribute == "value"){
		    console.error("Values should not be set in status directives");
		}
            }, this);
        },

        // Need to save state of the node editor in the status section
        // of the student model.  See documentation/json-format.md
        updateModelStatus: function(desc) {
            if (this.validStatus[desc.attribute]) {
                var opt = {};
                opt[desc.attribute] = desc.value;
                this._model.student.setStatus(this.currentID, desc.id, opt);
            } else {
                // There are some directives that should update
                // the student model node (but not the status section).

                // console.warn("======= not saving in status, node=" + this.currentID + ": ", desc);
            }
        },
		colorNodeBorder: function(){
				//console.log(this._model.student.getCorrectness());   //getCorrectness using student function  - studentID ? 
				var color = 'red';  //decide color using correctness response , test red
				console.log('coloring node  '+this.currentID);
				style.set(this.currentID,'border','1px solid '+color);
				//save the node
		}
    });
});
