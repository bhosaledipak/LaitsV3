<?php
$username = isset($_POST['old_username'])?$_POST['old_username']:$_POST['username'];
$mode = $_POST['mode'];
$problem_id = $_POST['problem_id'];
$section = $_POST['section'];
// These are optional
$author = isset($_POST['author'])?$_POST['author']:0;
$forum_url = isset($_POST['forumurl'])?$_POST['forumurl']:0;
$new_problem = isset($_POST['newproblem'])?$_POST['newproblem']:0;

header("Content-Disposition: attachment; filename=\"laits-$problem_id.jnlp\"");
header('Content-type: application/x-java-jnlp-file');
echo "<?xml version='1.0' encoding='UTF-8'?>";

/*
  Since the jnlp file is created dynamically, don't include
  the href attribute in the jnlp element.
  http://lopica.sourceforge.net/ref.html#jnlp
*/
$uri= $_SERVER['REQUEST_URI'];
$codebase="http://" . $_SERVER['SERVER_NAME'] . 
  substr($uri,0,strrpos($uri,'/')+1);
/* Codebase not needed in Java 7.  */
echo "<jnlp codebase=\"$codebase\" spec=\"1.0+\">\n";

?>
 
    <information>
        <title>Laits</title>
        <vendor>Arizona State University</vendor>
        <homepage href="dragoon.asu.edu/"/>
        <description>LAITS is a System Dynamics Modeling tool developed at the Arizona State
            University Computer Science department.
        </description>
        <description kind="short">Laits</description>
    </information>
    <update check="always"/>

    <security>
        <all-permissions/>
    </security>

    <resources>
        <j2se version="1.6+"/>
        <jar href="Laits.jar" main="true"/>
<?php
foreach (glob("lib/*.jar") as $filename){
  echo "        <jar href=\"".$filename."\"/>\n";
}
echo "        <property name=\"jnlp.username\" value=\"$username\"/>\n";
echo "        <property name=\"jnlp.mode\" value=\"$mode\" />\n";
echo "        <property name=\"jnlp.problem\" value=\"$problem_id\"/>\n";
echo "        <property name=\"jnlp.section\" value=\"$section\"/>\n";
echo "        <property name=\"jnlp.server\" value=\"$codebase\"/>\n";
if($author){
  echo "        <property name=\"jnlp.author\" value=\"$author\"/>\n";
}
if($forum_url){
   echo "        <property name=\"jnlp.forumURL\" value=\"$forum_url\"/>\n";
}
if($new_problem){
   echo "        <property name=\"jnlp.newProblem\" value=\"$new_problem\"/>\n";
}
?>
    </resources>

    <application-desc main-class="edu.asu.laits.gui.Application"/>

</jnlp>
