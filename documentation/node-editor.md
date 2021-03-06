# Node Editor Design #

* The undo button should be "Clear"
* The done button should be "Check expression"
* There should be a "Done" button at bottom to close window.  It should have the same behavior as the *X* button in the upper right hand corner.

## Expression Input Methods##

There are two basic methods for inputting expressions for Accumulator and Function nodes.  Algrebraic Expression Input is more flexible and suitable for users with stronger math skills, while Structured Expression Input is intended for users with weaker math skills.  For all major modes, choosing which of the two input method is available to the student/author is a pedagogical decision.  It is based on a student model, possibly using default settings for a section, subject to the constraints mentioned below.

###Algebraic Expression Input###

In this case, there is an input text area, a pull-down menu to select quantities, and buttons for some arithmetic operators.

###Structured Expression Input###

If Structured Expression Input is available, then there will be radio buttons to allow the student to choose between "Sum" and "Product."  Alternatively, these choices may be listed as subchoices of "Function" and "Accumulator" in the Type selector control.

The second consists of two select controls.  The first control allows the user to list positive contributions while the second allows the user to list negative contributions to the quantity. In AUTHOR mode, the user must be able to type in node names that do not yet exist in the list of nodes.  This precludes using the HTML control `<select multiple>` or the Dijit widget `dijit/form/MultiSelect`.  Instead, we will use `dijit/form/Select` in the student modes and `dijit/form/ComboBox` in AUTHOR mode.  Selecting an item will add that item to the expression shown below the box.  The "Clear" button will clear all the quantities selected.

Structured input style can only be used on nodes where the expression is a pure sum or product, as defined by
the functions `isSum()` and `isProduct()` in `www/js/equation.js`.  Thus, this input format may be enabled only on problems where *all* the Accumulator and Function nodes in the given model are pure sums or products.  This should be tested when a problem is opened.
