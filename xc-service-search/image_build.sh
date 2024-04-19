#!/bin/bash
# 删除原有docker镜像
docker rmi xc-service-search:1.0-SNAPSHOT
docker rmi swr.cn-southwest-2.myhuaweicloud.com/cloud-xczx/xc-service-search:$1
# 打包项目基础工程
mvn -f ../xc-framework-parent/pom.xml install
mvn -f ../xc-framework-model/pom.xml install
mvn -f ../xc-framework-utils/pom.xml install
mvn -f ../xc-framework-common/pom.xml install
mvn -f ../xc-service-api/pom.xml install
# maven插件构建docker镜像
mvn -f pom_docker.xml clean package -DskipTests docker:build
# 修改镜像名称(用于华为云平台镜像上传)
docker tag xc-service-search:1.0-SNAPSHOT swr.cn-southwest-2.myhuaweicloud.com/cloud-xczx/xc-service-search:$1
# 上传镜像
docker push swr.cn-southwest-2.myhuaweicloud.com/cloud-xczx/xc-service-search:$1