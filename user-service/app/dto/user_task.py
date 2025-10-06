from dataclasses import dataclass
from typing import List

@dataclass
class UserTask:
    id: str
    name: str
    task_descriptions: List[str]