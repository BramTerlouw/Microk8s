# Documentation

1: brew install microk8s argocd tektoncd-cli

2: microk8s install

3: microk8s start

4: microk8s status --wait-ready

5: microk8s enable dns storage registry dashboard

!! RBAC must be disabled !!



## Find dashboard token
1: multipass exec microk8s-vm -- sudo /snap/bin/microk8s kubectl -n kube-system describe secret $(multipass exec microk8s-vm -- sudo /snap/bin/microk8s kubectl -n kube-system get secret | grep default-token | cut -d " " -f1)

2: Copy the "token" part



## Expose dashboard
1: multipass exec microk8s-vm -- sudo /snap/bin/microk8s kubectl port-forward -n kube-system service/kubernetes-dashboard 10443:443 --address 0.0.0.0

2: multipass info microk8s-vm | grep IPv4 | awk '{ print $2 }' -> to grep the ip of container

3: Browser to htps://$CONTAINER_IP:10433 -> paste the token from previous step



## Configure Prometheus
1: cd prometheus

2: 
- kubectl apply -f prometheus-configmap.yaml 
- kubectl apply -f prometheus-deployment.yaml
- kubectl apply -f prometheus-service.yaml
- kubectl port-forward -n monitoring svc/prometheus 9090:9090



## Configure Grafana
1: helm repo add grafana https://grafana.github.io/helm-charts

2: helm repo update

3: microk8s helm install grafana grafana/grafana \
>  --namespace monitoring \
>  --set service.type=NodePort \
>  --set adminPassword=admin

4: microk8s kubectl port-forward svc/grafana 3000:80 -n monitoring



## Build demo applications
1: multipass info microk8s-vm | grep IPv4 | awk '{ print $2 }' -> to grep the ip of container

2: http://<CONTAINER_IP>:32000/v2 -> should connect to internal registry and show empty json

3: cd demo-app

4: podman build . -t demo-app

5: podman tag demo-app <CONTAINER_ID>32000/demo-app:latest

6: podman push <CONTAINER_IP>:32000/demo-app:latest



## Deploy demo applications
1:
- kubectl create project demo-app1-prod
- kubectl create project demo-app2-prod

2: cd config/demo-app1

3: kubectl apply -k .

4: cd config/demo-app2

5: kubectl apply -k .



## Grafana datasource configuration
Datasources -> Add datasource -> Select prometheus -> Connection url: http://prometheus.monitoring.svc.cluster.local:9090 -> Save en Test