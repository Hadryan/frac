# frac: Formula and Rules engine w/Actions, contained.

#### Development with AgensGraph docker image(s)
The bitnine/**agensBrowser** docker image comes with *AgensGraph DB* included but **the webserver doesn't start automagically**.
So:
* create a couple of docker volumes (so that the DB & webserver configuration persist across restarts):
  * eg agensGraphVolume:
    * docker volume create agensGraphVolume
    * docker volume create agensBrowserVolume
* create the container w/the volumes. ports & shell, starting it as a daemon: 
  * sudo docker run -itd -v agensGraphVolume:/home/agens/AgensGraph/data -v agensBrowserVolume:/home/agens/AgensBrowser -p 80:8085 -p 5432:5432 --name agensBrowser bitnine/agensBrowser /bin/bash
* shell into the container:
  * sudo docker exec -it agensBrowser /bin/bash
  * create the graph database and the graph *in* the database (once is enough 8^):
    * from the shell: 
      * createdb frac
      * agens frac
        * from the PostgreSQLAgensGraph CLI i.e. *agens*:
          * CREATE GRAPH frac;
          * SET GRAPH_PATH = frac;
          * \dG
          * \q
* cd into the home of the webserver, configure it & start it (i.e. an executable JAR) up: 
  * cd /home/agens/AgensBrowser
  * vi agens-browser.config.yml:
    * change agens:outer:datasource:graph_path to: frac
  * ./agensbrowser.sh

On the machine hosting the docker container: browse to http://localhost

https://github.com/bladewheels/frac/blob/master/src/test/resources/img/frac.agensBrowser.formula.example.png

<img src="frac\src\test\resources\img\frac.agensBrowser.formula.example.png" width="800px" height="auto">

<img src="frac/src/test/resources/img/frac.agensBrowser.formula.example.png" width="800px" height="auto">

