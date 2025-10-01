import os
import psutil
import time

process = psutil.Process(16600)#add PID

while True:
    # CPU percent over the last 1 second
    cpu = process.cpu_percent(interval=1)
    
    # Memory usage (RSS = resident memory)
    mem = process.memory_info().rss / (1024 * 1024)  # in MB
    
    print(f"CPU: {cpu:.1f}% | Memory: {mem:.2f} MB")