terraform {
  required_providers {
    twc = {
      source = "tf.timeweb.cloud/timeweb-cloud/timeweb-cloud"
    }
  }
  required_version = ">= 1.4.4"
}

provider "twc" {
  token = var.twc_token
}

data "twc_configurator" "configurator" {
  location = var.location
  preset_type = "premium"
}

data "twc_os" "os" {
  name = "ubuntu"
  version = var.os_version
}

resource "twc_ssh_key" "default" {
  name = "aneki-ssh-key"
  body = file(pathexpand(var.ssh_public_key_path))
}

resource "twc_server" "vm" {
  name = var.server_name
  os_id = data.twc_os.os.id

  ssh_keys_ids = [twc_ssh_key.default.id]

  configuration {
    configurator_id = data.twc_configurator.configurator.id
    disk = 1024 * 15
    cpu = 1
    ram = 1024
  }
}

resource "twc_server_ip" "vm_public_ip" {
  source_server_id = twc_server.vm.id
  type      = "ipv4"
}