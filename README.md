Prerequisites:
- Install Java : https://www.java.com/en/download/manual.jsp
- Install Maven : https://maven.apache.org/install.html
- Install Eclipse Modeling Tools & Acceleo : https://www.eclipse.org/downloads/packages/ & https://eclipse.dev/acceleo/download.html

NOTE: Future software releases will include a .jar package that does not need Eclipse to run

Running instructions:
- Open Eclipse IDE and make sure Acceleo is correctly installed
- Import the org.lnu.cloudSimCreator project through File>Import...>General>Existing Projects into Workspace
- Create a new "Java Application" configuration from Run>Run Configurations...
- Set org.lnu.cloudSimCreator as "Project" and org.lnu.cloudSimCreator.Main as "Main class", then add the path of your desired UML model under the "Arguments" tab in the "Program arguments" field
- Press "Run" to execute the Reasoning Framework's Interpretation, the Simulation should execute immediately after
- If the executable fails at invoking Maven, launch the commands "mvn clean install" and "mvn exec:java -Dexec.mainClass=YOURGENERATEDCLASSPACKAGENAME.YOURGENERATEDCLASSNAME" to launch the Simulation from a Terminal / Shell
