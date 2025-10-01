from flask import Blueprint, jsonify
from ..dto.task import Task

tasks_bp = Blueprint('tasks', __name__)

@tasks_bp.route("/tasks", methods=["GET"])
def get_tasks():
    tasks = [
        Task(id=1, description="task1", userId=1),
        Task(id=2, description="task2", userId=2)
    ]
    return jsonify([task.__dict__ for task in tasks])

@tasks_bp.route("/tasks", methods=["POST"])
def create_task():
    return "", 201