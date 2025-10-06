from app import create_app

if __name__ == "__main__":
    app = create_app()
    print("Starting user service with Kafka consumer...")
    app.run(debug=True, port=5001)
    