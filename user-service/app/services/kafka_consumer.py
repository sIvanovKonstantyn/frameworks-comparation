import json
import threading
from confluent_kafka import Consumer
from ..repository.user_repository import UserRepository

class KafkaConsumerService:
    def __init__(self):
        self.consumer = None
        self.user_repo = UserRepository()
    
    def init_app(self, app):
        self.consumer = Consumer({
            'bootstrap.servers': app.config['KAFKA_BOOTSTRAP_SERVERS'],
            'group.id': 'user-service-group',
            'auto.offset.reset': 'earliest'
        })
        self.consumer.subscribe(['tasks'])
    
    def start_consuming(self):
        def consume_messages():
            while True:
                try:
                    msg = self.consumer.poll(1.0)
                    if msg is None:
                        continue
                    if msg.error():
                        print(f"Consumer error: {msg.error()}")
                        continue
                    
                    task_data = json.loads(msg.value().decode('utf-8'))
                    self.user_repo.update_tasks(task_data['userId'], task_data['id'])
                    print(f"Updated user {task_data['userId']} with task {task_data['id']}")
                    
                except Exception as e:
                    print(f"Error processing message: {e}")
        
        threading.Thread(target=consume_messages, daemon=True).start()

kafka_consumer = KafkaConsumerService()