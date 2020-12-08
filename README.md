# frac: Formula and Rules engine w/Actions, contained.

#### Development with AgensGraph docker image(s)
The bitnine/**agensBrowser** docker image comes with *AgensGraph DB* included but **neither the DB nor the webserver start automagically** (however, the bitnine/agensGraph image/container **does** automagically startup the DB engine i.e. PostgreSQL).
So:
* create a docker volume (so that the DB persists across restarts):
  * eg agensGraphVolume:
    * docker volume create agensGraphVolume
* run the container w/the volume & shell: 
  * sudo docker run -itv agensGraphVolume:/home/agens/AgensGraph/data -p 80:8085 --name agensBrowser bitnine/agensBrowser /bin/bash
* change into the home of the DB and start it up:
  * cd /home/agens/AgensGraph && ag_ctl start
  * create the graph database and the graph *in* the database (once is enough 8^):
    * from the shell: 
      * createdb frac
      * agens frac
        * from the PostgreSQLAgensGraph CLI i.e. *agens*:
          * CREATE GRAPH frac;
          * \dG
          * \q
* cd into the home of the webserver, configure it & start it (i.e. an executable JAR) up: 
  * cd /home/agens/AgensBrowser
  * vi agens-browser.config.yml:
    * change agens:outer:datasource:graph_path to: frac
  * ./agensbrowser.sh

On the machine hosting the docker container: browse to http://localhost
