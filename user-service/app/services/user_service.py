from ..repository.user_repository import UserRepository
from ..clients.task_service_client import task_service_client
from ..dto.user_task import UserTask

class UserService:
    def __init__(self):
        self.user_repository = UserRepository()
        self.tasks_service_client = task_service_client
    
    def get_all_users_tasks(self):
        users = self.user_repository.get_all()
        tasks = self.tasks_service_client.get_all()
        
        task_map = {task['id']: task['description'] for task in tasks}

        return [
            UserTask(
                id=user['id'],
                name=user['name'],
                task_descriptions=[
                    task_map[task_id]
                    for task_id in user.get('task_ids', [])
                    if task_id in task_map
        ],
            )
            for user in users
        ]        

user_service = UserService()