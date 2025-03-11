# Documentation

1: brew install microk8s argocd tektoncd-cli

2: microk8s install

3: microk8s start

4: microk8s status --wait-ready

5: microk8s enable dns storage registry dashboard

!! RBAC must be disabled !!



## Configure Prometheus
1: cd prometheus

2: 
- kubectl apply -f prometheus-configmap.yaml 
- kubectl apply -f prometheus-deployment.yaml
- kubectl apply -f prometheus-service.yaml



## Configure Grafana
1: helm repo add grafana https://grafana.github.io/helm-charts

2: helm repo update

3: microk8s helm install grafana grafana/grafana \
>  --namespace monitoring \
>  --set service.type=NodePort \
>  --set adminPassword=admin



## Build demo applications
1: multipass info microk8s-vm | grep IPv4 | awk '{ print $2 }' -> to grep the ip of container

2: http://<CONTAINER_IP>:32000/v2 -> should connect to internal registry and show empty json

3: 
- cd demo-app
- podman build -t demo-app .
- podman tag demo-app <CONTAINER_ID>32000/demo-app:latest
- podman push <CONTAINER_IP>:32000/demo-app:latest

4:
- cd exporter
- podman build -t exporter .
- podman tag exporter <CONTAINER_ID>32000/exporter:latest
- podman push <CONTAINER_IP>:32000/exporter:latest



## Deploy demo applications
1:
- kubectl create project demo-app1-prod
- kubectl create project demo-app2-prod

2: cd config/demo-app1

3: kubectl apply -k .

4: cd config/demo-app2

5: kubectl apply -k .



## Expose all services with port-forwarding in background jobs

### Find dashboard token
1: microk8s kubectl -n kube-system describe secret $(microk8s kubectl -n kube-system get secret | grep default-token | cut -d " " -f1)

2: Copy the "token" part

### Port-forwarding
1: kubectl port-forward -n kube-system svc/kubernetes-dashboard 10443:443 --address 0.0.0.0 >/dev/null 2>&1 &

2: kubectl port-forward -n monitoring svc/prometheus 9090:9090 >/dev/null 2>&1 &

3: kubectl port-forward -n monitoring svc/grafana 3000:80 >/dev/null 2>&1 &

4: (optional) kubectl port-forward -n demo-app1-prod svc/demo-app-1 8080:8080 (/metrics) or 8080:8081 (/metrics exporter) >/dev/null 2>&1 &

5: (optional) kubectl port-forward -n demo-app1-prod svc/demo-app-2 8081:8080 (/metrics) or 8081:8081 (/metrics exporter) >/dev/null 2>&1 &

### Navigate to services
6: multipass info microk8s-vm | grep IPv4 | awk '{ print $2 }' -> to grep the ip of container and browse to htps://$CONTAINER_IP:10433
7: All the other services are localhost:<first_port_in_port_forwarding_command>



## Grafana datasource configuration
1: Datasources -> Add datasource -> Select prometheus -> Connection url: http://prometheus.monitoring.svc.cluster.local:9090 -> Save en Test

2: Notification template -> Create new template group -> Copy/Paste from "notification_templates.go" -> Save

3: Contact point -> Add contact point -> Make from instructions "grafana/cp-teams-demo.yaml" -> Save

4: Alert rules -> Add new rule -> Make from instructions "grafana/alerting-demo-rule-group.yaml" -> Save