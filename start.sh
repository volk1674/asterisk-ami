#!/bin/bash

docker run -d --rm \
-e NOTIFICATION_URL=http://2969d404-0b82-4ea6-bf9c-aa378053b4a9.mock.pstmn.io/notify \
-e AMI_HOST=192.168.0.110 \
-e AMI_PORT=5038 \
-e AMI_USER=test_ami \
-e AMI_PASSWORD=test_ami \
-p 9080:8080
--name asterisk-java-server asterisk-java-server:0.1.0