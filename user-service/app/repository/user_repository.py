from ..database import mongo
from bson import ObjectId

class UserRepository:
    def get_all(self):
        users = list(mongo.db.users.find())
        for user in users:
            user['id'] = str(user['_id'])
            del user['_id']
        return users
    
    def save(self, name, task_ids=[]):
        user_data = {
            'name': name,
            'task_ids': task_ids
        }
        result = mongo.db.users.insert_one(user_data)
        return {
            'id': str(result.inserted_id),
            'name': name,
            'task_ids': task_ids
        }
    
    def update_tasks(self, user_id, task_id):
        mongo.db.users.update_one(
            {'_id': ObjectId(user_id)},
            {'$addToSet': {'task_ids': task_id}}
        )