/* global define */
/*
 Get and set state variables for the tutor or the student.

 Variables are defined by:
     section (string):
     user (string): If user is omitted, then variable is a section-wide default.
     apopropos (string):  A group of parameters (see below)
     property (string):  A parameter name  
     value (JavaScript object):  Stored as JSON in the database

 Note that one may choose to store several parameters together as a more
 complicated object, if that is more convenient.

 Internally, the database table uses tid to label variables so that the 
 history of a variable may be tracked.

 Choices for apropos include:

     policies: settings of the tutor system, such as whether to use
         structured or algebraic equation inputs on the node editor
         or what level of hints to give.

     actions: counters detailing what the student has done
         in the past.  For example: how many greeens the student has seen.

     skills: variables describing what the student does or does not know

 Generally, actions are used to determine skills and skills are used
 to determine policies.

 The interface sort of follows the Dojo Stores API.
*/

define([
    "dojo/_base/declare",
    "dojo/_base/json",
    "dojo/_base/lang",
    "dojo/promise/Promise",
    "dojo/request/xhr",
    "dojo/when"
], function(declare, json, lang, promise, xhr, when){

    return declare(null, {

	cache: {},
	
	constructor: function(user, section, apropos, path){
	    this.params = {u: user, s: section, aps: apropos};
	    // Add a trailing slash to path, if it exists
	    if(path && path.substr(-1) != '/') path += '/';
	    this.path = (path || "") + "state.php";
	},
	
	/*
	 Get always returns a promise
	 */
	get: function(property){
	    if(property in this.cache)
		return when(this.cache[property]);
	    else {
		var object = xhr.get(this.path, {
		    query: lang.mixin({pry: property}, this.params),
		    handleAs: "json"
		});
		object.then(lang.hitch(this, function(x){
		    // Don't add to cache if property is also not on the server.
		    if(x)
			this.cache[property] = x;
		}));
		return object;
	    }
	},

	/*
	 This doesn't quite fit the Dojo Store API.
	*/
	put: function(property, object){
	    if(!this.cache){
		console.warn("Invalid scope ", this);
		console.trace();
	    }
	    this.cache[property] = object;
	    xhr.post(this.path, {
		data: lang.mixin({
		    pry: property, 
		    vle: json.toJson(object)
		}, this.params)
	    });
	},

	remove: function(property){
	    delete this.cache[property]; 
	    xhr.post(this.path, {
		data: lang.mixin({
		    pry: property, 
		    vle: ""
		}, this.params)
	    });
	},

	/*
	 This is not in Dojo Store API.
	 */
	increment: function(property, step){
	    return this.get(property).then(lang.hitch(this, function(x){
		x += step===undefined?1:step;
		this.put(property, x);
	    }));
	}

    });
});
