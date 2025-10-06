from flask import Blueprint, jsonify, request
from ..repository.user_repository import UserRepository
from ..services.user_service import user_service

users_bp = Blueprint('users', __name__)
user_repo = UserRepository()

@users_bp.route("/users", methods=["GET"])
def get_users():
    users = user_repo.get_all()
    return jsonify(users)

@users_bp.route("/users/tasks", methods=["GET"])
def get_users_tasks():
    users_tasks = user_service.get_all_users_tasks()
    return jsonify([{
        'id': ut.id,
        'name': ut.name,
        'task_descriptions': ut.task_descriptions
    } for ut in users_tasks])

@users_bp.route("/users", methods=["POST"])
def create_user():
    data = request.get_json()
    user = user_repo.save(data['name'], data.get('task_ids', []))
    return jsonify(user), 201