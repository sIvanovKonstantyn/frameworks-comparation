from flask import Flask
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.instrumentation.flask import FlaskInstrumentor
from opentelemetry.instrumentation.pymongo import PymongoInstrumentor
from .config import Config
from .database import mongo
from .services.kafka_consumer import kafka_consumer
from .clients.task_service_client import task_service_client

def create_app():
    from .routes.users import users_bp
    
    # Create Flask app
    app = Flask(__name__)
    app.config.from_object(Config)
    
    # Initialize MongoDB
    mongo.init_app(app)
    
    # Configure OpenTelemetry
    trace.set_tracer_provider(TracerProvider())
    zipkin_exporter = ZipkinExporter(endpoint="http://localhost:9411/api/v2/spans")
    span_processor = BatchSpanProcessor(zipkin_exporter)
    trace.get_tracer_provider().add_span_processor(span_processor)
    
    # Instrument Flask and PyMongo
    FlaskInstrumentor().instrument_app(app)
    PymongoInstrumentor().instrument()
    
    # Initialize task service client
    task_service_client.init_app(app)
    
    # Initialize and start Kafka consumer
    kafka_consumer.init_app(app)
    kafka_consumer.start_consuming()
    
    # Register blueprints
    app.register_blueprint(users_bp)
    
    return app

