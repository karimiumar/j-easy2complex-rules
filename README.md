**Requirements:**
It requires SimplyJdbc library. A jar file simply-jdbc-2.0.jar is provided in the lib directory.
Use the given maven command to install it locally.

```mvn install:install-file -Dfile=simply-jdbc-2.0.jar -DgroupId=com.umar -DartifactId=simply-jdbc -Dversion=2.0 -Dpackaging=jar```

Once installed use ```mvn clean compile test``` command
to test rule-engine. 
