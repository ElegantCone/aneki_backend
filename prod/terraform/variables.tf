variable "location" {
  type = string
  default = "ru-1"
}

variable "os_version" {
  type = string
  default = "24.04"
}

variable "server_name" {
  type = string
  default = "aneki-server"
}

variable "ssh_key_name" {
  type = string
  default = "aneki_ssh_key"
}

variable "ssh_public_key_path" {
  type = string
  default = "~/ssh-key-timeweb.pub"
}

variable "twc_token" {
  type = string
  sensitive = true
}