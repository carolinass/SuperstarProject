#!/usr/bin/env bash

apt-get update

# Instalando Postgres
apt-get install -y postgresql-9.4 -q
cp /vagrant/pg_hba.conf /etc/postgresql/9.4/main/
/etc/init.d/postgresql restart