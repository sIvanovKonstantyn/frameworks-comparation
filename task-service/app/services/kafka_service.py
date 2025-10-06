import json
import threading
from confluent_kafka import Producer
from flask import current_app

class KafkaService:
    def __init__(self):
        self.producer = None
    
    def init_app(self, app):
        self.producer = Producer({
            'bootstrap.servers': app.config['KAFKA_BOOTSTRAP_SERVERS']
        })
    
    def publish_task_async(self, task_id, description, user_id):
        def delivery_report(err, msg):
            if err is not None:
                print(f'Message delivery failed: {err}')
        
        message = {
            'id': task_id,
            'description': description,
            'userId': user_id
        }
        
        threading.Thread(
            target=self._publish_message,
            args=('tasks', json.dumps(message), delivery_report)
        ).start()
    
    def _publish_message(self, topic, message, callback):
        self.producer.produce(topic, message, callback=callback)
        self.producer.flush()

kafka_service = KafkaService()