#!/usr/bin/env bash

spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=apparels-service \
  --package-name=com.footballstore.apparels \
  --groupId=com.footballstore.apparels \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  apparels-service

spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=customers-service \
  --package-name=com.footballstore.customers \
  --groupId=com.footballstore.customers \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  customers-service

spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=warehouses-service \
  --package-name=com.footballstore.warehouses \
  --groupId=com.footballstore.warehouses \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  warehouses-service

spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=orders-service \
  --package-name=com.footballstore.orders \
  --groupId=com.footballstore.orders \
  --dependencies=web,webflux,validation \
  --version=1.0.0-SNAPSHOT \
  orders-service

spring init \
  --boot-version=3.4.4 \
  --build=gradle \
  --type=gradle-project \
  --java-version=17 \
  --packaging=jar \
  --name=api-gateway \
  --package-name=com.footballstore.apigateway \
  --groupId=com.footballstore.apigateway \
  --dependencies=web,webflux,validation,hateoas \
  --version=1.0.0-SNAPSHOT \
  api-gateway
