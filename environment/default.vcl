# https://www.varnish-cache.org/docs/4.0/users-guide/purging.html

vcl 4.0;

backend default {
    .host = "127.0.0.1";
    .port = "8080";
}

acl purge {
    "localhost";
    "127.0.0.1";
    "172.168.0.0/16";
    "10.0.0.0/8";
}

sub vcl_recv {
    if (req.method == "PURGE") {
        if (!client.ip ~ purge) {
            return(synth(405, "Not allowed."));
        }
        return (purge);
    }
}

