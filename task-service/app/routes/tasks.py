from flask import Blueprint, jsonify, request
from ..repository.task_repository import TaskRepository

tasks_bp = Blueprint('tasks', __name__)
task_repo = TaskRepository()

@tasks_bp.route("/tasks", methods=["GET"])
def get_tasks():
    tasks = task_repo.get_all()
    return jsonify([{"id": task.id, "description": task.description, "userId": task.userId} for task in tasks])

@tasks_bp.route("/tasks", methods=["POST"])
def create_task():
    data = request.get_json()
    task = task_repo.create(data['description'], data['userId'])
    return jsonify({"id": task.id, "description": task.description, "userId": task.userId}), 201