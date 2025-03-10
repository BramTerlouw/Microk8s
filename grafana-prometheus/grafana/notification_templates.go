{{define "demo.title"}}
{{ if gt (len .Alerts.Firing) 0 }}
  {{ $first := index .Alerts.Firing 0 }}
    Critical: {{$first.Labels.application}}
{{ end }}
{{end }}

{{ define "demo.text" }}
{{if gt (len .Alerts.Firing) 0}}
The following checks are firing:
{{template "__demo-text_list_markdown" .Alerts.Firing}}
{{ end }}
{{ end }}

{{ define "__demo-text_list_markdown" }}{{range .}}
Check:
{{ if .Labels.name }}  - Check name = {{ .Labels.name }}{{ end }}
{{ if .Labels.details }}  - Check details = {{ .Labels.details }}{{ end }}

Source: {{.GeneratorURL}}
{{end}}
{{ end }}