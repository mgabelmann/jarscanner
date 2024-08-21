# JarScanner
Scans a jar file looking for MANIFEST.MF or a pom.xml file. If either is found the 
program will try to determine the version.

This is useful when you are using a JDK or JRE that is of a lower version than
one of the libraries you are using. In this case you will get a MAJOR MINOR version
error which can be hard to diagnose. By running this utility you can find out which
library is the problem and downgrade or remove.

## Project Information

