# Documentation

1: brew install microk8s argocd tektoncd-cli

2: microk8s install

3: microk8s start

4: microk8s status --wait-ready

5: microk8s enable dns storage ingress dashboard



## Setup of Argo Cd
1: microk8s kubectl create namespace argocd

2: microk8s kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

3: microk8s kubectl port-forward svc/argocd-server -n argocd 8080:443



## Login web ui Argo Cd
3.1: microk8s kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 --decode

3.2: Navigate to https://localhost:8080

3.2: Username: admin, password extracted from argocd-initial-admin-secret



## Login Argo CD cli
4: argocd login localhost:8080 --username admin --password <your-password> --insecure

5: argocd account list



## Add microk8s cluster to Argo Cd
1: microk8s kubectl config view --raw > ~/.kube/microk8s.config

2: export KUBECONFIG=~/.kube/microk8s.config

3: kubectl config get-contexts -o name

4: argocd cluster add microk8s



## Setup of Tekton
1: microk8s kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml

2: microk8s kubectl get pods --namespace tekton-pipelines



## Setup your own Git repo
1: git clone https://github.com/BramTerlouw/Microk8s.git

2: create your own git repo with the name "Microk8s".

3: push /application/deployment.yaml to your repo.



## Setup Argo Cd app
1: microk8s kubelet create -f /argocd/argocd-app.yaml

2: verify in argocd-ui if app appears.



## Create secret with your Git credentials
1: microk8s kubectl create secret generic git-credentials \
>    --from-literal=git-username=<your_username> \
>    --from-literal=git-email=<your_email> \
>    --from-literal=git-token=<your_personal_access_token> -n default



## Setup pipeline and its task
1: microk8s kubectl apply -f /pipeline/pipeline.yaml

2: microk8s kubectl apply -f /pipeline/task.yaml



## Trigger pipeline manually
1: microk8s kubectl apply -f /pipeline/pipeline-run.yaml 
