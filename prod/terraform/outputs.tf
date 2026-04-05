output "server_id" {
  value = twc_server.vm.id
}

output "server_name" {
  value = twc_server.vm.name
}

output "public_ipv4" {
  value = twc_server_ip.vm_public_ip.ip
}
