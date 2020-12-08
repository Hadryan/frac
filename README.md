# frac
Formula and Rules engine w/Actions, contained

The bitnine/**agensBrowser** docker image comes with *AgensGraph DB* included but the webserver **doesn't start automagically**.
So:
* create a docker volume:
 * eg agensGraphVolume: TBD
* run the container w/the volume & shell: 
 * sudo docker run -itv agensGraphVolume:/home/agens/AgensGraph/data -p 80:8085 --name agensBrowser bitnine/agensBrowser /bin/bash
* cd into the home of the webserver & start it up: 
 * cd /home/agens/AgensBrowser && ./agensbrowser.sh

On the machine hosting the docker container: browse to http://localhost

TODO: setup DB access ie Connection to 127.0.0.1:5432 refused (when trying to run query in agensBrowser)
