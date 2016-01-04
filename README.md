# vpurge - a tiny varnish purge tool

vpurge is a tiny tool for purging URLs from multiple varnish
instances. Purge requests are distributed through rabbitmq.

Please be aware that this tool has not been proven in
production. There might be pitfalls, it was written for testing
purposed to create something potentially useful with clojure.

## Installation

Download from https://github.com/kautsig/vpurge

## Usage

### Vagrant environment

To set up a testing environment vagrant can be used. Vagrant will
install a debian system containing rabbitmq and varnish with the basic
configuration verify the basic functionality.

Start vagrant (takes a while when doing it for the first time):

    $ vagrant up

Now you should be able to access the rabbitmq interface on
http://localhost:15672/ using the default credentials admin/nimda.

### Using varnishlog

To see purge requests comming in you can simply run varnishlog within
vagrant.

    $ vagrant ssh
    $ sudo varnishlog

### Running consumers and publishing purge requests

Run multiple consumers (n for node id):

    $ lein run -n 1
    $ lein run -n 2

Run example producer:

    $ lein run -m vpurge.producer

Now you should see each consumer receiving purge requests and passing
them on to the varnish server. In real life you would run each vpurge
node instance along with varish, so that every vpurge instance takes
care for his own varnish. For testing having a single varnish instance
seems sufficient.

### Run consumers from single jar file

Run create an run from a single jar file:

    $ lein uberjar
    $ cd target/uberjar/
    $ java -jar vpurge-0.1.0-SNAPSHOT-standalone.jar -n 1

## Options

See src/vpurge/core.clj

## License

See LICENSE.txt

Copyright © 2015 kautsig
