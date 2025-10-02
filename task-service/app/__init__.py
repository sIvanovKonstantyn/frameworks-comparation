from flask import Flask
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.instrumentation.flask import FlaskInstrumentor
from opentelemetry.instrumentation.sqlalchemy import SQLAlchemyInstrumentor
from .config import Config
from .database import db


def create_app():
    from .routes.tasks import tasks_bp
    
    # Create Flask app
    app = Flask(__name__)
    app.config.from_object(Config)
    
    # Initialize database
    db.init_app(app)
    
    # Configure OpenTelemetry
    trace.set_tracer_provider(TracerProvider())
    zipkin_exporter = ZipkinExporter(endpoint="http://localhost:9411/api/v2/spans")
    span_processor = BatchSpanProcessor(zipkin_exporter)
    trace.get_tracer_provider().add_span_processor(span_processor)
    
    # Instrument Flask
    FlaskInstrumentor().instrument_app(app)
    
    # Create tables and instrument SQLAlchemy
    with app.app_context():
        db.create_all()
        SQLAlchemyInstrumentor().instrument(engine=db.engine)

    
    # Register blueprints
    app.register_blueprint(tasks_bp)
    
    return app

