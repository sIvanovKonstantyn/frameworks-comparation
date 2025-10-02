from ..models.task import Task
from ..database import db

class TaskRepository:
    def get_all(self):
        return Task.query.all()
    
    def create(self, description, user_id):
        task = Task(description=description, userId=user_id)
        db.session.add(task)
        db.session.commit()
        return task