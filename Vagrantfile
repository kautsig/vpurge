VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  # Run inside of virtualbox
  config.vm.provider "virtualbox"
  
  # Base box on debian jessie
  config.vm.box = "debian/jessie64"
  config.vm.boot_timeout = 400

  # Use this shell script to provision the box
  config.vm.provision "shell", path: "environment/provisioner.sh"

  # Always run apache starting when the box comes up (apache depends on nfs mounts to be present)
  # config.vm.provision :shell, :inline => "sudo service apache2 restart", run: "always"

  # Retreive an IP address for 
  config.vm.network "private_network", ip: "172.16.17.18"

  # Forward ports for rabbitmq
  config.vm.network "forwarded_port", guest: 5672, host: 5672
  config.vm.network "forwarded_port", guest: 15672, host: 15672

  # Forwarded ports for varnish
  config.vm.network "forwarded_port", guest: 6081, host: 6081
  
  # Enable NFS for the shared folder
  # config.vm.synced_folder ".", "/vagrant", type: "nfs"
  
  # Gets rid of tty warnings, see https://github.com/mitchellh/vagrant/issues/1673
  config.ssh.shell = "bash -c 'BASH_ENV=/etc/profile exec bash'"

end
