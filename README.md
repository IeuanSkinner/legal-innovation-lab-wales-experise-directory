# Legal Innovation Lab Wales
### Challenge Task: Expertise Directory

This repository houses my challenge task implementation for "Legal Innovation Lab Wales". For this challenge task I have
decided to implement a variant on the [Directory of Expertise](https://directoryofexpertise.legaltech.wales/) that has 
been created for Swansea Universities School of Law and provide a directory of expertise for the 
[College of Science](https://www.swansea.ac.uk/staff/science/).

The core technologies used for this project are:
* Java 11 running the Helidon MP framework. This Java app provides a web server capable of serving static content to
clients as well as handling REST requests where needed, the bulk of the back-end infrastructure I've built at SCUK 
utilises Java 11 + Helidon and I'm definitely an evangelist for it.

* Python. The python script is used to fetch the relevant data pertaining to each member of staff, this data is then 
collated into a JSON structure for ease of use.

---

### Starting Web Server

To run this application you will need to have [Java 11](https://openjdk.java.net/projects/jdk/11/) 
and [Maven](https://maven.apache.org/) installed on your machine.

Build the application from the project root folder

``mvn clean install``

Run the application

``java -jar target/legal-innovation-lab-wales.jar``

You should now be able view the main web page at 

``http://localhost:8080``