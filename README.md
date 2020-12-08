# frac: Formula and Rules engine w/Actions, contained.

#### Development with AgensGraph docker image(s)
The bitnine/**agensBrowser** docker image comes with *AgensGraph DB* included but **neither the DB nor the webserver start automagically**.
So:
* create a docker volume (so that the DB persists across restarts):
  * eg agensGraphVolume:
    * docker volume create agensGraphVolume
* run the container w/the volume & shell: 
  * sudo docker run -itv agensGraphVolume:/home/agens/AgensGraph/data -p 80:8085 --name agensBrowser bitnine/agensBrowser /bin/bash
* change into the home of the DB and start it up:
  * cd /home/agens/AgensGraph && ag_ctl start
  * create the graph database:
    * createdb frac
* cd into the home of the webserver & start it up: 
  * cd /home/agens/AgensBrowser && ./agensbrowser.sh

On the machine hosting the docker container: browse to http://localhost

TODO: setup DB access ie Connection to 127.0.0.1:5432 refused (when trying to run query in agensBrowser)
i.e. specify the DB name in the webserver config 
TODO : add path here
