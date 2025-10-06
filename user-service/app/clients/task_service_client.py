import requests
from flask import current_app

class TaskServiceClient:
    def __init__(self):
        self.base_url = None
    
    def init_app(self, app):
        self.base_url = app.config['TASK_SERVICE_URL']
    
    def get_all(self):
        try:
            response = requests.get(f"{self.base_url}/tasks")
            response.raise_for_status()
            return response.json()
        except requests.RequestException as e:
            print(f"Error fetching tasks: {e}")
            return []

task_service_client = TaskServiceClient()