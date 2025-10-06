from dataclasses import dataclass
from typing import Optional

@dataclass
class Task:
    id: Optional[int]
    description: str
    userId: str