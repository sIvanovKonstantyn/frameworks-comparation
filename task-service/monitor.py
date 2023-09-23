import subprocess
import time
import re

# Define the command you want to execute
command = "docker exec 4752cdbda4a1 jcmd 1 VM.native_memory"

# Define regular expressions to extract the desired data
total_committed_pattern = r"Total:.*committed=(\d+)KB"
heap_committed_pattern = r"Java Heap.*committed=(\d+)KB"
gc_committed_pattern = r"GC.*committed=(\d+)KB"

try:
    while True:
        # Execute the command and capture its output
        result = subprocess.run(
            command,
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            universal_newlines=True
        )

        # Check if the command executed successfully
        if result.returncode == 0:
            # Parse the command output if needed
            text = result.stdout

            # Find matches in the text using regular expressions
            total_committed = re.search(total_committed_pattern, text).group(1)
            heap_committed = re.search(heap_committed_pattern, text).group(1)
            gc_committed = re.search(gc_committed_pattern, text).group(1)

            # Specify the file path to save the output
            output_file_path = "monitor.csv"

            # Save the parsed output to a file
            with open(output_file_path, "a") as output_file:
                output_file.write(f"{total_committed};{heap_committed};{gc_committed}\n")

            print(f"Command output saved to {output_file_path}")
        else:
            print("Error executing the command:")
            print(result.stderr)

        # Wait for the specified interval before running the command again
        time.sleep(5)

except KeyboardInterrupt:
    print("Loop terminated by user (Ctrl+C)")

except Exception as e:
    print(f"An error occurred: {str(e)}")