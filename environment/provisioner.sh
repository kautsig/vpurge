#!/bin/bash

# Update the systems pgp keys
apt-get update
apt-get -q -y install debian-keyring debian-archive-keyring

# Install required packages (debian distro packages)
apt-get -q -y install varnish
apt-get -q -y install rabbitmq-server

# Set up varnish, remind that this copies
rm /etc/varnish/default.vcl
ln -s /vagrant/environment/default.vcl /etc/varnish/default.vcl

# Restart varnish to allow the changed config to take effect
service varnish restart

# Enable managment plugin
rabbitmq-plugins enable rabbitmq_management

# Restart rabbitmq  to allow the changed config to take effect
service rabbitmq-server restart

rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app

# Add the admin user
rabbitmqctl add_user admin nimda
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

# Get the management python script from the management plugin web
# interface (yes, seriously!)
wget http://localhost:15672/cli/rabbitmqadmin
cp rabbitmqadmin /usr/bin/
chmod 755 /usr/bin/rabbitmqadmin

