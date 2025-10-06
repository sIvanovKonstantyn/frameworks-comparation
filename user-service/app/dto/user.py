from dataclasses import dataclass
from typing import List

@dataclass
class User:
    id: str
    name: str
    task_ids: List[int]