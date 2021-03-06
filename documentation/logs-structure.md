# Logging Format #
sss
This document discusses the structure of the logging and the associated table formats on the server.

## Database Table Structure ##

The logging will use the **`session`** table (see
[sessions.md](sessions.md))  and:

**`step`**:  Log individual events from client:

*	`tid` – An auto-incremented integer that uniquely identifies each log event.
*	`session_id` – foreign key. 
*	`method` can be one of the following:
	*	`start-session` send session ID to the server as well as the user name and section and major mode.  Normally, it will also include the problem name and the author name for custom problems.  This will create an entry in the `session` table.
	* `open-problem` - Client asks for the problem from the server or opens a local file.  The message will include problem name and possibly section name and author name for custom problems.
	*	`client-message` - Java/JavaScript exceptions and warnings.  Messages associated with the dragoon code itself.
	*	`student-status` - Informational messages about the status of the student's progress in solving the problem.  
	*	`ui-action` - Actions taken by the user on the interface, such as clicking on a menu or moving a node on the canvas, that are not problem-solving steps.  We will not log keystrokes or mouse events but a level above it. Like switch tabs and open node editor, values added to the node editor *et cetera*.  
 The JavaScript version will record going in and out of focus.  See [`andes/drawing.js`](https://github.com/bvds/andes/blob/master/web-UI/andes/drawing.js) for an example.
 *	`solution-step`  - Steps that the student takes toward solving the
 problem.  This will generally be associated with something that could
 turn red or green.  The log message will contain both the student
 action and the tutors response, if any. For Dragoon, this includes the "check" button.
 * `seek-help` -  Student request for help and the response. For Dragoon, this includes the "Demo" button.
 *	`close-problem` - The student has closed the session.  This may be missing if the session was interrupted (e.g. the network connection died). 
*	`message` - A `text` format field that holds actual log message. The format is specified in the section "Message Format" below.

This table is analogous to the table `STEP_TRANSACTION` in Andes; see [`create_STEP_TRANSACTION.sql`](https://github.com/bvds/andes/blob/master/LogProcessing/database/create_STEP_TRANSACTION.sql).  The Andes table can be used to see how the `step` table should be formatted.


## Data to be logged##

Following a conversation with the instructors of the classes which
have used Dragoon, we've identified specific information which must be
included in the logs.  The following list enumerates the
most important information missing from the present logging:
 
* Enter Node Editor (method `ui-action`) Needed info:
  + Entered thru Create Node action or existing Node
  + Which tab Node Editor opens to
  + Which Node was opened (if existing)
* **Check** button pressed (method `solution-step`) Needed info:
  + The node
  + Tab that was checked
  + Value student entered
  + Correct or Incorrect
  + If incorrect, what was correct value
* **Demo** button pressed (method `seek-help`) Needed info:
  + The node
  + Tab that was demoed
* Calculations panel button **New Node** pressed (method `ui-action` or `solution-step`?)  Needed info:
 + Same **Check**/**Demo** info as default Node Editor
 + What node (if any) was created
* Close Node Editor (method `ui-action` or `solution-step`?) Needed info:
 + Was node completed (*i.e.* was Calculations panel correct or demoed, indicating node complete?)
* **Show Graph** button (method `ui-action`) Needed info:
  + Was correct or incorrect
  + If correct, indicate problem is completed
* **Show Forum** button pressed (method `ui-action`)
* End session (method `close-problem`) Needed info:
 + Was problem completed

For many examples, we have found the choice of `method` to be
ambiguous.  Thus, we need to further clarify the definitions of the
methods and perhaps merge some of them.

## Message Format ##

The `message` column of the `step` table will be in
[JSON](http://json.org/) format.  Each log message will include a member `time` that
contains the number of seconds, according to the client, that have elapsed since the start of
the session.

We will not write a formal definition of the log message format.  Instead, we will define it via a set of
examples. We can use the Andes logging as a staring point for creating
the Dragoon log message format.  See an 
[annotated session log for Andes](http://gideon.eas.asu.edu/web-UI/Documentation/AsuDocs/nokes-example-json.txt)
as well as the
[json-rpc SMD](http://dojotoolkit.org/reference-guide/dojox/rpc/smd.html)
specification of the
[Andes logging format](http://gideon.eas.asu.edu/web-UI/andes/andes3.smd).

### Example Session ###

Here is the logging for an example session:

Student opened a new task ID: 105 - Intro Problem 1  
--  method: `open-problem`  message: `{"time": 1.3, "problem":"105"}`  
For custom problems, it will also include the author and section.

Student pressed the **create node** button.  This might create two messages:
one for the menu button and one for opening the node editor.  
-- method: `ui-action`  
-- message: `{"time": 21.3, "type": "menu-choice",  
  "name": "create-node"}`  
-- method: `ui-action`  
-- message: `{"time": 21.3, "type": "open-dialog-box",
  "name": "node-editor", "tab": "DESCRIPTION", "node": null}`  
In the JavaScript version, we will use the node id to name the node, so this will never be null.  In the Java version, we use the node name, when it is known.

Possible logging message associated with above  
-- method: `student-status` 
-- message: `{"time": 21.3, "type": "info",
  "text": "Vertex Details before opening node editor", "data":
  {"node":  "description", "descriptionPanelStatus": null, "selected
  plan": null, "planPanelStatus": null, "nodeType": null}}`  
Note that member names with a space, like `"plan panel"`, do not work
well with JavaScript or other languages.  It is better to use
camelCase or underscores.

Student chooses a quantity in the description tab.  
-- method: `solution-step`  
-- message: `{"time": 40.2, "node": null, "type": "enter-quantity",
  "name": "fat content", "text": "The ratio of the weight of the fat
  in a potato chip to the weight of the potato chip", "checkResult":
  "CORRECT"}`  
In the JavaScript version, `"node"` is the node id, in the Java
  version, it is either null or the node name `"fat content"`.

Student switches tabs:  
-- method: `ui-action`  
-- message: `{"time": 50.1, "type": "dialog-box-tab",
  "name": "node-editor", "tab": "PLAN", "node": "fat content"}`  

Student chooses node type:  
-- method: `solution-step`  
-- message: `{"time": 53.1, "node": fat content, "type": "quantity-type",
  "name": "CONSTANT", "checkResult":  "CORRECT"}`

Student switches tabs:  
-- method: `ui-action`  
-- message: `{"time": 57.6, "type": "dialog-box-tab",  
  "name": "node-editor", "tab": "CALCULATIONS", "node": "fat content"}`  

Student fills out the calculation tab.   
-- method: `solution-step`  
-- message: `{"time": 60.2, "node": "fat content", "type": "quantity-initial-value",
  "value": "0.35", "correct-value": "0.35", "checkResult":  "CORRECT"}`  
For the calculation tab, `solution-step` logging can be broken into several messages, depending on how the
  grading/evaluation is done:  each `solution-step` should be something that
  is evaluated (turns red/rgeen) separately.

Student closes node editor:  
-- method: `ui-action`  
-- message: `{"time": 61.6, "type": "close-dialog-box",
  "name": "node-editor", "tab": "CALCULATIONS", "node": "fat content"}`  
Member `"tab"` is optional.

##### Not done yet, I was following session `4b736807374ba02eaa2131a22523b746` in table `laits_ram` #####

### Further Examples ###

Student closes node editor for an accumulator node in the rabbits
problem:  
-- method: `ui-action`  
-- message: `{"time": 61.6, "type": "close-dialog-box",
  "name": "node-editor", "tab": "CALCULATIONS", "node": "population"}`  
-- method: `student-status`   
-- message: `{"time": 61.6, "type": "info",
  "text": "Node Editor closed. Vertex Details after closing", "data":
  {"node":  "population", "Description": "The ratio of rabbits born in a month
to the rabbit population", "DescriptionPanelStatus": "CORRECT ",
"SelectedPlan": "accumulator", "PlanPanelStatus": "GAVEUP",
"CalculationPanelStatus": "CORRECT", "InitialValue": "0.2" }}`  
Member `"tab"` is optional.

For a quantity that is an accumulator, the logging for the calculations
tab should contain two actions by the student corresponding to the
initial value and the equation.  These can be
described using either two solution step rows or using a single solution step
with two substeps.

This is the version with two solution steps.  This matches the design
for the JavaScript version better.   
-- method: `solution-step`  
-- message: `{"time": 60.2, "node": "velocity", "type": "quantity-initial-value",
  "value": "0.45", "correct-value": "0.65", "checkResult": "INCORRECT"}`  
-- method: `solution-step`   
-- message: `{"time": 60.2, "node": "velocity", "type": "quantity-equation",
  "value": "fish+fowl*2", "correct-value": "fish-fowl", "checkResult":  "CORRECT"}`

This is the version with one solution step.  This better matches the
behavior of the current Java version where both things turn red/green
when the **check** button is clicked.  
-- method: `solution-step`  
-- message: `{"time": 60.2, "node": "velocity", "substeps"; [{"type": "quantity-initial-value",
  "value": "0.45", "correct-value": "0.65"},{"type": "quantity-equation",
  "value": "fish+fowl*2", "correct-value": "fish-fowl"}], "checkResult":  "INCORRECT"}`
