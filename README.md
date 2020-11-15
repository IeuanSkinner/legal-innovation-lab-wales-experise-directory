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
collated into a JSON structure for ease of use. A second python script is then used to index this data in an 
ElasticSearch cluster.

* HTML, CSS + JS. All HTML, CSS and JS for this project is coded to the HTML5, CSS3 and ES6 JS standards. A key feature
I would add to this codebase would be a build tool for minification of the JS and utilising SASS.

* ElasticSearch. An ElasticSearch cluster has been configured within ElasticCloud to house the index for this data. This
cluster is using the free-trial period so won't be around from Nov 26th 2020. The core configuration involves a new role
and user with permission to read from the cluster leveraged by the back-end web service along with the admin user 
than can write to the cluster which is leveraged by the python script. These admin credentials are not provided so you
won't be able to run the index data script but there is a configured Github Action for it you can view.

* Github Action. A Github action has been set up to run the fetch and index scripts on a scheduled basis 
[here](https://github.com/IeuanSkinner/legal-innovation-lab-wales/actions).

* Google Cloud App Engine. A live version of this project has been deployed using Google App Engine 
[here](https://legal-innovation-lab-wales.nw.r.appspot.com/). I will take this down from Nov 26th.

---

### Running Python Scripts

To run the python scripts you will need to have Python 3 installed along with the following packages

``pip install requests beautifulsoup4 clean-text[gpl] elasticsearch``

You won't be able to run the index_data python script without the requisite credentials but you can see this script
running in the Github Action linked in above section.

---

### Running Web Server Locally

To run this application you will need to have [Java 11](https://openjdk.java.net/projects/jdk/11/) 
and [Maven](https://maven.apache.org/) installed on your machine.

Build the application from the project root folder

``mvn clean install``

Run the application

``java -jar target/legal-innovation-lab-wales.jar``

You should now be able view the main web page at 

``http://localhost:8080``

---

### Limitations

This project is dependent on scraping the Swansea University website to find the members of staffs expertise, should the
site structure change this may not be possible. Furthermore, this project depends on staff members filling out their 
Areas of Expertise on their profiles, currently about 50% of College of Science staff members have not done this. 
A future enhancement could be the ability to generate a reminder email to those members of staff who haven't filled out
this section to do so.

---

### Key Features

Some key features of this project include

* Highlighting for matched keyword terms.

* Departmental filter alongside keyword filter.

* Open API for other developers to search this indexed data, this is as yet undocumented but a future improvement
 would be to do this.

* Mobile friendly UI.

* Incremental result loading.

![](desktop_demo.gif)

![](mobile_demo.gif)