from flask import Flask
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.zipkin.json import ZipkinExporter
from opentelemetry.instrumentation.flask import FlaskInstrumentor


def create_app():
    from .routes.tasks import tasks_bp
    # Configure OpenTelemetry
    trace.set_tracer_provider(TracerProvider())
    
    # Configure Zipkin exporter
    zipkin_exporter = ZipkinExporter(
        endpoint="http://localhost:9411/api/v2/spans"
    )
    
    # Add span processor
    span_processor = BatchSpanProcessor(zipkin_exporter)
    trace.get_tracer_provider().add_span_processor(span_processor)
    
    # Create Flask app
    app = Flask(__name__)
    
    # Instrument Flask app
    FlaskInstrumentor().instrument_app(app)
    
    # Register blueprints
    app.register_blueprint(tasks_bp)
    
    return app

