import time
from threading import Thread
from app import create_app

if __name__ == "__main__":
    start_total = time.time()
    
    # Step 1: create app
    start_app = time.time()
    app = create_app()
    end_app = time.time()
    print(f"App factory executed in {end_app - start_app:.3f} seconds")

    # Step 2: start server in a thread
    def run_server():
        app.run(debug=False, use_reloader=False)  # disable reloader for clean timing

    t = Thread(target=run_server)
    t.start()
    # run_server()
    # # Optional: wait a short moment for server to bind (approximation)
    time.sleep(0.2)

    end_total = time.time()
    print(f"Total startup time (app + server ready): {end_total - start_total:.3f} seconds")
    