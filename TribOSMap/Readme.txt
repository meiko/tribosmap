1. you can import this folder as an eclipse project

2. you will need the android and scala eclipse plugins

3. change the CLASSPATH File:
<classpath>
	<classpathentry kind="lib" path="/***/workspace/TribOSMap/bin"/> <!-- path to the bin folder of this project -->
	<classpathentry kind="src" path="src"/>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
	<classpathentry kind="con" path="org.eclipse.jdt.USER_LIBRARY/MyDroid"/> <!-- the android library -->
	<classpathentry kind="lib" path="/***/jars/scala-android-library.jar" sourcepath="/home/meiko/tribosmap/jars/scalaAndroidSource"/> <!-- the scala library (fixed for android)-->
	<classpathentry kind="output" path="bin"/>
</classpath>