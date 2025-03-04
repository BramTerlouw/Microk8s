from flask import Flask, Response, request
import random

app = Flask(__name__)

# Initial state
app_status = 'OK'

# Fixed messages for each state
STATE_MESSAGES = {
    'OK': 'None',
    'WARNING': 'High latency detected',
    'ERROR': 'Service unavailable'
}

@app.route('/update')
def update():
    global app_status
    # Get the desired state from the query parameter 'state', default to current state if not provided
    new_state = request.args.get('state', app_status).upper()
    
    # Validate and update the state
    if new_state in ['OK', 'WARNING', 'ERROR']:
        app_status = new_state
        return f"Status changed to: {app_status}"
    else:
        return f"Invalid state: {new_state}. Use 'OK', 'WARNING', or 'ERROR'.", 400

@app.route('/metrics')
def metrics():
    # Map status to numeric value
    status_value = {'OK': 0, 'WARNING': 1, 'ERROR': 2}[app_status]
    # Get the fixed message for the current state
    message = STATE_MESSAGES[app_status]

    prometheus_res = (
        "# HELP app_status Application status (0=OK, 1=WARNING, 2=ERROR)\n"
        "# TYPE app_status gauge\n"
        f"app_status {status_value}\n\n"
        "# HELP app_status_message Current status message\n"
        "# TYPE app_status_message gauge\n"
        f'app_status_message{{message="{message}"}} 1\n'
    )

    return Response(prometheus_res.strip(), mimetype="text/plain")

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
