from flask import Flask
from datetime import datetime, timezone

app = Flask(__name__)

@app.route('/')
def index():
    current_time = datetime.now(timezone.utc).strftime('%Y-%m-%dT%H:%M:%S+00:00')
    return f'''
    <html>
    <head>
        <title>Rozwiązanie zadania PZC 2.1</title>
    </head>
    <body>
    {current_time}
    </body>
    </html>
    '''

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
